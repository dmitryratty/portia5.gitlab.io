import java.io.File
import java.nio.file.Paths
import kotlin.text.StringBuilder

class PagesGenerator {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            PagesGenerator().main()
        }
    }

    // "<span class=\"nowrap\">$1</span>"
    private val dashNoWrap = "(\\S+-\\S+)".toRegex()
    // "<a href=\"$1\">\$1</a>"
    private val hyperlink = "(http\\S+)".toRegex()
    // "<span class=\"nowrap\">$1</span>"
    private val footnote = "(\\S+\\[\\d+])".toRegex()
    private val wbrBefore = "([/~.,\\-_?#%])".toRegex()
    private val wbrAfter = "([:])".toRegex()
    private val wbrBeforeAfter = "([=&])".toRegex()

    fun main() {
        Library().main()
        val projectDir = Paths.get("ratty-public")
        val resourcesDir = projectDir.resolve("src/main/resources")
        val pageTemplate = resourcesDir.resolve("page-template.html").toFile().readText()
        listOf("game", "tech", "math", "library", "life").forEach {
            val txtFile = resourcesDir.resolve("$it.txt").toFile()
            val text = reformatTxt(txtFile.readText())
            txtFile.writeText(text)
            val htmlFile = projectDir.resolve("public/$it.html").toFile()
            generateHtml(pageTemplate, text, htmlFile)
        }
    }

    private fun reformatTxt(raw: String): String {
        // Replace "..." with html entity instead "…"?
        return raw.replace("...", "…")
    }

    private fun makeNoWrap(word: String): String {
        return "<span class=\"nowrap\">$word</span>"
    }

    private fun longUrlLineBreaks(url: String): String {
        // https://css-tricks.com/better-line-breaks-for-long-urls/
        return url.split("//").joinToString("//<wbr>") { part ->
            // Insert a word break opportunity after a colon
            part.replace(wbrAfter, "\$1<wbr>")
                // Before a single slash, tilde, period, comma, hyphen, underline,
                // question mark, number sign, or percent symbol.
                .replace(wbrBefore, "<wbr>\$1")
                // Before and after an equals sign or ampersand
                .replace(wbrBeforeAfter, "<wbr>\$1<wbr>")
        }
    }

    private fun processLine(line: String): String? {
        if (!line.contains("http") && !line.contains('-') && !line.contains('[')
            && !line.contains("<•>") && !line.contains('❖')) {
            return null
        }
        val builder = StringBuilder()
        line.split(' ').forEach { word ->
            if (builder.isNotEmpty()) {
                builder.append(' ')
            }
            if (word.isEmpty()) {
                // Leading space in string, for example for padding.
                //builder.append(" ")
            } else if (word.length == 1) {
                builder.append(word)
            } else if (word.startsWith("http")) {
                builder.append("<a href=\"$word\">${longUrlLineBreaks(word)}</a>")
            } else if (word.contains('-')) {
                builder.append(makeNoWrap(word))
            } else if (word.contains('[') && footnote.matches(word)) {
                // Footnote inside text, like "Hello, world![2]" and
                // we make "world![2]" no wrap.
                builder.append(makeNoWrap(word))
            } else if (word == "<•>") {
                builder.append(makeNoWrap(word))
            } else {
                builder.append(word)
            }
        }
        return builder.toString()
    }

    private fun generateHtml(pageTemplate: String, text: String, htmlFile: File) {
        val title = StringBuilder()
        val article = StringBuilder()
        val paragraphs = text.split("\n\n")
        paragraphs.forEach { paragraph ->
            if (title.isEmpty()) {
                title.append(paragraph)
                return@forEach
            }
            if (article.isNotEmpty()) {
                article.append("\n\n    ")
            }
            if (paragraph.contains("* * *")) {
                if (paragraph != "* * *") {
                    throw IllegalStateException()
                }
                article.append("<p class=\"dinkus\">* * *</p>")
                return@forEach
            }
            val lines = paragraph.trim().split('\n').toMutableList()
            val iterate = lines.listIterator()
            while (iterate.hasNext()) {
                val oldValue = iterate.next()
                val newValue = processLine(oldValue)
                if (newValue != null) {
                    iterate.set(newValue)
                }
            }
            article.append("<p>")
            article.append(lines.joinToString("\n        <br/>"))
            article.append("</p>")
        }
        val titledTemplate = pageTemplate.replace("<!-- TITLE -->", title.toString())
        htmlFile.writeText(titledTemplate.replace("<!-- DATA -->", article.toString()))
    }
}