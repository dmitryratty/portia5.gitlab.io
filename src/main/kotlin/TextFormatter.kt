class TextFormatter {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            TextFormatter().main()
        }
    }

    fun main() {
        Utils().textPagesPaths().forEach {
            val file = it.toFile()
            file.writeText(textFormatting(file.readText()))
        }
    }

    fun textFormatting(text: String): String {
        // Replace "…" to "...".
        // Replace ’ with ', “” with ".
        // Replace multiple spaces in the middle of the line with single space.
        // Remove trailing spaces.
        Utils().splitToParagraphs(text).forEach {

        }
        return text
    }
}