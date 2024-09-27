import Utils.HOST_NAME
import java.nio.file.Path
import kotlin.io.path.pathString

data class RatUrl(val srcAbsolutePath: Path, val srcRelativePath: Path, val dstDirPath: Path) {
    val srcAbsolutePathString = srcAbsolutePath.pathString
    val srcRelativePathString = srcRelativePath.pathString
    val relativeUrl: String
    val absoluteUrl: String
    val dstRelativePath: Path = if (srcRelativePathString.endsWith(".txt")) {
        Path.of("${srcRelativePathString.removeSuffix("txt")}html")
    } else {
        srcRelativePath
    }
    val dstRelativePathString = dstRelativePath.pathString
    val dstAbsolutePath: Path = dstDirPath.resolve(dstRelativePath)
    val redirects: Set<String>
    val isPage: Boolean
    val isDirectory: Boolean
    val isRoot: Boolean
    val isRaw = !srcRelativePathString.endsWith(".txt")
    val isMap: Boolean
    val isGen: Boolean

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
        isRoot = relativeUrl == RelativeUtils.ROOT_RELATIVE_URL
        absoluteUrl = if (isRoot) HOST_NAME else "$HOST_NAME$relativeUrl"
        isMap = relativeUrl == RelativeUtils.MAP_RELATIVE_URL
        isGen = srcAbsolutePathString.startsWith(Utils.srcGenDir.pathString)
    }
}