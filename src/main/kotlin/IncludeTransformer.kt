import kotlin.io.path.isDirectory

class IncludeTransformer {

    private val hostName = Utils().hostName
    private val includeTag = "#include "
    private val includeShortTag = "#include-short "
    val shortSeparator = "<< * * * >>"

    fun transform(pages: Map<String, Page>, page: Page) {
        if (page.includeText.isNotEmpty()) return
        val resultBuilder = StringBuilder()
        Utils().splitToParagraphs(page.raw).forEachIndexed { i, line ->
            if (resultBuilder.isNotEmpty()) resultBuilder.append("\n\n")
            if (line == shortSeparator) {
                if (i != 1) throw IllegalStateException("$page $i")
                page.summaryText = resultBuilder.toString().trim()
                var path = page.path
                if (path == "index.txt") {
                    page.summaryText += "\n - $hostName"
                } else {
                    path = if (path.endsWith("/index.txt")) {
                        path.removeSuffix("/index.txt")
                    } else {
                        path.removeSuffix(".txt")
                    }
                    page.summaryText += "\n - ${hostName}/${path}"
                }
                resultBuilder.append(line)
            } else if (line.startsWith(includeTag)) {
                val path = line.substring(includeTag.length, line.length)
                val includedPage = pages[path]
                if (includedPage!!.includeText.isEmpty()) {
                    transform(pages, includedPage)
                }
                if (includedPage.includeFullText != null) {
                    resultBuilder.append(includedPage.includeFullText)
                } else {
                    resultBuilder.append(includedPage.includeText)
                }
            } else if (line.startsWith(includeShortTag)) {
                var path = line.substring(includeShortTag.length, line.length)
                val include = Utils().pagesSrcDir.resolve(path)
                path += if (include.isDirectory()) {
                    "/index.txt"
                } else {
                    ".txt"
                }
                val includedPage = pages[path]
                if (includedPage!!.includeText.isEmpty()) {
                    transform(pages, includedPage)
                }
                if (includedPage.summaryText == null) throw IllegalStateException(path)
                resultBuilder.append(includedPage.summaryText)
            } else {
                resultBuilder.append(line)
            }
        }
        page.includeText = resultBuilder.toString()
        if (page.summaryText != null) {
            page.includeFullText = page.includeText.replaceFirst("$shortSeparator\n", "")
        }
    }
}