import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

class Generator {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Generator().main()
        }
    }

    private fun cleanBuildDirs() {
        Utils.cleanupBuildDir()
        Utils.srcPagesGeneratedDir.toFile().deleteRecursively()
        Utils.srcPagesGeneratedDir.toFile().mkdir()
    }

    private fun copyRawRes() {
        val srcDir = Utils.srcOtherDir
        Files.walk(srcDir).forEach { src: Path ->
            if (src == srcDir) return@forEach
            Files.copy(src, Utils.buildOutDir.resolve(srcDir.relativize(src)), REPLACE_EXISTING)
        }
    }

    private fun main() {
        cleanBuildDirs()
        Favicon().main()
        Library().main()
        val sitemap = Sitemap()
        sitemap.main()
        TextFormatter().main()
        HtmlTransform().main(sitemap.urls)
        copyRawRes()
    }
}