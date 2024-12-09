package archive.image

import image.Utils
import rattyExec
import java.io.File
import java.io.FileFilter
import java.nio.file.Files

class TiffConverter {
    fun main(dir: File) {
        val archive = dir.resolve("Sources")
        if (!archive.exists()) {
            check(archive.mkdir())
        }
        val filter = FileFilter{ file -> file.extension == "tif" }
        val images = Utils.listFilesNaturalOrder(dir, filter)
        images.forEach { convertTiff(dir, archive, it) }
    }

    private fun convertTiff(dir: File, archiveDir: File, src: File) {
        val dst = dir.resolve("${src.nameWithoutExtension}.png")
        rattyExec(
            "convert",
            "-define",
            "tiff:ignore-layers=true",
            src.absolutePath,
            dst.absolutePath
        )
        check(dst.exists())
        Files.move(src.toPath(), archiveDir.resolve(src.name).toPath())
    }
}