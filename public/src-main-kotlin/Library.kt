import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries

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
        val names: List<Name>, val authors: List<Author>, val tags: Set<String>, val rating: Int
    )

    fun main() {
        generatePublic()
    }

    private fun printCount(inputWritings: List<Writing>) {
        val novelCount = inputWritings.count { it.tags.contains("novel") }
        val recommendation = inputWritings.count {
            it.tags.contains("novel") && it.tags.contains("recommendation")
        }
        val entertaining = inputWritings.count {
            it.tags.contains("novel") && it.tags.contains("entertaining")
        }
        val archive = inputWritings.count {
            it.tags.contains("novel") && it.tags.contains("archive")
        }
        val chaos = inputWritings.count {
            it.tags.contains("novel") && it.tags.contains("chaos")
        }
        if ((recommendation + entertaining + archive + chaos) != novelCount) {
            throw IllegalStateException()
        }
        // Total novels 163, listed 128, unlisted 35.
        // Recommendation 10, entertaining 0, archive 109, chaos 9.
        val unlisted = 35
        print("Total novels ${novelCount + unlisted}, listed $novelCount, unlisted $unlisted.")
        print(" ")
        print("Recommendation $recommendation, entertaining $entertaining,")
        print(" ")
        print("archive $archive, chaos $chaos.")
        print("\n")
    }

    private fun formatWritings(writings: List<Writing>, language: String): String {
        return writings.joinToString(
            separator = "», «", prefix = ": «", postfix = "».", transform = { it.names[0].name })
    }

    private fun formatAuthors(authors: List<Author>, language: String): String {
        var author = authors[0].names[0].name
        if (authors[0].names.size > 1) {
            authors[0].names.forEach { if (it.language == language) author = it.name }
        }
        return author
    }

    fun loadWritings(srcDir: Path): MutableList<Writing> {
        val writingsIn: MutableList<Writing> = arrayListOf()
        srcDir.listDirectoryEntries("library*.json").forEach {
            writingsIn.addAll(Json.decodeFromString<List<Writing>>(it.toFile().readText()))
        }
        //printCount(writingsIn)
        return writingsIn
    }

    fun articlesFileFilter(w: Writing): Boolean {
        return (w.tags.contains("essay") || w.tags.contains("blogging")) && !w.tags.contains("chaos")
    }

    fun otherFileFilter(w: Writing): Boolean {
        return (!w.tags.contains("essay") && !w.tags.contains("blogging")) && !w.tags.contains("chaos")
    }

    fun chaosFileFilter(w: Writing): Boolean {
        return w.tags.contains("chaos")
    }

    fun writeWritings(dst: Path, writings: List<Writing>) {
        val format = Json { prettyPrint = true }

        val articlesToSave = writings.filter { articlesFileFilter(it) }.sortedBy { it.rating }
        val outArticleFile = dst.resolve("library-article.json").toFile()
        outArticleFile.writeText(format.encodeToString(articlesToSave))

        val othersToSave = writings.filter { otherFileFilter(it) }.sortedBy { it.rating }
        val outOtherFile = dst.resolve("library-other.json").toFile()
        outOtherFile.writeText(format.encodeToString(othersToSave))

        val chaosToSave = writings.filter { chaosFileFilter(it) }.sortedBy { it.rating }
        val outChaosFile = dst.resolve("library-chaos.json").toFile()
        outChaosFile.writeText(format.encodeToString(chaosToSave))

        val rest =
            writings.filter { !articlesFileFilter(it) && !otherFileFilter(it) && !chaosFileFilter(it) }
        if (rest.isNotEmpty()) throw IllegalStateException()
    }

    private fun loadWritings(): MutableList<Writing> {
        val writings = loadWritings(UtilsAbsolute.srcResDir)
        writeWritings(UtilsAbsolute.srcResDir, writings)
        return writings
    }

    fun booksFilter(writing: Writing): Boolean {
        return writing.tags.contains("recommendation")
    }

    fun articlesFilter(writing: Writing): Boolean {
        return writing.tags.contains("entertaining") && (writing.tags.contains("blogging") || writing.tags.contains(
            "short story"
        ))
    }

    private fun generatePublic() {
        val writingsIn = loadWritings()
        val libraryOut = UtilsAbsolute.srcGenDir

        val favoritesBuilder = StringBuilder("Интересные штуки размером с книгу. </>")
        val recommendations =
            writingsIn.filter { booksFilter(it) }.sortedBy { it.rating }.groupBy { it.authors }
        recommendations.forEach { (authors, writings) ->
            if (favoritesBuilder.isNotEmpty()) {
                favoritesBuilder.append(" ")
            }
            favoritesBuilder.append(formatAuthors(authors, "ru"))
            favoritesBuilder.append(formatWritings(writings, "ru"))
        }
        libraryOut.resolve("library-favorites.txt").toFile().writeText(favoritesBuilder.toString())

        val listsBuilder = StringBuilder()

        listsBuilder.append("Интересные штуки размером со статью. </> ")
        val posts =
            writingsIn.filter { articlesFilter(it) }.sortedBy { it.rating }.groupBy { it.authors }
        val postsBuilder = StringBuilder()
        posts.forEach { (authors, writings) ->
            if (postsBuilder.isNotEmpty()) {
                postsBuilder.append(" ")
            }
            postsBuilder.append(formatAuthors(authors, "en"))
            postsBuilder.append(formatWritings(writings, "en"))
        }
        listsBuilder.append(postsBuilder)

        listsBuilder.append("\n\n")

        listsBuilder.append("Ещё я читал этих авторов. </> ")
        val authors = writingsIn.filter {
                !recommendations.keys.contains(it.authors) && !posts.keys.contains(it.authors)
            }.sortedBy { it.rating }.groupBy { it.authors }.keys.toMutableList()
        listsBuilder.append(
            authors.joinToString(
                separator = ", ", prefix = "", postfix = ".", transform = { it[0].names[0].name })
        )
        libraryOut.resolve("library-interesting.txt").toFile().writeText(listsBuilder.toString())
    }
}