import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.lang.StringBuilder
import java.nio.file.Paths

/**
 * Novel - роман.
 */
class Library {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            Library().main()
        }
    }

    @Serializable
    data class Name(val name: String, val language: String, val link: String? = null)

    @Serializable
    data class Author(val names: List<Name>)

    @Serializable
    data class Writing(val names: List<Name>,
        val authors: List<Author>,
        val tags: Set<String>,
        val rating: Int)

    fun main() {
        generatePublic()
    }

    private fun printCount(inputWritings: List<Writing>) {
        val novelCount = inputWritings.count { it.tags.contains("novel") }
        val recommendation = inputWritings.count { it.tags.contains("novel")
                && it.tags.contains("recommendation") }
        val entertaining = inputWritings.count { it.tags.contains("novel")
                && it.tags.contains("entertaining") }
        val archive = inputWritings.count { it.tags.contains("novel")
                && it.tags.contains("archive") }
        val hidden = inputWritings.count { it.tags.contains("novel")
                && it.tags.contains("hidden") }
        if ((recommendation + entertaining + archive + hidden) != novelCount) {
            throw IllegalStateException()
        }
        // Total novels 162, listed 127, unlisted 35.
        // Recommendation 7, entertaining 0, archive 111, hidden 9.
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

    private fun generatePublic() {
        val moduleDir = Paths.get("ratty-public")
        val inputFile = moduleDir.resolve("src/main/resources/library.json").toFile()
        val writingsIn = Json.decodeFromString<List<Writing>>(inputFile.readText())
        printCount(writingsIn)

        val result = StringBuilder("Ну… Библиотека!")

        result.append("\n\nШтуки, которые могу порекомендовать.\n\n")
        val recommendations = writingsIn.filter { it.tags.contains("recommendation") }
            .sortedBy { it.rating }.groupBy { it.authors }
        recommendations.forEach { (authors, writings) ->
            result.append(" ")
            result.append(formatAuthors(authors, "ru"))
            result.append(formatWritings(writings, "ru"))
        }
        result.append("\n\n* * *")

        val entertaining =
            writingsIn.filter { it.tags.contains("entertaining") && !it.tags.contains("blogging") }
                .groupBy { it.authors }
        if (entertaining.isNotEmpty()) {
            result.append("\n\nПросто забавные штуки.\n\n")
            entertaining.forEach { (authors, writings) ->
                result.append(" ")
                result.append(formatAuthors(authors, "ru"))
                result.append(formatWritings(writings, "ru"))
            }
            result.append("\n\n* * *")
        }

        result.append("\n\nЕщё я читал этих авторов.\n\n")
        val authors =
            writingsIn.filter { !it.tags.contains("hidden") && !it.tags.contains("blogging") }
                .sortedBy { it.rating }.groupBy { it.authors }.keys.toMutableList()
        authors.removeAll(recommendations.keys)
        authors.removeAll(entertaining.keys)
        result.append(authors.joinToString(
            separator = ", ",
            prefix = " ",
            postfix = ".",
            transform = { it[0].names[0].name }))
        result.append("\n\n* * *")

        result.append("\n\nИнтересные тексты в Интернетах.\n\n")
        val posts =
            writingsIn.filter { it.tags.contains("entertaining") && it.tags.contains("blogging") }
                .groupBy { it.authors }
        posts.forEach { (authors, writings) ->
            result.append(" ")
            result.append(formatAuthors(authors, "en"))
            result.append(formatWritings(writings, "en"))
        }

        Utils().pagesDir.resolve("library.txt").toFile().writeText(result.toString())
    }
}