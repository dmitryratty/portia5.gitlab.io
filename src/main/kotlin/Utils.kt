import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.listDirectoryEntries

class Utils {

    val projectDir: Path
        get() {
            val current = Paths.get("").toAbsolutePath()
            if (current.endsWith("ratty-public")) {
                return current
            }
            return current.resolve("ratty-public")
        }

    val resourcesDir get() = projectDir.resolve("src/main/resources")

    val pagesDir get() = resourcesDir.resolve("pages")

    fun textPagesPaths(): List<Path> {
        return pagesDir.listDirectoryEntries("*.txt")
    }

    fun splitToParagraphs(text: String): List<String> {
        return text.split("\n\n")
    }

    fun splitParagraphToLines(paragraph: String): List<String> {
        return paragraph.split('\n')
    }

    fun transformLine(
        tag: String, line: String,
        wordTransformer: (tag: String, line: String) -> String
    ): String {
        val builder = StringBuilder()
        var processingLeadingSpaces = true
        line.split(' ').forEach { word ->
            if (builder.isNotEmpty() && !processingLeadingSpaces) {
                builder.append(' ')
            }
            if (word.isNotBlank()) {
                processingLeadingSpaces = false
            }
            if (word.isEmpty()) {
                builder.append(' ')
                return@forEach
            }
            builder.append(wordTransformer(tag, word))
        }
        return builder.toString()
    }

    fun isHyperlink(word: String): Boolean {
        // if (word.contains(" ")) throw IllegalStateException()
        return word.startsWith("http")
    }
}