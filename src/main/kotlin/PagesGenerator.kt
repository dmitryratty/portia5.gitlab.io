import java.io.File
import java.nio.file.Paths
import kotlin.text.StringBuilder

class PagesGenerator {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            PagesGenerator().main()
        }
    }

    val dashNoWrap = "(\\S+-\\S+)".toRegex()
    val hyperlink = "(http\\S+)".toRegex()

    fun main() {
        val projectDir = Paths.get("ratty-public")
        val resourcesDir = projectDir.resolve("src/main/resources")
        val pageTemplate = resourcesDir.resolve("page-template.html").toFile().readText()
        listOf("life", "library").forEach {
            val text = resourcesDir.resolve("$it.txt").toFile().readText()
            val pageOut = projectDir.resolve("public/$it.html").toFile()
            processPage(pageTemplate, text, pageOut)
        }
    }

    private fun processPage(pageTemplate: String, text: String, pageOut: File) {
        val title = StringBuilder()
        val article = StringBuilder()
        val paragraphs = text.split("\n\n")
        paragraphs.forEach { paragraph ->
            if (title.isEmpty()) {
                title.append(paragraph)
                return@forEach
            }
            if (article.isNotEmpty()) {
                article.append("\n\n    ")
            }
            if (paragraph.contains("* * *")) {
                if (paragraph != "* * *") {
                    throw IllegalStateException()
                }
                article.append("<p class=\"dinkus\">* * *</p>")
                return@forEach
            }
            val lines = paragraph.trim().split('\n').toMutableList()
            val iterate = lines.listIterator()
            while (iterate.hasNext()) {
                val oldValue = iterate.next()
                if (oldValue.contains('-')) {
                    iterate.set(dashNoWrap.replace(oldValue, "<span class=\"nowrap\">$1</span>"))
                }
                if (oldValue.contains(" http")) {
                    iterate.set(hyperlink.replace(oldValue, "<a href=\"$1\">\$1</a>"))
                }
            }
            article.append("<p>")
            article.append(lines.joinToString("\n        <br/>"))
            article.append("</p>")
        }
        val titledTemplate = pageTemplate.replace("<!-- TITLE -->", title.toString())
        pageOut.writeText(titledTemplate.replace("<!-- DATA -->", article.toString()))
    }
}