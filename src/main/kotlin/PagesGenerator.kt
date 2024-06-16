import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.nameWithoutExtension
import kotlin.text.StringBuilder

class PagesGenerator {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            PagesGenerator().main()
        }
    }

    private val maxUnwrappedWordLenght = 30
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
    private val bottomNavigationHtml = "<p class=\"dinkus\">* * *</p>" +
            "<p><a href=\"https://dmitryratty.gitlab.io\">В начало</a>.</p>"
    private val resourcesDir = Utils().resourcesDir
    private val htmlTemplate get() = resourcesDir.resolve("page-template.html").toFile().readText()

    fun main() {
        Library().main()
        val projectDir = Utils().projectDir
        Utils().pagesDir.listDirectoryEntries("*.txt").forEach {
            val pageName = it.nameWithoutExtension
            val beautyfiedText = beautifyText(it.toFile().readText())
            val titleAndBody = titleAndBody(beautyfiedText)
            val bodyHtml = textToHtml(titleAndBody.second)
            val htmlFile = projectDir.resolve("public/$pageName.html").toFile()
            htmlFile.writeText(htmlPage(titleAndBody.first, bodyHtml, pageName != "index"))
        }
    }

    fun htmlPage(title: String, body: String, bottomNavigation: Boolean): String {
        return htmlTemplate
            .replace("<!-- TITLE -->", title)
            .replace("<!-- DATA -->", body)
            .replace("<!-- DATA FOOTER -->", if (bottomNavigation) bottomNavigationHtml else "")
    }

    fun titleAndBody(beautyfiedText: String): Pair<String, String> {
        val titleAndBody = beautyfiedText.split("\n\n", limit = 2)
        return Pair(titleAndBody[0], titleAndBody[1])
    }

    fun beautifyText(raw: String): String {
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

    @Suppress("RegExpSimplifiable")
    private val longWordLineBreaks = "((.{$maxUnwrappedWordLenght})|(.+))".toRegex()

    fun longWordLineBreaks(word: String): String {
        return longWordLineBreaks.findAll(word).map { it.value }.joinToString("<wbr>")
    }

    fun transformWord(word: String): String {
        return if (word.length == 1) {
            word
        } else if (word.startsWith("http")) {
            "<a href=\"$word\">${longUrlLineBreaks(word)}</a>"
        } else if (word.contains('-')) {
            makeNoWrap(word)
        } else if (word.contains('[') && footnote.matches(word)) {
            // Footnote inside text, like "Hello, world![2]" and
            // we make "world![2]" no wrap.
            makeNoWrap(word)
        } else if (word == breakLevelOne) {
            makeNoWrap("&lt;•&gt;")
        } else if (word.length > maxUnwrappedWordLenght) {
            longWordLineBreaks(word)
        } else {
            word
        }
    }

    fun transformLine(line: String): String {
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
            builder.append(transformWord(word))
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
            iterate.set(newValue)
            if (newValue.contains("  ")) {
                throw IllegalStateException("Double space: [$newValue]")
            }
        }
        result.append("<p>")
        result.append(lines.joinToString("\n        <br/>"))
        result.append("</p>")
        return result.toString()
    }

    fun textToHtml(txtString: String): String {
        val article = StringBuilder()
        val paragraphs = txtString.split("\n\n")
        paragraphs.forEach { paragraph ->
            if (article.isNotEmpty()) {
                article.append("\n\n    ")
            }
            article.append(transformParagraph(paragraph))
        }
        return article.toString()
    }
}