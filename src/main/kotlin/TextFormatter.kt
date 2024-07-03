class TextFormatter {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            TextFormatter().main()
        }
    }

    fun main() {
        Utils().textPagesInput().forEach {
            val file = it.value
            file.writeText(transform(file.readText()))
        }
    }

    fun transformLine(line: String): String {
        if (line.trim().contains("  ")) {
            // Detect multiple spaces in the middle of the line, it's usually a typos.
            throw IllegalStateException("Double space in line: [$line]")
        }
        return line.trimEnd()
            .replace("…", "...")
            .replace("’", "'")
            .replace("ʼ", "'")
            .replace("“", "\"")
            .replace("”", "\"")
    }

    fun transformParagraph(paragraph: String): String {
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