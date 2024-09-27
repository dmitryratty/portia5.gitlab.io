class IncludeTransform {

    private val includeShort = "short"
    private val includeLink = "link"
    private val includeParag = "paragraph"
    private val includeSection = "section"
    private val includeTag = "#include "
    val abstractSeparatorTemp = "<< * * * >>"
    val abstractSeparator = "\n\n$abstractSeparatorTemp\n\n"
    val sectionSeparator = "\n\n* * *\n\n"
    val paragSeparator = "\n\n"

    private fun onInclude(generator: Generator,
        page: Page,
        parag: String,
        paragIterator: MutableListIterator<String>,
        section: MutableList<String>,
        sectionsIterator: MutableListIterator<MutableList<String>>
    ) {
        if (page.url.isMap && generator.firstRun) return
        val commands = parag.split(" ").toMutableList()
        if (!commands.remove("#include")) throw IllegalStateException()
        val withLink = commands.remove(includeLink)
        val asSection = commands.remove(includeSection)
        val path = commands.removeLast()
        val includedPage =
            generator.sitemap.pages[path] ?: throw IllegalStateException("$path in ${page.url.relativeUrl}")
        transform(generator, includedPage)
        if (commands.isEmpty()) {
            if (!asSection && includedPage.summaryParag.isNotEmpty()) {
                paragIterator.remove()
                paragIterator.add(includedPage.summaryParag(withLink))
            } else if (includedPage.summarySection.isNotEmpty()) {
                paragIterator.remove()
                includedPage.summarySection(withLink).forEach {
                    paragIterator.add(it)
                }
            } else {
                throw IllegalStateException("$path in ${page.url.relativeUrl}")/*
                val summarySection = includedPage.summaryFull
                paragIterator.remove()
                summarySection.forEach {
                    paragIterator.add(it)
                }
                */
            }
        } else {
            throw IllegalStateException("${page.url.relativeUrl} $parag")
        }
    }

    fun transform(generator: Generator, page: Page) {
        if (page.abstracts.isNotEmpty()) return
        page.abstracts.addAll(page.formatted.split(abstractSeparator).map { supersection ->
            supersection.split(sectionSeparator).map { section ->
                section.split(paragSeparator).map { it }.toMutableList()
            }.toMutableList()
        })
        page.abstracts.forEach { abstract ->
            val sectionsIterator = abstract.listIterator()
            for (section in sectionsIterator) {
                val paragIterator = section.listIterator()
                for (parag in paragIterator) {
                    if (parag.startsWith(includeTag)) {
                        onInclude(generator, page, parag, paragIterator, section, sectionsIterator)
                    }
                }
            }
        }
        page.includeText = page.abstracts.joinToString(separator = abstractSeparator) { abstract ->
            abstract.joinToString(separator = sectionSeparator) { section ->
                section.joinToString(separator = paragSeparator) { it }
            }
        }
    }
}