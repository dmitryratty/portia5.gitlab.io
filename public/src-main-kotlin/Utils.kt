
import java.nio.file.Path

object Utils {
    const val HOST_NAME = "https://dmitryratty.gitlab.io"

    val currentPath: Path = Path.of(System.getProperty("user.dir")).normalize().toRealPath()

    val projectDir: Path = if (currentPath.endsWith("public"))
        currentPath else currentPath.resolve("public")

    val srcPagesDir: Path = projectDir.resolve("src-main-pages")
    val srcGenDir: Path = projectDir.resolve("src-main-gen")
    val srcOtherDir: Path = projectDir.resolve("src-main-other")
    val srcResDir: Path = projectDir.resolve("src-main-res")

    val testResDir: Path = projectDir.resolve("src-test-res")
    val testGenDir: Path = projectDir.resolve("src-test-res/gen")

    val dstDir: Path = projectDir.resolve("site")

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