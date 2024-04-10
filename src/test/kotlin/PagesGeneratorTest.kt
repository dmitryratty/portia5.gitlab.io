import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.test.Test
import kotlin.test.assertEquals

class PagesGeneratorTest {

    private val pagesGenerator: PagesGenerator = PagesGenerator()

    @Test
    fun txtBeatify() {
        var lineIn = " - Start space-dash. And - dash in middle... And - another dash...\n" +
                "- Start dash. Hello!\n" +
                " \n" +
                "- Another start dash. And - dash..."
        var lineOut: String? = " — Start space-dash. And — dash in middle… And — another dash…\n" +
                "— Start dash. Hello!\n" +
                " \n" +
                "— Another start dash. And — dash…"
        assertEquals(lineOut, pagesGenerator.txtBeatify(lineIn))
    }

    @Test
    fun transformLines() {
        var lineIn = "\"The Map of Mathematics\" by Domain of Science:"
        var lineOut: String? = null
        assertEquals(lineOut, pagesGenerator.transformLine(lineIn))
        lineIn = " — https://youtu.be/OmJ-4B-mS-Y?si=bBWOSbdlpQ7kV9Bz"
        lineOut = "&nbsp;— <a href=\"https://youtu.be/OmJ-4B-mS-Y?si=bBWOSbdlpQ7kV9Bz\">https:<wbr>//<wbr>youtu<wbr>.be<wbr>/OmJ<wbr>-4B<wbr>-mS<wbr>-Y<wbr>?si<wbr>=<wbr>bBWOSbdlpQ7kV9Bz</a>"
        assertEquals(lineOut, pagesGenerator.transformLine(lineIn))
        lineIn = "    — https://youtu.be/OJ4B"
        lineOut = "&nbsp;&nbsp;&nbsp;&nbsp;— <a href=\"https://youtu.be/OJ4B\">https:<wbr>//<wbr>youtu<wbr>.be<wbr>/OJ4B</a>"
        assertEquals(lineOut, pagesGenerator.transformLine(lineIn))
    }

    @Test
    fun transformParagraphs() {
        var paragraphIn = "\"The Map of Mathematics\" by Domain of Science:\n" +
                " — https://youtu.be/OmJ-4B-mS-Y?si=bBWOSbdlpQ7kV9Bz"
        var paragraphOut = "<p>\"The Map of Mathematics\" by Domain of Science:\n" +
                "        <br/>&nbsp;— <a href=\"https://youtu.be/OmJ-4B-mS-Y?si=bBWOSbdlpQ7kV9Bz\">https:<wbr>//<wbr>youtu<wbr>.be<wbr>/OmJ<wbr>-4B<wbr>-mS<wbr>-Y<wbr>?si<wbr>=<wbr>bBWOSbdlpQ7kV9Bz</a></p>"
        assertEquals(paragraphOut, pagesGenerator.transformParagraph(paragraphIn))
    }

    @Test
    fun txtToHtml() {
        val resourcesDir = Paths.get("src/test/resources")
        assert(resourcesDir.exists())
        val txtString = resourcesDir.resolve("test1.txt").toFile().readText()
        val expectedHtmlString = resourcesDir.resolve("test1.html").toFile().readText()
        val txtBeatify = pagesGenerator.txtBeatify(txtString)
        assertEquals(expectedHtmlString, pagesGenerator.txtToHtml(txtBeatify))
    }
}