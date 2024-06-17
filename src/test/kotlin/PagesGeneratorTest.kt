import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.test.Test
import kotlin.test.assertEquals

class PagesGeneratorTest {

    private val pagesGenerator: PagesGenerator = PagesGenerator()

    @Test
    fun beautifyLine() {
        val lineIn = " - Start space-dash. And - dash in middle... And - another dash..."
        val lineOut = " — Start space-dash. And — dash in middle… And — another dash…"
        assertEquals(lineOut, TextBeautifier().transformLine("test", lineIn))
    }

    @Test
    fun beautifyParagraphs() {
        val textIn = " - Start space-dash. And - dash in middle... And - another dash...\n" +
                "- Start dash. Hello!\n\n" +
                "- Another start dash. And - dash..."
        val textOut = " — Start space-dash. And — dash in middle… And — another dash…\n" +
                "— Start dash. Hello!\n\n" +
                "— Another start dash. And — dash…"
        assertEquals(textOut, TextBeautifier().transform("test", textIn))
    }

    @Test
    fun transformLine() {
        var lineIn = "\"The Map of Mathematics\" by Domain of Science:"
        var lineOut: String? = lineIn
        assertEquals(lineOut, pagesGenerator.transformLine(lineIn))

        lineIn = " — https://youtu.be/OmJ-4B-mS-Y?si=bBWOSbdlpQ7kV9Bz"
        lineOut = "&nbsp;— <a href=\"https://youtu.be/OmJ-4B-mS-Y?si=bBWOSbdlpQ7kV9Bz\">" +
                "https:<wbr>//<wbr>youtu<wbr>.be<wbr>/OmJ<wbr>-4B<wbr>-mS<wbr>-Y<wbr>" +
                "?si<wbr>=<wbr>bBWOSbdlpQ7kV9Bz</a>"
        assertEquals(lineOut, pagesGenerator.transformLine(lineIn))

        lineIn = "    — https://youtu.be/OJ4B"
        lineOut = "&nbsp;&nbsp;&nbsp;&nbsp;— <a href=\"https://youtu.be/OJ4B\">" +
                "https:<wbr>//<wbr>youtu<wbr>.be<wbr>/OJ4B</a>"
        assertEquals(lineOut, pagesGenerator.transformLine(lineIn))
    }

    @Test
    fun transformParagraph() {

        val paragraphIn = "\"The Map of Mathematics\" by Domain of Science:\n" +
                " — https://youtu.be/OmJ-4B-mS-Y?si=bBWOSbdlpQ7kV9Bz"

        var paragraphOut = "<p>\"The Map of Mathematics\" by Domain of Science:\n" +
                "        <br>&nbsp;— <a href=\"https://youtu.be/OmJ-4B-mS-Y?si=bBWOSbdlpQ7kV9Bz\">" +
                "https:<wbr>//<wbr>youtu<wbr>.be<wbr>/OmJ<wbr>-4B<wbr>-mS<wbr>-Y<wbr>" +
                "?si<wbr>=<wbr>bBWOSbdlpQ7kV9Bz</a></p>"

        assertEquals(paragraphOut, PagesGenerator(false).transformParagraph(paragraphIn))

        paragraphOut = "<p>\"The Map of Mathematics\" by Domain of Science:\n" +
                "        <br/>&nbsp;— <a href=\"https://youtu.be/OmJ-4B-mS-Y?si=bBWOSbdlpQ7kV9Bz\">" +
                "https:<wbr/>//<wbr/>youtu<wbr/>.be<wbr/>/OmJ<wbr/>-4B<wbr/>-mS<wbr/>-Y<wbr/>" +
                "?si<wbr/>=<wbr/>bBWOSbdlpQ7kV9Bz</a></p>"

        assertEquals(paragraphOut, PagesGenerator(true).transformParagraph(paragraphIn))
    }

    @Test
    fun textToHtml() {
        val resourcesDir = Paths.get("src/test/resources")
        assert(resourcesDir.exists())
        val textString = resourcesDir.resolve("test1.txt").toFile().readText()
        val expectedHtmlString = resourcesDir.resolve("test1.html").toFile().readText()

        val beautyfiedText = TextBeautifier().transform("test", textString)
        val titleAndBody = pagesGenerator.titleAndBody(beautyfiedText)
        val bodyHtml = pagesGenerator.textToHtml(titleAndBody.second)
        val htmlPage = pagesGenerator.htmlPage(titleAndBody.first, bodyHtml, true)
        assertEquals(expectedHtmlString, htmlPage)
    }
}