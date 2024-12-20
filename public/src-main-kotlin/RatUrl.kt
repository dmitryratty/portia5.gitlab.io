
import UtilsAbsolute.HOST_NAME
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.pathString

data class RatUrl(
    override val srcAbsolutePath: Path,
    override val srcRelativePath: Path,
    override val dstDirPath: Path) : RatUrlInterface {

    override val srcAbsolutePathString = srcAbsolutePath.pathString
    override val srcRelativePathString = srcRelativePath.pathString
    override val dstRelativePath: Path = if (srcRelativePathString.endsWith(".txt")) {
        Path.of("${srcRelativePathString.removeSuffix("txt")}html")
    } else {
        srcRelativePath
    }

    override val dstRelativePathString = dstRelativePath.pathString
    override val dstAbsolutePath: Path = dstDirPath.resolve(dstRelativePath)
    override val isRoot = dstRelativePathString == "index.html"
    override val isRaw = !srcRelativePathString.endsWith(".txt")
    override val isGen = srcAbsolutePathString.startsWith(UtilsAbsolute.srcGenDir.pathString)
    override val isIndexOfDirectory: Boolean

    override val relativeUrl: String
    override val absoluteUrl: String
    override val redirects: Set<String>
    override val isPage: Boolean
    override val isDirectory: Boolean
    override val isMap: Boolean

    init {
        if (isRoot) {
            relativeUrl = UtilsRelative.ROOT_RELATIVE_URL
            redirects = setOf("/index.html")
            isPage = true
            isDirectory = true
        } else if (dstRelativePathString.endsWith("/index.html")) {
            relativeUrl = "/" + dstRelativePathString.removeSuffix("/index.html")
            redirects = setOf("/$dstRelativePathString", "$relativeUrl/")
            isPage = true
            isDirectory = true
        } else if (dstRelativePathString.endsWith(".html")) {
            relativeUrl = "/" + dstRelativePathString.removeSuffix(".html")
            redirects = setOf("/$dstRelativePathString")
            isPage = true
            isDirectory = false
        } else if (dstRelativePathString.endsWith(".css")
            || dstRelativePathString.endsWith(".png")
            || dstRelativePathString.endsWith(".svg")
            || dstRelativePathString.endsWith(".ico")
            || dstRelativePathString.endsWith(".json")
            || dstRelativePathString.endsWith(".webmanifest")
            || dstRelativePathString == "_redirects") {
            relativeUrl = "/$dstRelativePathString"
            redirects = emptySet()
            isPage = false
            isDirectory = false
        } else {
            throw IllegalStateException(srcAbsolutePathString)
        }
        absoluteUrl = if (isRoot) HOST_NAME else "$HOST_NAME$relativeUrl"
        isMap = relativeUrl == UtilsRelative.MAP_RELATIVE_URL
        isIndexOfDirectory = if (isPage && isRaw) {
            Path.of(srcAbsolutePathString.removeSuffix(".html")).exists()
        } else if (isPage) {
            Path.of(srcAbsolutePathString.removeSuffix(".txt")).exists()
        } else {
            false
        }
    }
}