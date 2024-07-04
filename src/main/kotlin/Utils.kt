import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.listDirectoryEntries
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

    val resourcesDir: Path get() = projectDir.resolve("src/main/resources")

    val pagesTextSrcDir: Path get() = resourcesDir.resolve("pages")

    val buildOutDir: Path get() = projectDir.resolve("public")

    fun textPagesInput(): Map<Path, File> {
        return pagesTextSrcDir.toFile().walk().filter { it.name.endsWith(".txt") }
            .map { it.toPath().relativeTo(pagesTextSrcDir) to it }.toMap()
    }

    fun splitToParagraphs(text: String): List<String> {
        return text.split("\n\n")
    }

    fun cleanupBuildDir() {
        val persistentFiles = listOf(buildOutDir.resolve("css"), buildOutDir.resolve("test"))
        buildOutDir.listDirectoryEntries().forEach {
            if (!persistentFiles.contains(it)) it.toFile().deleteRecursively()
        }
    }

    fun splitParagraphToLines(paragraph: String): List<String> {
        return paragraph.split('\n')
    }

    fun isHyperlink(word: String): Boolean {
        // if (word.contains(" ")) throw IllegalStateException()
        return word.startsWith("http")
    }

    fun textPageInputToHtmlOutputFile(path: Path): File {
        val pathString = path.pathString
        val pathWithoutExtension = pathString.substring(0, pathString.length - "txt".length)
        return buildOutDir.resolve("${pathWithoutExtension}html").toFile()
    }
}