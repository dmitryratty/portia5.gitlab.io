
import java.nio.file.Path

object UtilsAbsolute {
    const val HOST_NAME = "https://portia3.gitlab.io"

    val currentPath: Path = Path.of(System.getProperty("user.dir")).normalize().toRealPath()

    val projectDir: Path = if (currentPath.endsWith("public"))
        currentPath else currentPath.resolve("public")

    val srcTxtDir: Path = projectDir.resolve("src-main-txt")
    val srcRawDir: Path = projectDir.resolve("src-main-raw")
    val srcResDir: Path = projectDir.resolve("src-main-res")
    val srcGenDir: Path = projectDir.resolve("src-main-gen")
    val dstMainDir: Path = projectDir.resolve("site")

    val testResDir: Path = projectDir.resolve("src-test-res")
    val dstTestDir: Path = projectDir.resolve("src-test-res/gen")

    fun splitToParagraphs(text: String): MutableList<String> {
        return text.split("\n\n").toMutableList()
    }

    fun splitParagraphToLines(paragraph: String): List<String> {
        return paragraph.split('\n')
    }

    fun isHyperlink(word: String): Boolean {
        // if (word.contains(" ")) throw IllegalStateException()
        return word.startsWith("http://") || word.startsWith("https://")
    }

    fun isPrime(n: Long): Boolean {
        // Corner case.
        if (n <= 1) {
            return false
        }
        // Check from 2 to n - 1.
        for (i in 2 until n) {
            if (n % i == 0L) {
                return false
            }
        }
        return true
    }
}