
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

class Generator {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Generator().main()
        }
    }

    private fun main() {
        Utils().cleanupBuildDir()
        PagesGenerator().main()
        val srcDir = Utils().resourcesDir.resolve("other")
        val dstDir = Utils().buildOutDir
        Files.walk(srcDir).forEach { src: Path ->
            if (src == srcDir) return@forEach
            Files.copy(src, dstDir.resolve(srcDir.relativize(src)),
                StandardCopyOption.REPLACE_EXISTING)
        }
    }
}