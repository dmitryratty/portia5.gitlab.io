import java.lang.StringBuilder
import java.nio.file.Paths

class PagesGenerator {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            PagesGenerator().main()
        }
    }

    fun main() {
        val projectDir = Paths.get("public")
        val pageTemplate = projectDir.resolve("src/page-template.html").toFile().readText()

        val title = "Ну… Жизнь!"
        val article = StringBuilder()
        val text = projectDir.resolve("src/life.txt").toFile().readText()
        val dashNoWrap = "(\\S+-\\S+)".toRegex()
        val paragraphs = text.split("\n\n")
        paragraphs.forEach { paragraph ->
            if (article.isNotEmpty()) {
                article.append("\n\n    ")
            }
            val lines = paragraph.trim().split('\n').toMutableList()
            val iterate = lines.listIterator()
            while (iterate.hasNext()) {
                val oldValue = iterate.next()
                if (oldValue.contains('-')) {
                    iterate.set(dashNoWrap.replace(oldValue, "<span class=\"nowrap\">$1</span>"))
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