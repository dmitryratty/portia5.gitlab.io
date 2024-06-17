class TextFormatter {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            TextFormatter().main()
        }
    }

    fun main() {
        Utils().textPagesPaths().forEach {
            val file = it.toFile()
            file.writeText(transform(file.name, file.readText()))
        }
    }

    fun transformLine(name: String, line: String): String {
        if (line.trim().contains("  ")) {
            // Detect multiple spaces in the middle of the line, it's usually a typos.
            throw IllegalStateException("Double space in [$name], line: [$line]")
        }
        return line.trimEnd()
            .replace("…", "...")
            .replace("’", "'")
            .replace("ʼ", "'")
            .replace("“", "\"")
            .replace("”", "\"")
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