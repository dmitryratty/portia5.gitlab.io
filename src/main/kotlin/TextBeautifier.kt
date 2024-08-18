class TextBeautifier {

    private val typewriterApostrophes = true
    private val breakLevelOne = "</>"
    private val breakLevelOneBeautified = "<•>"
    private val dataStart = "<<"
    private val dataEnd = ">>"
    private val lineTransformer = LineTransformer()

    fun transformWord(word: String): String {
        if (word == breakLevelOne) return breakLevelOneBeautified
        // 《》 ⟨⟩ ❝❞
        if (word == dataStart) return "❝"
        if (word == dataEnd) return "❞"
        if (!typewriterApostrophes) {
            if (!Utils().isHyperlink(word)) {
                // ''''' - Wikipedia typewriter apostrophe.
                // ’’’’’ - Substack curly apostrophe.
                // ’’’’’ (U+2019 RIGHT SINGLE QUOTATION MARK)
                // ʼʼʼʼʼ (U+02BC MODIFIER LETTER APOSTROPHE)
                return word.replace("'", "’")
            }
        }
        return word
    }

    fun transformLine(line: String): String {
        var newLine = line
        if (line.startsWith("- ")) {
            newLine = newLine.replaceFirst("- ", "— ")
        }
        newLine = newLine.replace("...", "…").replace(" - ", " — ")
        return lineTransformer.transform(newLine, ::transformWord)
    }

    val shortSeparator = IncludeTransformer().shortSeparator
    val beautifiedShortSeparator = "⁂ ⁂ ⁂"

    fun transformParagraph(paragraph: String): String {
        if (paragraph == shortSeparator) {
            return beautifiedShortSeparator
        }
        val result = StringBuilder()
        Utils().splitParagraphToLines(paragraph).forEach { line ->
            if (result.isNotEmpty()) {
                result.append("\n")
            }
            result.append(transformLine(line))
        }
        return result.toString()
    }

    fun transform(text: String): String {
        val result = StringBuilder()
        Utils().splitToParagraphs(text).forEach { paragraph ->
            if (result.isNotEmpty()) {
                result.append("\n\n")
            }
            result.append(transformParagraph(paragraph))
        }
        return result.toString()
    }
}