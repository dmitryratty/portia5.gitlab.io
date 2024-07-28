import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.pathString

/**
 * https://evilmartians.com/chronicles/how-to-favicon-in-2021-six-files-that-fit-most-needs
 * - Path "/favicon.ico" may be expected by clients.
 */
class Favicon {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            Favicon().main()
        }
    }

    private fun svgToPng(src: Path, dst: Path, size: Int) {
        val output = rattyExec(
            "inkscape",
            "--export-type=png",
            "--export-width=$size",
            "--export-filename=${dst.absolutePathString()}",
            src.absolutePathString(),
        )
        if (output.isNotEmpty()) {
            println(output)
        }
        check(dst.toFile().exists())
    }

    fun main() {
        val projectPath = Utils().projectDir
        val otherPath = Utils().resourcesDir.resolve("other")
        val svgSrc = otherPath.resolve("favicon.svg")
        val tmpPng = otherPath.resolve("favicon-temp.png")
        val icoDst = otherPath.resolve("favicon.ico")
        svgToPng(svgSrc, tmpPng, 32)
        val output = rattyExec(
            "convert",
            tmpPng.absolutePathString(),
            icoDst.absolutePathString()
        )
        if (output.isNotEmpty()) {
            println(output)
        }
        tmpPng.toFile().delete()
        svgToPng(svgSrc, otherPath.resolve("icon-512.png"), 512)
        svgToPng(svgSrc, otherPath.resolve("icon-192.png"), 192)
        svgToPng(svgSrc, otherPath.resolve("apple-touch-icon.png"), 180)
    }
}