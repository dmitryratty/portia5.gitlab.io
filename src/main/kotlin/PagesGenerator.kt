import java.lang.StringBuilder
import java.nio.file.Paths

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
        val pageTemplate = projectDir.resolve("src/page-template.html").toFile().readText()

        val title = "Ну… Жизнь!"
        val article = StringBuilder()
        val text = projectDir.resolve("src/life.txt").toFile().readText()

        val paragraphs = text.split("\n\n")
        paragraphs.forEach { paragraph ->
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
        val pageOut = projectDir.resolve("public/life.html")
        val titledTemplate = pageTemplate.replace("<!-- TITLE -->", title)
        pageOut.toFile().writeText(titledTemplate.replace("<!-- DATA -->", article.toString()))
    }
}