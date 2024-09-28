
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

class Generator(c: ContextInterface = Context()) : ContextInterface by c {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Generator().main()
        }
    }

    private val includeTransform = IncludeTransform(this)
    private val htmlTransform = HtmlTransform()
    val sitemap = Sitemap(c)
    var firstRun = true

    private fun cleanDstDirs() {
        dstMainDir.toFile().deleteRecursively()
        dstMainDir.toFile().mkdir()
        srcGenDir.toFile().deleteRecursively()
        srcGenDir.toFile().mkdir()
    }

    private fun copyRawRes() {
        Files.walk(srcRawDir).forEach { srcRaw: Path ->
            if (srcRaw == srcRawDir) return@forEach
            Files.copy(srcRaw, dstMainDir.resolve(srcRawDir.relativize(srcRaw)), REPLACE_EXISTING)
        }
    }

    private fun processPage(page: Page) {
        page.srcAbsolutePath.toFile().writeText(page.formatted)
        includeTransform.transform(page)
        page.beautyText = TextBeautifier().transform(page.url, page.includeText)
        val bodyHtml = htmlTransform.textToHtml(page.url, page.beautyText)
        val htmlFile = page.htmlOutFile.toFile()
        htmlFile.parentFile.mkdirs()
        htmlFile.writeText(htmlTransform.htmlPage(page.title, bodyHtml, page.navigation))
    }

    private fun main() {
        cleanDstDirs()
        Favicon().main()
        Library().main()
        sitemap.updateUrls()
        sitemap.pages.forEach { processPage(it.value) }
        firstRun = false
        sitemap.updateMaps(htmlTransform.mapOfLinks)
        processPage(sitemap.getMapOrder())
        processPage(sitemap.getMapChaos())
        processPage(sitemap.getMap())
        dstTestDir.resolve("links-list.txt").toFile()
            .writeText(htmlTransform.setOfLinks.joinToString("\n"))
        dstTestDir.resolve("long-words-list.txt").toFile()
            .writeText(htmlTransform.setOfLongWords.joinToString("\n"))
        copyRawRes()
    }
}