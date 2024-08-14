import java.io.File
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.pathString
import kotlin.io.path.relativeTo

class Utils {

    val currentPath: Path = Path.of(System.getProperty("user.dir")).normalize().toRealPath()

    val projectDir: Path
        get() {
            if (currentPath.endsWith("ratty-public")) {
                return currentPath
            }
            return currentPath.resolve("ratty-public")
        }

    val resourcesDir: Path get() = projectDir.resolve("src/main/resources")

    val testResourcesDir: Path get() = projectDir.resolve("src/test/resources")
    val testAutogeneratedDir: Path get() = projectDir.resolve("src/test/resources/autogenerated")

    val pagesSrcDir: Path get() = resourcesDir.resolve("pages")

    val buildOutDir: Path get() = projectDir.resolve("public")

    fun textPagesInput(): Map<Path, File> {
        return pagesSrcDir.toFile().walk().filter { it.name.endsWith(".txt") }
            .map { it.toPath().relativeTo(pagesSrcDir) to it }.toMap()
    }

    fun splitToParagraphs(text: String): List<String> {
        return text.split("\n\n")
    }

    fun cleanupBuildDir() {
        val persistentFiles = emptyList<Path>()
        buildOutDir.listDirectoryEntries().forEach {
            if (!persistentFiles.contains(it)) it.toFile().deleteRecursively()
        }
    }

    fun splitParagraphToLines(paragraph: String): List<String> {
        return paragraph.split('\n')
    }

    fun isHyperlink(word: String): Boolean {
        // if (word.contains(" ")) throw IllegalStateException()
        return word.startsWith("http://") || word.startsWith("https://")
    }

    fun textPageInputToHtmlOutputFile(path: String): File {
        val pathWithoutExtension = path.substring(0, path.length - "txt".length)
        return buildOutDir.resolve("${pathWithoutExtension}html").toFile()
    }
}