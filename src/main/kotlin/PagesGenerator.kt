import java.io.File
import java.nio.file.Paths
import kotlin.text.StringBuilder

class PagesGenerator {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            PagesGenerator().main()
        }
    }

    // "<span class=\"nowrap\">$1</span>"
    val dashNoWrap = "(\\S+-\\S+)".toRegex()
    // "<a href=\"$1\">\$1</a>"
    val hyperlink = "(http\\S+)".toRegex()
    // "<span class=\"nowrap\">$1</span>"
    val footnote = "(\\S+\\[\\d+])".toRegex()

    fun main() {
        Library().main()
        val projectDir = Paths.get("ratty-public")
        val resourcesDir = projectDir.resolve("src/main/resources")
        val pageTemplate = resourcesDir.resolve("page-template.html").toFile().readText()
        listOf("life", "library").forEach {
            val text = resourcesDir.resolve("$it.txt").toFile().readText()
            val pageOut = projectDir.resolve("public/$it.html").toFile()
            processPage(pageTemplate, text, pageOut)
        }
    }

    private fun makeNoWrap(word: String): String {
        return "<span class=\"nowrap\">$word</span>"
    }

    private fun processLine(line: String): String? {
        if (!line.contains("http") && !line.contains('-') && !line.contains('[')) {
            return null
        }
        val builder = StringBuilder()
        line.split(' ').forEach { word ->
            if (builder.isNotEmpty()) {
                builder.append(' ')
            }
            if (word.length == 1) {
                builder.append(word)
            } else if (word.startsWith("http")) {
                builder.append("<a href=\"$word\">$word</a>")
            } else if (word.contains('-')) {
                builder.append(makeNoWrap(word))
            } else if (word.contains('[') && footnote.matches(word)) {
                builder.append(makeNoWrap(word))
            } else {
                builder.append(word)
            }
        }
        return builder.toString()
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
                val newValue = processLine(oldValue)
                if (newValue != null) {
                    iterate.set(newValue)
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