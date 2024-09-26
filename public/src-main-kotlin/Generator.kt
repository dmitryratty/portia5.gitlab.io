import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import kotlin.io.path.listDirectoryEntries

class Generator {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Generator().main()
        }
    }

    private fun cleanBuildDirs() {
        val persistentFiles = emptyList<Path>()
        Utils.dstDir.listDirectoryEntries().forEach {
            if (!persistentFiles.contains(it)) it.toFile().deleteRecursively()
        }
        Utils.srcGeneratedDir.toFile().deleteRecursively()
        Utils.srcGeneratedDir.toFile().mkdir()
    }

    private fun copyRawRes() {
        val srcDir = Utils.srcOtherDir
        Files.walk(srcDir).forEach { src: Path ->
            if (src == srcDir) return@forEach
            Files.copy(src, Utils.dstDir.resolve(srcDir.relativize(src)), REPLACE_EXISTING)
        }
    }

    private fun main() {
        cleanBuildDirs()
        Favicon().main()
        Library().main()
        val sitemap = Sitemap(setOf(Utils.srcPagesDir, Utils.srcOtherDir), Utils.dstDir)
        sitemap.main()
        val pages = sitemap.urls.filter { !it.isRaw }.associate { it.relativeUrl to Page(it) }
        HtmlTransform().main(pages)
        copyRawRes()
    }
}