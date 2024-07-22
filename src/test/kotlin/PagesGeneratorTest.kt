import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.test.Test
import kotlin.test.assertEquals

class PagesGeneratorTest {

    private val pagesGenerator: PagesGenerator = PagesGenerator()

    @Test fun utilsListPages() {
        Utils().textPagesInput().forEach {
            println(it)
        }
        val relativePath = "some/some.txt"
        println(relativePath.substring(0, relativePath.length - 3))
    }

    @Test
    fun transformLine() {

        var lineIn = "\"The Map of Mathematics\" by Domain of Science:"
        var lineOut = lineIn
        assertEquals(lineOut, pagesGenerator.transformLine(lineIn))

        lineIn = " — https://youtu.be/OmJ-4B-mS-Y?si=bBWOSbdlpQ7kV9Bz"
        lineOut = "&nbsp;— <a href=\"https://youtu.be/OmJ-4B-mS-Y?si=bBWOSbdlpQ7kV9Bz\">" +
                "https:<wbr>//<wbr>youtu<wbr>.be<wbr>/OmJ<wbr>-4B<wbr>-mS<wbr>-Y<wbr>" +
                "?si<wbr>=<wbr>bBWOSbdlpQ7kV9Bz</a>"
        assertEquals(lineOut, pagesGenerator.transformLine(lineIn))

        lineIn = "    — https://youtu.be/OJ4B"
        lineOut = "&nbsp;&nbsp;&nbsp;&nbsp;— <a href=\"https://youtu.be/OJ4B\">" +
                "https://youtu.be/OJ4B</a>"
        assertEquals(lineOut, pagesGenerator.transformLine(lineIn))

        lineIn = "    — https://youtu.be/OJ4BLKJIRGKSHDKFSHAG"
        lineOut = "&nbsp;&nbsp;&nbsp;&nbsp;— <a href=\"https://youtu.be/OJ4BLKJIRGKSHDKFSHAG\">" +
                "https:<wbr>//<wbr>youtu<wbr>.be<wbr>/OJ4BLKJIRGKSHDKFSHAG</a>"
        assertEquals(lineOut, pagesGenerator.transformLine(lineIn))
    }

    @Test
    fun isFootnote() {
        var input = "world![1]"
        assertEquals(true, pagesGenerator.footnote.matches(input))
        input = "world![1][2][3]"
        assertEquals(true, pagesGenerator.footnote.matches(input))
        input = "world![132122][3][111]"
        assertEquals(true, pagesGenerator.footnote.matches(input))
        input = "world![132122][3][111]"
        assertEquals(true, pagesGenerator.footnote.matches(input))
        input = "world!"
        assertEquals(false, pagesGenerator.footnote.matches(input))
        input = "wo[rl]d!"
        assertEquals(false, pagesGenerator.footnote.matches(input))
        input = "world[1]!"
        assertEquals(false, pagesGenerator.footnote.matches(input))
        input = "world[1]."
        assertEquals(false, pagesGenerator.footnote.matches(input))
        input = "[1]"
        assertEquals(false, pagesGenerator.footnote.matches(input))
        input = "[1]word!"
        assertEquals(false, pagesGenerator.footnote.matches(input))
        input = "world![.]"
        assertEquals(false, pagesGenerator.footnote.matches(input))
    }

    @Test
    fun transformParagraph() {

        val paragraphIn = "\"The Map of Mathematics\" by Domain of Science:" +
                "\n" +
                " — https://youtu.be/OmJ-4B-mS-Y?si=bBWOSbdlpQ7kV9Bz"

        // xhmtlCompatibleVoidElements = false
        var paragraphOut = "<p>\"The Map of Mathematics\" by Domain of Science:" +
                "\n" +
                "        <br>&nbsp;— <a href=\"https://youtu.be/OmJ-4B-mS-Y?si=bBWOSbdlpQ7kV9Bz\">" +
                "https:<wbr>//<wbr>youtu<wbr>.be<wbr>/OmJ<wbr>-4B<wbr>-mS<wbr>-Y<wbr>" +
                "?si<wbr>=<wbr>bBWOSbdlpQ7kV9Bz</a></p>"
        // Test.
        assertEquals(paragraphOut, PagesGenerator(false).transformParagraph(paragraphIn))

        // xhmtlCompatibleVoidElements = true
        paragraphOut = "<p>\"The Map of Mathematics\" by Domain of Science:" +
                "\n" +
                "        <br/>&nbsp;— <a href=\"https://youtu.be/OmJ-4B-mS-Y?si=bBWOSbdlpQ7kV9Bz\">" +
                "https:<wbr/>//<wbr/>youtu<wbr/>.be<wbr/>/OmJ<wbr/>-4B<wbr/>-mS<wbr/>-Y<wbr/>" +
                "?si<wbr/>=<wbr/>bBWOSbdlpQ7kV9Bz</a></p>"
        // Test.
        assertEquals(paragraphOut, PagesGenerator(true).transformParagraph(paragraphIn))
    }

    @Test
    fun textToHtml() {
        val resourcesDir = Paths.get("src/test/resources")
        assert(resourcesDir.exists())
        val textString = resourcesDir.resolve("test1.txt").toFile().readText()
        val expectedHtmlString = resourcesDir.resolve("test1.html").toFile().readText()

        val beautyfiedText = TextBeautifier().transform(textString)
        val titleAndBody = pagesGenerator.titleAndBody("test1.txt", beautyfiedText)
        val bodyHtml = pagesGenerator.textToHtml(titleAndBody.first, titleAndBody.second)
        val htmlPage = pagesGenerator.htmlPage(titleAndBody.first, bodyHtml, true)
        assertEquals(expectedHtmlString, htmlPage)
    }
}