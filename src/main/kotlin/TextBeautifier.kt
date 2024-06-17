class TextBeautifier {

    private val typewriterApostrophes = true
    private val breakLevelOne = "</>"
    private val breakLevelOneBeautified = "<•>"

    fun transformWord(name: String, word: String): String {
        if (word == breakLevelOne) {
            return breakLevelOneBeautified
        } else if (!typewriterApostrophes) {
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

    fun transformLine(name: String, line: String): String {
        var newLine = line
        if (line.startsWith("- ")) {
            newLine = newLine.replaceFirst("- ", "— ")
        }
        newLine = newLine.replace("...", "…").replace(" - ", " — ")
        return Utils().transformLine(name, newLine, ::transformWord)
    }

    fun transformParagraph(name: String, paragraph: String): String {
        val result = StringBuilder()
        Utils().splitParagraphToLines(paragraph).forEach { line ->
            if (result.isNotEmpty()) {
                result.append("\n")
            }
            result.append(transformLine(name, line))
        }
        return result.toString()
    }

    fun transform(name: String, text: String): String {
        val result = StringBuilder()
        Utils().splitToParagraphs(text).forEach { paragraph ->
            if (result.isNotEmpty()) {
                result.append("\n\n")
            }
            result.append(transformParagraph(name, paragraph))
        }
        return result.toString()
    }
}