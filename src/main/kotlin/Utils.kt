import java.nio.file.Path
import java.nio.file.Paths

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
}