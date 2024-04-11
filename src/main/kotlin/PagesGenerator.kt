import java.nio.file.Path
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
    private val lineStartDash = "^(- )".toRegex(RegexOption.MULTILINE)
    private val breakLevelOne = "</>"

    private val projectDir: Path
        get() {
            val current = Paths.get("").toAbsolutePath()
            if (current.endsWith("ratty-public")) {
                return current
            }
            return current.resolve("ratty-public")
        }
    private val resourcesDir get() = projectDir.resolve("src/main/resources")
    private val htmlTemplate get() = resourcesDir.resolve("page-template.html").toFile().readText()

    fun main() {
        Library().main()
        listOf("game", "tech", "math", "library", "life").forEach {
            val txtFile = resourcesDir.resolve("$it.txt").toFile()
            val htmlFile = projectDir.resolve("public/$it.html").toFile()
            htmlFile.writeText(txtToHtml(txtBeatify(txtFile.readText())))
        }
    }

    fun txtBeatify(raw: String): String {
        // Replace "..." with html entity instead "…"?
        val result = raw.replace(lineStartDash, "— ")
        return result.replace("...", "…").replace(" - ", " — ")
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

    fun transformLine(line: String): String? {
        if (!line.contains("http") && !line.contains('-')
            && !line.contains('[') && !line.contains(breakLevelOne)
            && !line.contains('❖') && !line.startsWith(' ')) {
            return null
        }
        val builder = StringBuilder()
        var processingLeadingSpaces = true
        line.split(' ').forEach { word ->
            if (builder.isNotEmpty() && !processingLeadingSpaces) {
                builder.append(' ')
            }
            if (word.isNotBlank()) {
                processingLeadingSpaces = false
            }
            if (word.isEmpty()) {
                if (processingLeadingSpaces) {
                    // Leading spaces in string, for example for padding.
                    builder.append("&nbsp;")
                } else {
                    builder.append(' ')
                }
                return@forEach
            }
            if (word.length == 1) {
                builder.append(word)
            } else if (word.startsWith("http")) {
                builder.append("<a href=\"$word\">${longUrlLineBreaks(word)}</a>")
            } else if (word.contains('-')) {
                builder.append(makeNoWrap(word))
            } else if (word.contains('[') && footnote.matches(word)) {
                // Footnote inside text, like "Hello, world![2]" and
                // we make "world![2]" no wrap.
                builder.append(makeNoWrap(word))
            } else if (word == breakLevelOne) {
                builder.append(makeNoWrap("&lt;/&gt;"))
            } else {
                builder.append(word)
            }
        }
        return builder.toString()
    }

    fun transformParagraph(paragraph: String): String {
        val result = StringBuilder()
        if (paragraph.contains("* * *")) {
            if (paragraph != "* * *") {
                throw IllegalStateException()
            }
            result.append("<p class=\"dinkus\">* * *</p>")
            return result.toString()
        }
        val lines = paragraph.trim().split('\n').toMutableList()
        val iterate = lines.listIterator()
        while (iterate.hasNext()) {
            val oldValue = iterate.next()
            val newValue = transformLine(oldValue)
            if (newValue != null) {
                iterate.set(newValue)
            }
            val value = newValue ?: oldValue
            if (value.contains("  ")) {
                throw IllegalStateException("Double space: [$value]")
            }
        }
        result.append("<p>")
        result.append(lines.joinToString("\n        <br/>"))
        result.append("</p>")
        return result.toString()
    }

    fun txtToHtml(txtString: String): String {
        val title = StringBuilder()
        val article = StringBuilder()
        val paragraphs = txtString.split("\n\n")
        paragraphs.forEach { paragraph ->
            if (title.isEmpty()) {
                title.append(paragraph)
                return@forEach
            }
            if (article.isNotEmpty()) {
                article.append("\n\n    ")
            }
            article.append(transformParagraph(paragraph))
        }
        return htmlTemplate
            .replace("<!-- TITLE -->", title.toString())
            .replace("<!-- DATA -->", article.toString())
    }
}