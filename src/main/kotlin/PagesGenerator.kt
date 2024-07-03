import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.pathString
import kotlin.text.StringBuilder

class PagesGenerator(
    /**
     * https://stackoverflow.com/questions/1946426/html-5-is-it-br-br-or-br
     */
    val xhmtlCompatibleVoidElements: Boolean = false
) {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            PagesGenerator().main()
        }
    }

    private val wbrElement = if (xhmtlCompatibleVoidElements) "<wbr/>" else "<wbr>"
    private val brElement = if (xhmtlCompatibleVoidElements) "<br/>" else "<br>"
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
    private val bottomNavigationHtml = "<p class=\"dinkus\">* * *</p>" +
            "<p><a href=\"https://dmitryratty.gitlab.io\">В начало</a>.</p>"
    private val resourcesDir = Utils().resourcesDir
    private val htmlTemplate get() = resourcesDir.resolve("page-template.html").toFile().readText()
    private val lineTransformer = LineTransformer(true, LineTransformer().simpleSpacesTransformer)

    fun main() {
        Library().main()
        TextFormatter().main()
        Utils().textPagesInput().forEach {
            val beautyfiedText = TextBeautifier().transform(it.value.readText())
            val titleAndBody = titleAndBody(beautyfiedText)
            val bodyHtml = textToHtml(titleAndBody.second)
            val htmlFile = Utils().textPageInputToHtmlOutputFile(it.key)
            htmlFile.parentFile.mkdirs()
            val bottomNavigation = it.key.pathString != "index.txt"
            htmlFile.writeText(htmlPage(titleAndBody.first, bodyHtml, bottomNavigation))
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

    private fun makeNoWrap(word: String): String {
        return "<span class=\"nowrap\">$word</span>"
    }

    private fun longUrlLineBreaks(url: String): String {
        // https://css-tricks.com/better-line-breaks-for-long-urls/
        val newUrl = url.split("//").joinToString("//<wbr>") { part ->
            // Insert a word break opportunity after a colon
            part.replace(wbrAfter, "\$1<wbr>")
                // Before a single slash, tilde, period, comma, hyphen, underline,
                // question mark, number sign, or percent symbol.
                .replace(wbrBefore, "<wbr>\$1")
                // Before and after an equals sign or ampersand
                .replace(wbrBeforeAfter, "<wbr>\$1<wbr>")
        }
        return if (xhmtlCompatibleVoidElements) {
            newUrl.replace("<wbr>", "<wbr/>")
        } else {
            newUrl
        }
    }

    @Suppress("RegExpSimplifiable")
    private val longWordLineBreaks = "((.{$maxUnwrappedWordLenght})|(.+))".toRegex()

    fun longWordLineBreaks(word: String): String {
        return longWordLineBreaks.findAll(word).map { it.value }.joinToString(wbrElement)
    }

    fun transformWord(word: String): String {
        if (Utils().isHyperlink(word)) {
            return "<a href=\"$word\">${longUrlLineBreaks(word)}</a>"
        }
        // Replace "…" with html entity?
        var newWord = word.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
            //.replace("\"", "&quot;").replace("'", "&apos;")
        if (newWord.contains('-')) {
            newWord = makeNoWrap(newWord)
        } else if (newWord.contains('[') && footnote.matches(newWord)) {
            // Footnote inside text, like "Hello, world![2]" and
            // we make "world![2]" no wrap.
            newWord = makeNoWrap(newWord)
        } else if (newWord.length > maxUnwrappedWordLenght) {
            newWord = longWordLineBreaks(newWord)
        }
        return newWord
    }

    fun transformLine(line: String): String {
        return lineTransformer.transform(line, ::transformWord)
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
        result.append("<p>")
        val lines = Utils().splitParagraphToLines(paragraph)
        result.append(lines.joinToString("\n        $brElement", transform = ::transformLine))
        result.append("</p>")
        return result.toString()
    }

    fun textToHtml(text: String): String {
        val article = StringBuilder()
        Utils().splitToParagraphs(text).forEach { paragraph ->
            if (article.isNotEmpty()) {
                article.append("\n\n    ")
            }
            article.append(transformParagraph(paragraph))
        }
        return article.toString()
    }
}