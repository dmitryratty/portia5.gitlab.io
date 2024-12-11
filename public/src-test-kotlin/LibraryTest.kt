
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
    data class WritingFullRecord(
        val name: String, val author: String, val tags: Set<String>, val rating: Int
    )

    @Serializable
    data class WritingMainRecord(
        val name: String, val author: String, val tags: Set<String>, val rating: Int
    )

    fun articlesFileFilter1(w: Library.Writing): Boolean {
        return (w.tags.contains("essay") || w.tags.contains("blogging"))
                && !w.tags.contains("chaos")
    }

    fun otherFileFilter1(w: Library.Writing): Boolean {
        return (!w.tags.contains("essay") && !w.tags.contains("blogging"))
                && !w.tags.contains("chaos")
    }

    fun chaosFileFilter1(w: Library.Writing): Boolean {
        return w.tags.contains("chaos")
    }

    fun loadWritings1(srcDir: Path): MutableList<Library.Writing> {
        val writingsIn: MutableList<Library.Writing> = arrayListOf()
        srcDir.listDirectoryEntries("library*.json").forEach {
            writingsIn.addAll(Json.decodeFromString<List<Library.Writing>>(it.toFile().readText()))
        }
        return writingsIn
    }

    fun writeWritings1(dst: Path, writings: List<Library.Writing>) {
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

    @Test fun test1() {
        val writings = loadWritings1(resIn)
        writeWritings1(resIn, writings)
        val authors = mutableListOf<Library.Author>()
        writings.forEach { authors.addAll(it.authors) }
        authors.sortBy { it.id() + it.names.size }
        val authorsRecords = mutableMapOf<String, Library.Author>()
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
        val authorsRecordsIn = mutableMapOf<String, Library.Author>()
        authorsRecordsIn.putAll(Json.decodeFromString<Map<String, Library.Author>>(resInAuthors.readText()))
        val authors = authorsRecordsIn.values.toMutableList()
        authors.sortBy { it.id() + it.names.size }
        val authorsRecords = mutableMapOf<String, Library.Author>()
        authors.forEach {
            val key = it.id()
            val duplicate = authorsRecords[key]
            if (duplicate == null) {
                authorsRecords[it.id()] = it
            } else {
                duplicate.names.addAll(it.names)
            }
        }
        val resOutAuthors = resOut.resolve("authors-2.json").toFile()
        resOutAuthors.writeText(format.encodeToString(authorsRecords))
        val writings = loadWritings1(resIn)
        writings.forEach { w ->
            w.authors.forEachIndexed { i, a ->
                if (authorsRecords[a.id()] == null) {
                    var newAuthorRecord: Library.Author? = null
                    authorsRecords.values.forEach { ar ->
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
        val chaosToSave = writings.sortedBy { it.rating }
        val outChaosFile = resOut.resolve("library-chaos.json").toFile()
        outChaosFile.writeText(format.encodeToString(chaosToSave))
    }
}