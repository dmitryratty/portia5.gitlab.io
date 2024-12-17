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

    interface FiltrableWriting {
        fun containTags(vararg tags: String): Boolean
    }

    @Serializable
    data class Name(val name: String, val language: String,
                    val link: String? = null, val comment: String? = null)

    @Serializable
    data class Author(val names: LinkedHashSet<Name>) {
        fun id(): String {
            val nameEn = names.filter { it.language == "en" }
            require(nameEn.size < 2)
            return if (nameEn.size == 1) {
                nameEn.first().name
            } else {
                names.first().name
            }
        }
        fun sort(): String {
            return id() + names.size
        }
    }

    @Serializable
    data class Writing(
        val names: List<Name>, val authors: MutableList<Author>, val tags: Set<String>, val rating: Int
    ) : FiltrableWriting {
        override fun containTags(vararg tags: String): Boolean {
            for (tag in tags) if (this.tags.contains(tag)) return true
            return false
        }
    }

    @Serializable
    data class WritingRecord(
        val names: List<Name>, val authors: MutableList<String>, val tags: Set<String>,
        val rating: Int
    ) : FiltrableWriting {
        override fun containTags(vararg tags: String): Boolean {
            for (tag in tags) if (this.tags.contains(tag)) return true
            return false
        }
    }

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
        var author = authors[0].names.first().name
        if (authors[0].names.size > 1) {
            authors[0].names.forEach { if (it.language == language) author = it.name }
        }
        return author
    }

    fun saveLibrary(dst: Path, authorsMap: MutableMap<String, Author>, writingsIn: MutableList<Writing>) {
        val format = Json { prettyPrint = true }
        val outAuthorsFile = dst.resolve("authors.json").toFile()
        outAuthorsFile.writeText(format.encodeToString(authorsMap.values.toList()))

        val writings = mutableListOf<WritingRecord>()
        writingsIn.forEach { w ->
            val a = w.authors.map { it.id() }.toMutableList()
            writings.add(WritingRecord(w.names, a, w.tags, w.rating))
        }

        val articlesToSave = writings.filter { articlesFileFilter(it) }.sortedBy { it.rating }
        val outArticleFile = dst.resolve("library-article.json").toFile()
        outArticleFile.writeText(format.encodeToString(articlesToSave))

        val othersToSave = writings.filter { otherFileFilter(it) }.sortedBy { it.rating }
        val outOtherFile = dst.resolve("library-other.json").toFile()
        outOtherFile.writeText(format.encodeToString(othersToSave))

        val chaosToSave = writings.filter { chaosFileFilter(it) }.sortedBy { it.rating }
        val outChaosFile = dst.resolve("library-chaos.json").toFile()
        outChaosFile.writeText(format.encodeToString(chaosToSave))

        val rest = writings.filter { !articlesFileFilter(it)
                && !otherFileFilter(it) && !chaosFileFilter(it) }
        if (rest.isNotEmpty()) throw IllegalStateException()
    }

    fun loadAuthors(srcDir: Path): MutableMap<String, Author> {
        val authorsIn: List<Author> = Json.decodeFromString<List<Author>>(
            srcDir.resolve("authors.json").toFile().readText())
        val authorsMap = mutableMapOf<String, Author>()
        authorsIn.forEach {
            authorsMap[it.id()] = it
        }
        return authorsMap
    }

    fun loadWritings(srcDir: Path, authorsMap: MutableMap<String, Author>): MutableList<Writing> {
        val writingsIn: MutableList<WritingRecord> = arrayListOf()
        srcDir.listDirectoryEntries("library*.json").forEach {
            writingsIn.addAll(Json.decodeFromString<List<WritingRecord>>(it.toFile().readText()))
        }
        val writings = mutableListOf<Writing>()
        writingsIn.forEach { writingIn ->
            val authors = mutableListOf<Author>()
            writingIn.authors.forEach {
                authors.add(authorsMap[it]!!)
            }
            writings.add(Writing(writingIn.names, authors, writingIn.tags, writingIn.rating))
        }
        // Next is for case when author ID changed and to match writing with
        // its author we perform search in authors using all ID variations
        // of each author. Change of author ID may occur when we add author
        // name in another language.
        writings.forEach { writing ->
            writing.authors.forEachIndexed { i, author ->
                if (authorsMap[author.id()] == null) {
                    var newAuthor: Author? = null
                    authorsMap.values.forEach { authorFromMap ->
                        author.names.forEach { an ->
                            if (authorFromMap.names.contains(an)) {
                                newAuthor = authorFromMap
                            }
                        }
                    }
                    if (newAuthor == null) throw IllegalStateException(author.toString())
                    writing.authors[i] = newAuthor!!
                }
            }
        }
        return writings
    }

    fun articlesFileFilter(w: FiltrableWriting): Boolean {
        return w.containTags("essay", "blogging") && !w.containTags("chaos")
    }

    fun otherFileFilter(w: FiltrableWriting): Boolean {
        return (!w.containTags("essay") && !w.containTags("blogging")) && !w.containTags("chaos")
    }

    fun chaosFileFilter(w: FiltrableWriting): Boolean {
        return w.containTags("chaos")
    }

    fun booksFilter(writing: FiltrableWriting): Boolean {
        return writing.containTags("recommendation")
    }

    fun articlesFilter(writing: FiltrableWriting): Boolean {
        return writing.containTags("entertaining") && writing.containTags("blogging", "short story")
    }

    private fun generatePublic() {
        val authors = loadAuthors(UtilsAbsolute.srcResDir)
        val writingsIn = loadWritings(UtilsAbsolute.srcResDir, authors)
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
        val authorsList = writingsIn.filter {
                !recommendations.keys.contains(it.authors) && !posts.keys.contains(it.authors)
            }.sortedBy { it.rating }.groupBy { it.authors }.keys.toMutableList()
        listsBuilder.append(
            authorsList.joinToString(
                separator = ", ", prefix = "", postfix = ".", transform = { it[0].names.first().name })
        )
        libraryOut.resolve("library-interesting.txt").toFile().writeText(listsBuilder.toString())
    }
}