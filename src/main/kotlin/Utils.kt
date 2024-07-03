import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.pathString
import kotlin.io.path.relativeTo

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

    fun textPagesInput(): Map<Path, File> {
        return pagesDir.toFile().walk().filter { it.name.endsWith(".txt") }
            .map { it.toPath().relativeTo(pagesDir) to it }.toMap()
    }

    fun splitToParagraphs(text: String): List<String> {
        return text.split("\n\n")
    }

    fun splitParagraphToLines(paragraph: String): List<String> {
        return paragraph.split('\n')
    }

    fun isHyperlink(word: String): Boolean {
        // if (word.contains(" ")) throw IllegalStateException()
        return word.startsWith("http")
    }

    fun textPageInputToHtmlOutputFile(path: Path): File {
        val pathWithoutExtension = path.pathString.substring(0, path.pathString.length - 3)
        return projectDir.resolve("public/${pathWithoutExtension}html").toFile()
    }
}