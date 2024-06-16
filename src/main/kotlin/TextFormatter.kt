class TextFormatter {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            TextFormatter().main()
        }
    }

    fun main() {
        Utils().textPagesPaths().forEach {
            val file = it.toFile()
            file.writeText(textFormatting(file.name, file.readText()))
        }
    }

    fun textFormatting(name: String, text: String): String {
        // Replace ’ with ', “” with ".
        // Remove trailing spaces.
        val result = StringBuilder()
        Utils().splitToParagraphs(text).forEach { paragraph ->
            val paragraphBuilder = StringBuilder()
            Utils().splitParagraphToLines(paragraph).forEach { line ->
                if (line.trim().contains("  ")) {
                    // Detect multiple spaces in the middle of the line, it's usually a typos.
                    throw IllegalStateException("Double space in [$name], line: [$line]")
                }
                if (paragraphBuilder.isNotEmpty()) {
                    paragraphBuilder.append("\n")
                }
                paragraphBuilder.append(line.trimEnd()
                    .replace("…", "..."))
            }
            if (result.isNotEmpty()) {
                result.append("\n\n")
            }
            result.append(paragraphBuilder)
        }
        return result.toString()
    }
}