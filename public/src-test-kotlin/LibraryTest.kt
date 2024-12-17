
import UtilsAbsolute.testResDir
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.test.Test

class LibraryTest {
    private val resLibrary: Path = testResDir.resolve("library")
    private val resMigration: Path = resLibrary.resolve("migration")
    private val resIn: Path = resMigration.resolve("in")
    private val resOut: Path = resMigration.resolve("out")
    private val format = Json { prettyPrint = true }

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
        val names: List<Name>, val authors: MutableList<Author>, val tags: Set<String>,
        val rating: Int
    )

    @Serializable
    data class WritingRecord(
        val names: List<Name>, val authors: MutableList<String>, val tags: Set<String>,
        val rating: Int
    )

    fun articlesFileFilter1(w: Writing): Boolean {
        return (w.tags.contains("essay") || w.tags.contains("blogging"))
                && !w.tags.contains("chaos")
    }

    fun otherFileFilter1(w: Writing): Boolean {
        return (!w.tags.contains("essay") && !w.tags.contains("blogging"))
                && !w.tags.contains("chaos")
    }

    fun chaosFileFilter1(w: Writing): Boolean {
        return w.tags.contains("chaos")
    }

    fun loadWritings1(srcDir: Path): MutableList<Writing> {
        val writingsIn: MutableList<Writing> = arrayListOf()
        srcDir.listDirectoryEntries("library*.json").forEach {
            writingsIn.addAll(Json.decodeFromString<List<Writing>>(it.toFile().readText()))
        }
        return writingsIn
    }

    fun writeWritings1(dst: Path, writings: List<Writing>) {
        val articlesToSave = writings.filter { articlesFileFilter1(it) }.sortedBy { it.rating }
        val outArticleFile = dst.resolve("library-article.json").toFile()
        outArticleFile.writeText(format.encodeToString(articlesToSave))

        val othersToSave = writings.filter { otherFileFilter1(it) }.sortedBy { it.rating }
        val outOtherFile = dst.resolve("library-other.json").toFile()
        outOtherFile.writeText(format.encodeToString(othersToSave))

        val chaosToSave = writings.filter { chaosFileFilter1(it) }.sortedBy { it.rating }
        val outChaosFile = dst.resolve("library-chaos.json").toFile()
        outChaosFile.writeText(format.encodeToString(chaosToSave))

        val rest = writings.filter { !articlesFileFilter1(it)
                    && !otherFileFilter1(it) && !chaosFileFilter1(it) }
        if (rest.isNotEmpty()) throw IllegalStateException()
    }

    fun updateWritingsAuthors(authorsMap: Map<String, Author>, writings: List<Writing>) {
        // This is for case when author ID changed and to match writing with
        // its author we perform search in authors using all ID variations
        // of each author. Change of author ID may occur when we add author
        // name in another language.
        writings.forEach { w ->
            w.authors.forEachIndexed { i, a ->
                if (authorsMap[a.id()] == null) {
                    var newAuthorRecord: Author? = null
                    authorsMap.values.forEach { ar ->
                        a.names.forEach { an ->
                            if (ar.names.contains(an)) {
                                newAuthorRecord = ar
                            }
                        }
                    }
                    if (newAuthorRecord == null) throw IllegalStateException(a.toString())
                    w.authors[i] = newAuthorRecord!!
                }
            }
        }
    }

    @Test fun test1() {
        val writings = loadWritings1(resIn)
        writeWritings1(resIn, writings)
        // Extract authors.
        val authors = mutableListOf<Author>()
        writings.forEach { authors.addAll(it.authors) }
        // Normalize authors.
        authors.sortBy { it.id() + it.names.size }
        val authorsRecords = mutableMapOf<String, Author>()
        authors.forEach {
            val key = it.id()
            val duplicate = authorsRecords[key]
            if (duplicate == null) {
                if (key == "Eliezer Yudkowsky") {
                    if (it.names.size != 1) {
                        // Special condition for test data after sorting, required for test of
                        // merging names.
                        throw IllegalStateException("First occurence should be single name.")
                    }
                }
                authorsRecords[it.id()] = it
            } else {
                duplicate.names.addAll(it.names)
            }
        }
        val outAuthorsFile = resOut.resolve("authors.json").toFile()
        outAuthorsFile.writeText(format.encodeToString(authorsRecords))
    }


    @Test fun test2() {
        val resInAuthors = resOut.resolve("authors-1.json").toFile()
        val authorsMapIn = mutableMapOf<String, Author>()
        authorsMapIn.putAll(Json.decodeFromString<Map<String, Author>>(resInAuthors.readText()))
        val authors = authorsMapIn.values.toMutableList()
        authors.sortBy { it.id() + it.names.size }
        val authorsMap = mutableMapOf<String, Author>()
        authors.forEach {
            val key = it.id()
            val duplicate = authorsMap[key]
            if (duplicate == null) {
                authorsMap[it.id()] = it
            } else {
                duplicate.names.addAll(it.names)
            }
        }
        val resOutAuthors = resOut.resolve("authors-2.json").toFile()
        resOutAuthors.writeText(format.encodeToString(authorsMap))
        val writings = loadWritings1(resIn)
        updateWritingsAuthors(authorsMap, writings)
        val writingRecords = mutableListOf<WritingRecord>()
        writings.forEach { w ->
            val a = w.authors.map { it.id() }.toMutableList()
            writingRecords.add(WritingRecord(w.names, a, w.tags, w.rating))
        }
        val writingsFullToSave = writings.sortedBy { it.rating }
        val outWritingsFullFile = resOut.resolve("writings-full.json").toFile()
        outWritingsFullFile.writeText(format.encodeToString(writingsFullToSave))
        val recordsFile = resOut.resolve("writings-chaos.json").toFile()
        recordsFile.writeText(format.encodeToString(writingRecords))
    }
}