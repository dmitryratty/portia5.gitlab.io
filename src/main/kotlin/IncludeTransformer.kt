import kotlin.io.path.isDirectory

class IncludeTransformer {

    private val hostName = Utils().hostName
    private val includeTag = "#include "
    private val includeShortTag = "#include-short "
    val shortSeparator = "<< * * * >>"

    fun transform(pages: Map<String, Page>, page: Page) {
        if (page.includeText.isNotEmpty()) return
        val resultBuilder = StringBuilder()
        page.raw.split('\n').forEachIndexed { i, line ->
            if (line == shortSeparator) {
                if (i != 2) throw IllegalStateException("$page $i")
                page.includeShortText = resultBuilder.toString()
                var path = page.path
                path = if (path.endsWith("/index.txt")) {
                    path.removeSuffix("/index.txt")
                } else {
                    path.removeSuffix(".txt")
                }
                page.includeShortText += " - ${hostName}/${path}"
                resultBuilder.append('\n').append(line)
            } else if (line.startsWith(includeTag)) {
                val path = line.substring(includeTag.length, line.length)
                val includedPage = pages[path]
                if (includedPage!!.includeText.isEmpty()) {
                    transform(pages, includedPage)
                }
                if (includedPage.includeFullText != null) {
                    resultBuilder.append('\n').append(includedPage.includeFullText)
                } else {
                    resultBuilder.append('\n').append(includedPage.includeText)
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
                if (includedPage.includeShortText == null) throw IllegalStateException(path)
                resultBuilder.append('\n').append(includedPage.includeShortText)
            } else {
                resultBuilder.append('\n').append(line)
            }
        }
        page.includeText = resultBuilder.toString()
        if (page.includeShortText != null) {
            page.includeFullText = page.includeText.replaceFirst("$shortSeparator\n", "")
        }
    }
}