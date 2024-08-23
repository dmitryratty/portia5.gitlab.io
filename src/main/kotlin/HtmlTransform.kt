
import Utils.hostName
import Utils.resourcesDir

class HtmlTransform(
    /**
     * https://stackoverflow.com/questions/1946426/html-5-is-it-br-br-or-br
     */
    val xhmtlCompatibleVoidElements: Boolean = false
) {

    private val wbrElement = if (xhmtlCompatibleVoidElements) "<wbr/>" else "<wbr>"
    private val brElement = if (xhmtlCompatibleVoidElements) "<br/>" else "<br>"
    private val maxUnwrappedWordLenght = 25
    // "<span class=\"nowrap\">$1</span>"
    private val dashNoWrap = "(\\S+-\\S+)".toRegex()
    // "<a href=\"$1\">\$1</a>"
    private val hyperlink = "(http\\S+)".toRegex()
    val footnote = "(\\S+\\[\\d+])".toRegex()
    private val wbrBefore = "([/~.,\\-_?#%])".toRegex()
    private val wbrAfter = "([:])".toRegex()
    private val wbrBeforeAfter = "([=&])".toRegex()
    private val htmlTemplate get() = resourcesDir.resolve("page-template.html").toFile().readText()
    private val lineTransform = LineTransform(true, LineTransform().simpleSpacesTransformer)
    private val setOfLinks = sortedSetOf<String>()
    private val setOfLongWords = sortedSetOf<String>()

    fun main(pages: Map<String, Page>) {
        val includeTransform = IncludeTransform()
        pages.forEach {
            val page = it.value
            page.url.srcAbsolutePath.toFile().writeText(page.formatted)
            includeTransform.transform(pages, page)
            page.beautyText = TextBeautifier().transform(page.includeText)
            val bodyHtml = textToHtml(page.url.srcRelativePathString, page.beautyText)
            val htmlFile = page.htmlOutFile.toFile()
            htmlFile.parentFile.mkdirs()
            htmlFile.writeText(htmlPage(page.title, bodyHtml, page.navigation))
        }
        Utils.testAutogeneratedDir.resolve("links-list.txt").toFile()
            .writeText(setOfLinks.joinToString("\n"))
        Utils.testAutogeneratedDir.resolve("long-words-list.txt").toFile()
            .writeText(setOfLongWords.joinToString("\n"))
    }

    private val bottomNavigationHtml = "\n    <p class=\"dinkus\">* * *</p>" +
            "\n\n    <p>🏠 <a href=\"/\">$hostName</a></p>"

    fun htmlPage(title: String, body: String, bottomNavigation: Boolean): String {
        return htmlTemplate
            .replace("<!--TITLE-->", title)
            .replace("<!--DATA-->", "    $body")
            .replace("<!--DATA-FOOTER-->", if (bottomNavigation) bottomNavigationHtml else "")
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

    fun longWordLineBreaksTwo(word: String): String {
        // TODO "&shy;" vs wbrElement
        return longWordLineBreaks.findAll(word).map { it.value }.joinToString(wbrElement)
    }

    fun longWordLineBreaks(word: String): String {
        val result = StringBuilder()
        word.split('-').forEach {
            if (result.isNotEmpty()) result.append('-')
            if (it.length > maxUnwrappedWordLenght) {
                setOfLongWords.add(word)
                result.append(longWordLineBreaksTwo(it))
            } else {
                result.append(it)
            }
        }
        return result.toString()
    }

    fun transformLink(link: String): String {
        setOfLinks.add(link)
        val linkDisplay = if (link.length > maxUnwrappedWordLenght) {
            longUrlLineBreaks(link)
        } else {
            link
        }
        if (link.startsWith(hostName)) {
            val relativeLink = if (link == hostName) "/" else link.removePrefix(hostName)
            return "<a href=\"$relativeLink\">$linkDisplay</a>"
        } else {
            return "<a href=\"$link\">$linkDisplay</a>"
        }
    }

    fun isIdeographic(word: String): Boolean {
        // TODO Character.isIdeographic(int codepoint)
        return word.contains("東亜重工")
    }

    fun transformWord(word: String): String {
        if (Utils.isHyperlink(word)) {
            return transformLink(word)
        }
        // Replace "…" with html entity?
        var newWord = word.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
            //.replace("\"", "&quot;").replace("'", "&apos;")
        if (newWord.length > maxUnwrappedWordLenght) {
            newWord = longWordLineBreaks(newWord)
        } else {
            if (newWord.contains('-')) {
                newWord = makeNoWrap(newWord)
            } else if (isIdeographic(newWord)) {
                newWord = makeNoWrap(newWord)
            } else if (footnote.matches(newWord)) {
                // Footnote inside text, like "world![2]". We make "world![2]"
                // no wrap, because by default "[2]" can be wrapped to the next line
                // from "world!".
                newWord = makeNoWrap(newWord)
            }
        }
        return newWord
    }

    fun transformLine(line: String): String {
        return lineTransform.transform(line, ::transformWord)
    }

    val beautifiedShortSeparator = TextBeautifier().beautifiedShortSeparator

    fun transformParagraph(paragraph: String): String {
        val result = StringBuilder()
        if (paragraph.contains("* * *")) {
            if (paragraph != "* * *") {
                throw IllegalStateException(paragraph)
            }
            result.append("<p class=\"dinkus\">* * *</p>")
            return result.toString()
        }
        if (paragraph == beautifiedShortSeparator) {
            result.append("<p class=\"dinkus\">$beautifiedShortSeparator</p>")
            return result.toString()
        }
        result.append("<p>")
        val lines = Utils.splitParagraphToLines(paragraph)
        result.append(lines.joinToString("\n        $brElement", transform = ::transformLine))
        result.append("</p>")
        return result.toString()
    }

    fun textToHtml(tag: String, text: String): String {
        val article = StringBuilder()
        Utils.splitToParagraphs(text).forEach { paragraph ->
            if (article.isNotEmpty()) {
                article.append("\n\n    ")
            }
            try {
                article.append(transformParagraph(paragraph))
            } catch (e: Exception) {
                throw IllegalStateException(tag, e)
            }
        }
        return article.toString()
    }
}