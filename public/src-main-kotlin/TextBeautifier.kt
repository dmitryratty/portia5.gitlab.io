class TextBeautifier {

    private val typewriterApostrophes = true
    private val breakLevelOne = "</>"
    private val breakLevelOneBeautified = "<•>"
    private val dataStart = "<<"
    private val dataEnd = ">>"
    private val lineTransform = LineTransform()

    fun transformWord(url: RatUrl, word: String): String {
        if (word == breakLevelOne) return breakLevelOneBeautified
        // 《》 ⟨⟩ ❝❞
        if (word == dataStart) return "❝"
        if (word == dataEnd) return "❞"
        if (!typewriterApostrophes) {
            if (!Utils.isHyperlink(word)) {
                // ''''' - Wikipedia typewriter apostrophe.
                // ’’’’’ - Substack curly apostrophe.
                // ’’’’’ (U+2019 RIGHT SINGLE QUOTATION MARK)
                // ʼʼʼʼʼ (U+02BC MODIFIER LETTER APOSTROPHE)
                return word.replace("'", "’")
            }
        }
        return word
    }

    fun transformLine(url: RatUrl, line: String): String {
        var newLine = line
        if (line.startsWith("- ")) {
            newLine = newLine.replaceFirst("- ", "— ")
        }
        newLine = newLine.replace("...", "…").replace(" - ", " — ")
        return lineTransform.transform(url, newLine, ::transformWord)
    }

    val shortSeparator = IncludeTransform().abstractSeparatorTemp
    val beautifiedShortSeparator = "⁂ ⁂ ⁂"

    fun transformParagraph(url: RatUrl, paragraph: String): String {
        if (paragraph == shortSeparator) return beautifiedShortSeparator
        if (paragraph == "...") return "•••"
        val result = StringBuilder()
        Utils.splitParagraphToLines(paragraph).forEach { line ->
            if (result.isNotEmpty()) {
                result.append("\n")
            }
            result.append(transformLine(url, line))
        }
        return result.toString()
    }

    fun transform(url: RatUrl, text: String): String {
        val result = StringBuilder()
        Utils.splitToParagraphs(text).forEach { paragraph ->
            if (result.isNotEmpty()) {
                result.append("\n\n")
            }
            result.append(transformParagraph(url, paragraph))
        }
        return result.toString()
    }
}