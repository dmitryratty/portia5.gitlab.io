import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.getLastModifiedTime

/**
 * https://evilmartians.com/chronicles/how-to-favicon-in-2021-six-files-that-fit-most-needs
 * - Path "/favicon.ico" may be expected by clients.
 */
class Favicon {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Favicon().main()
        }
    }

    @Serializable
    data class BuildInfo(val lastModifiedSvg: Long, val lastModifiedKt: Long)

    val ktSrc = Utils.projectDir.resolve("src/main/kotlin/Favicon.kt")
    val otherPath = Utils.resourcesDir.resolve("other")
    val svgSrc = otherPath.resolve("favicon.svg")
    val jsonFile = Utils.resourcesDir.resolve("favicon.json").toFile()

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

    private fun generationRequired(): Boolean {
        if (!jsonFile.exists()) return true
        val buildInfo = Json.decodeFromString<BuildInfo>(jsonFile.readText())
        if (svgSrc.getLastModifiedTime().toMillis() > buildInfo.lastModifiedSvg) return true
        if (ktSrc.getLastModifiedTime().toMillis() > buildInfo.lastModifiedKt) return true
        return false
    }

    private fun generationCompleted() {
        val format = Json { prettyPrint = true }
        val buildInfo = BuildInfo(
            svgSrc.getLastModifiedTime().toMillis(), ktSrc.getLastModifiedTime().toMillis()
        )
        jsonFile.writeText(format.encodeToString(buildInfo))
    }

    fun main() {
        if (!generationRequired()) return
        println("Generating favicon.")
        val tmpPng = otherPath.resolve("favicon-temp.png")
        val icoDst = otherPath.resolve("favicon.ico")
        svgToPng(svgSrc, tmpPng, 32)
        val output = rattyExec(
            "convert", tmpPng.absolutePathString(), icoDst.absolutePathString()
        )
        if (output.isNotEmpty()) {
            println(output)
        }
        tmpPng.toFile().delete()
        svgToPng(svgSrc, otherPath.resolve("icon-512.png"), 512)
        svgToPng(svgSrc, otherPath.resolve("icon-192.png"), 192)
        svgToPng(svgSrc, otherPath.resolve("apple-touch-icon.png"), 180)
        generationCompleted()
    }
}