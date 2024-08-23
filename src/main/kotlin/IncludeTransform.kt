class IncludeTransform {

    private val includeParag = "#include-paragraph "
    private val includeSection = "#include-section "
    private val includeFull = "#include "
    val supsecSeparatorTemp = "<< * * * >>"
    val supsecSeparator = "\n\n$supsecSeparatorTemp\n\n"
    val sectionSeparator = "\n\n* * *\n\n"
    val paragSeparator = "\n\n"

    fun includedPage(pages: Map<String, Page>, pref: String, parag: String): Page {
        val path = parag.substring(pref.length, parag.length)
        val includedPage = pages[path] ?: throw IllegalStateException("[$path]")
        transform(pages, includedPage)
        return includedPage
    }

    fun flatten(page: Page): String {
        return page.supsecs.joinToString(separator = supsecSeparator) { supersection ->
            supersection.joinToString(separator = sectionSeparator) { section ->
                section.joinToString(separator = paragSeparator) { it }
            }
        }
    }

    fun transform(pages: Map<String, Page>, page: Page) {
        if (page.supsecs.isNotEmpty()) return
        page.supsecs.addAll(page.raw.split(supsecSeparator).map { supersection ->
            supersection.split(sectionSeparator).map { section ->
                section.split(paragSeparator).map { it }.toMutableList()
            }.toMutableList()
        })
        if (page.url.isRoot && false) {
            page.supsecs.forEach { supersection ->
                println("supsec GO")
                supersection.forEach { section ->
                    println("section GO")
                    section.forEach { paragraph ->
                        println("[$paragraph]")
                    }
                    println("section OK")
                }
                println("supsec OK")
            }
        }
        page.supsecs.forEach { supsec ->
            val sectionsIterator = supsec.listIterator()
            for (section in sectionsIterator) {
                val paragIterator = section.listIterator()
                for (parag in paragIterator) {
                    if (parag.startsWith(includeParag)) {
                        val includedPage = includedPage(pages, includeParag, parag)
                        val summaryParagraph = includedPage.summaryParag
                            ?: throw IllegalStateException("${page.url.relativeUrl}, $parag")
                        paragIterator.remove()
                        paragIterator.add(summaryParagraph)
                    } else if (parag.startsWith(includeSection)) {
                        val includedPage = includedPage(pages, includeSection, parag)
                        val summarySection = includedPage.summarySection
                            ?: throw IllegalStateException("${page.url.relativeUrl}, $parag")
                        paragIterator.remove()
                        summarySection.forEach {
                            paragIterator.add(it)
                        }
                    } else if (parag.startsWith(includeFull)) {
                        val includedPage = includedPage(pages, includeFull, parag)
                        val summarySection = includedPage.summaryMax
                        paragIterator.remove()
                        summarySection!!.forEach {
                            paragIterator.add(it)
                        }
                    }
                }
            }
        }
        if (page.supsecs.size == 1) {
            val supsec = page.supsecs[0]
            if (supsec.size == 1) {
                val section = supsec[0]
                page.summaryMax = section
            }
        } else if (page.supsecs.size == 2) {
            val firstSupsec = page.supsecs[0]
            if (firstSupsec.size != 1) {
                throw IllegalStateException("${page.url.relativeUrl} ${firstSupsec[0]}")
            }
            val section = firstSupsec[0]
            if (section.size == 1) {
                page.summaryParag = "${section[0]}\n - ${page.url.absoluteUrl}"
            } else {
                page.summarySection = mutableListOf()
                page.summarySection!!.addAll(section)
                page.summarySection!!.add(page.url.absoluteUrl)
            }
        } else if (page.supsecs.size == 3) {
            throw IllegalStateException("TODO")
        } else {
            throw IllegalStateException()
        }
        page.includeText = flatten(page)
    }
}