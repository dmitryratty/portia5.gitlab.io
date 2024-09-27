
import Utils.HOST_NAME
import java.util.*

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
    private val htmlTemplate get() = Utils.srcResDir.resolve("page-template.html").toFile().readText()
    private val lineTransform = LineTransform(true, LineTransform().simpleSpacesTransformer)
    val setOfLinks = sortedSetOf<String>()
    val setOfLongWords = sortedSetOf<String>()
    val mapOfLinks = sortedMapOf<String, TreeSet<String>>()

    private val bottomNavigationHtml = "\n    <p class=\"dinkus\">* * *</p>" +
            "\n\n    <p>🏠 <a href=\"/\">$HOST_NAME</a></p>"

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

    private fun longWordLineBreaksTwo(word: String): String {
        // TODO "&shy;" vs wbrElement
        return longWordLineBreaks.findAll(word).map { it.value }.joinToString(wbrElement)
    }

    private fun longWordLineBreaks(word: String): String {
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

    private fun transformLink(url: RatUrl, link: String): String {
        setOfLinks.add(link)
        if (link.startsWith(HOST_NAME)) {
            var links = mapOfLinks[url.absoluteUrl]
            if (links == null) {
                links = sortedSetOf()
                mapOfLinks[url.absoluteUrl] = links
            }
            links.add(link)
        }
        val linkDisplay = if (link.length > maxUnwrappedWordLenght) {
            longUrlLineBreaks(link)
        } else {
            link
        }
        if (link.startsWith(HOST_NAME)) {
            val relativeLink = if (link == HOST_NAME) "/" else link.removePrefix(HOST_NAME)
            return "<a href=\"$relativeLink\">$linkDisplay</a>"
        } else {
            return "<a href=\"$link\">$linkDisplay</a>"
        }
    }

    fun isIdeographic(word: String): Boolean {
        // TODO Character.isIdeographic(int codepoint)
        return word.contains("東亜重工")
    }

    fun transformWord(url: RatUrl, word: String): String {
        if (Utils.isHyperlink(word)) {
            return transformLink(url, word)
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

    fun transformLine(url: RatUrl, line: String): String {
        return lineTransform.transform(url, line, ::transformWord)
    }

    val beautifiedShortSeparator = TextBeautifier().beautifiedShortSeparator

    fun transformParagraph(url: RatUrl, paragraph: String): String {
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
        val lines = Utils.splitParagraphToLines(paragraph).map { transformLine(url, it) }
        result.append(lines.joinToString("\n        $brElement"))
        result.append("</p>")
        return result.toString()
    }

    fun textToHtml(url: RatUrl, text: String): String {
        val article = StringBuilder()
        Utils.splitToParagraphs(text).forEach { paragraph ->
            if (article.isNotEmpty()) {
                article.append("\n\n    ")
            }
            try {
                article.append(transformParagraph(url, paragraph))
            } catch (e: Exception) {
                throw IllegalStateException(url.srcRelativePathString, e)
            }
        }
        return article.toString()
    }
}