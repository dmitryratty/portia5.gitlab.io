import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.io.path.listDirectoryEntries
import kotlin.text.StringBuilder

/**
 * Novel - роман.
 */
class Library {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Library().main()
        }
    }

    @Serializable
    data class Name(val name: String, val language: String, val link: String? = null)

    @Serializable
    data class Author(val names: List<Name>)

    @Serializable
    data class Writing(
        val names: List<Name>,
        val authors: List<Author>,
        val tags: Set<String>,
        val rating: Int
    )

    fun main() {
        generatePublic()
    }

    private fun printCount(inputWritings: List<Writing>) {
        val novelCount = inputWritings.count { it.tags.contains("novel") }
        val recommendation = inputWritings.count {
            it.tags.contains("novel")
                    && it.tags.contains("recommendation")
        }
        val entertaining = inputWritings.count {
            it.tags.contains("novel")
                    && it.tags.contains("entertaining")
        }
        val archive = inputWritings.count {
            it.tags.contains("novel")
                    && it.tags.contains("archive")
        }
        val hidden = inputWritings.count {
            it.tags.contains("novel")
                    && it.tags.contains("hidden")
        }
        if ((recommendation + entertaining + archive + hidden) != novelCount) {
            throw IllegalStateException()
        }
        // Total novels 163, listed 128, unlisted 35.
        // Recommendation 10, entertaining 0, archive 109, hidden 9.
        val unlisted = 35
        print("Total novels ${novelCount + unlisted}, listed $novelCount, unlisted $unlisted.")
        print(" ")
        print("Recommendation $recommendation, entertaining $entertaining,")
        print(" ")
        print("archive $archive, hidden $hidden.")
        print("\n")
    }

    private fun formatWritings(writings: List<Writing>, language: String): String {
        return writings.joinToString(
            separator = "», «",
            prefix = ": «",
            postfix = "».",
            transform = { it.names[0].name })
    }

    private fun formatAuthors(authors: List<Author>, language: String): String {
        var author = authors[0].names[0].name
        if (authors[0].names.size > 1) {
            authors[0].names.forEach { if (it.language == language) author = it.name }
        }
        return author
    }

    private fun loadWritings(): MutableList<Writing> {
        val resourcesDir = Utils.resourcesDir
        val writingsIn: MutableList<Writing> = arrayListOf()
        resourcesDir.listDirectoryEntries("library*.json").forEach {
            writingsIn.addAll(Json.decodeFromString<List<Writing>>(it.toFile().readText()))
        }
        //printCount(writingsIn)

        val format = Json { prettyPrint = true }

        val articlesToSave = writingsIn
            .filter { it.tags.contains("essay") || it.tags.contains("blogging") }
            .sortedBy { it.rating }
        val outArticleFile = resourcesDir.resolve("library-article.json").toFile()
        outArticleFile.writeText(format.encodeToString(articlesToSave))

        val othersToSave = writingsIn
            .filter { !it.tags.contains("essay") && !it.tags.contains("blogging") }
            .sortedBy { it.rating }
        val outOtherFile = resourcesDir.resolve("library-other.json").toFile()
        outOtherFile.writeText(format.encodeToString(othersToSave))
        return writingsIn
    }

    private fun generatePublic() {
        val writingsIn = loadWritings()
        val libraryOut = Utils.srcPagesGeneratedDir

        val favoritesBuilder = StringBuilder("Интересные штуки размером с книгу. </>")
        val recommendations = writingsIn
            .filter { it.tags.contains("recommendation") }
            .sortedBy { it.rating }
            .groupBy { it.authors }
        recommendations.forEach { (authors, writings) ->
            if (favoritesBuilder.isNotEmpty()) {
                favoritesBuilder.append(" ")
            }
            favoritesBuilder.append(formatAuthors(authors, "ru"))
            favoritesBuilder.append(formatWritings(writings, "ru"))
        }
        libraryOut.resolve("library-favorites.txt").toFile().writeText(favoritesBuilder.toString())

        val listsBuilder = StringBuilder()
        val entertaining = writingsIn
            .filter { it.tags.contains("entertaining") && !it.tags.contains("blogging") }
            .sortedBy { it.rating }
            .groupBy { it.authors }
        if (entertaining.isNotEmpty()) {
            listsBuilder.append("\n\nПросто забавные штуки.\n\n")
            entertaining.forEach { (authors, writings) ->
                if (listsBuilder.isNotEmpty()) {
                    listsBuilder.append(" ")
                }
                listsBuilder.append(formatAuthors(authors, "ru"))
                listsBuilder.append(formatWritings(writings, "ru"))
            }
            listsBuilder.append("\n\n* * *\n\n")
        }

        listsBuilder.append("Интересные штуки размером со статью. </> ")
        val posts = writingsIn
            .filter { it.tags.contains("entertaining") && it.tags.contains("blogging") }
            .sortedBy { it.rating }
            .groupBy { it.authors }
        val postsBuilder = StringBuilder()
        posts.forEach { (authors, writings) ->
            if (postsBuilder.isNotEmpty()) {
                postsBuilder.append(" ")
            }
            postsBuilder.append(formatAuthors(authors, "en"))
            postsBuilder.append(formatWritings(writings, "en"))
        }
        listsBuilder.append(postsBuilder)

        listsBuilder.append("\n\n* * *\n\n")

        listsBuilder.append("Ещё я читал этих авторов. </> ")
        val authors = writingsIn
            .filter { !it.tags.contains("hidden") && !it.tags.contains("blogging") }
            .sortedBy { it.rating }
            .groupBy { it.authors }
            .keys.toMutableList()
        authors.removeAll(recommendations.keys)
        authors.removeAll(entertaining.keys)
        listsBuilder.append(
            authors.joinToString(
                separator = ", ",
                prefix = "",
                postfix = ".",
                transform = { it[0].names[0].name })
        )
        libraryOut.resolve("library-interesting.txt").toFile().writeText(listsBuilder.toString())
    }
}