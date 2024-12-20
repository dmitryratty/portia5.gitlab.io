
import UtilsAbsolute.srcRawDir
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class GalleryGrid {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            GalleryGrid().main()
        }
    }

    @Serializable
    data class ImageInfo(val name: String, val w: Int, val h: Int)

    fun main() {
        val imagesInfosFile = srcRawDir.resolve("image/gallery/infos.json").toFile()
        val imagesInfos = Json.decodeFromString<List<ImageInfo>>(imagesInfosFile.readText())
        val builder = StringBuilder()
        imagesInfos.forEach {
            if (builder.isNotEmpty()) builder.appendLine()
            val imgElem = """<img src="/image/gallery/${it.name}" alt="${it.name}"/>"""
            val w = it.w.toDouble()
            val h = it.h.toDouble()
            val iElem = """<i style="padding-bottom:${h/w*100}%"></i>"""
            val divElem = """<div style="flex-grow:${w*100/h};flex-basis:${w*240/h}px;">$iElem$imgElem</div>"""
            builder.append(divElem)
        }
        val template = srcRawDir.resolve("test/image/gallery-template.html").toFile()
        srcRawDir.resolve("test/image/test4.html").toFile().writeText(
            template.readText().replace("<!-- IMAGES -->", builder.toString()))
    }
}