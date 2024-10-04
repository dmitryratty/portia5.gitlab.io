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
    override val relativeUrl: String
    override val absoluteUrl: String
    override val dstRelativePath: Path = if (srcRelativePathString.endsWith(".txt")) {
        if (Path.of(srcAbsolutePath.pathString.removeSuffix(".txt")).exists()) {
            Path.of("${srcRelativePathString.removeSuffix(".txt")}/index.html")
        } else {
            Path.of("${srcRelativePathString.removeSuffix("txt")}html")
        }
    } else {
        srcRelativePath
    }
    override val dstRelativePathString = dstRelativePath.pathString
    override val dstAbsolutePath: Path = dstDirPath.resolve(dstRelativePath)
    override val redirects: Set<String>
    override val isPage: Boolean
    override val isDirectory: Boolean
    override val isRoot: Boolean
    override val isRaw = !srcRelativePathString.endsWith(".txt")
    override val isMap: Boolean
    override val isGen: Boolean

    init {
        if (dstRelativePathString == "index.html") {
            relativeUrl = "/"
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
            || dstRelativePathString.endsWith(".webmanifest")) {
            relativeUrl = "/$dstRelativePathString"
            redirects = emptySet()
            isPage = false
            isDirectory = false
        } else {
            throw IllegalStateException(srcAbsolutePathString)
        }
        isRoot = relativeUrl == UtilsRelative.ROOT_RELATIVE_URL
        absoluteUrl = if (isRoot) HOST_NAME else "$HOST_NAME$relativeUrl"
        isMap = relativeUrl == UtilsRelative.MAP_RELATIVE_URL
        isGen = srcAbsolutePathString.startsWith(UtilsAbsolute.srcGenDir.pathString)
    }
}