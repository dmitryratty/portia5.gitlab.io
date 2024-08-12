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
        val resourcesDir = Utils().resourcesDir
        val writingsIn : MutableList<Writing> = arrayListOf()
        resourcesDir.listDirectoryEntries("library*.json").forEach {
            writingsIn.addAll(Json.decodeFromString<List<Writing>>(it.toFile().readText()))
        }
        printCount(writingsIn)

        val format = Json { prettyPrint = true }

        val articlesToSave = writingsIn
            .filter { it.tags.contains("essay") || it.tags.contains("blogging") }
        val outArticleFile = resourcesDir.resolve("library-article.json").toFile()
        outArticleFile.writeText(format.encodeToString(articlesToSave))

        val othersToSave = writingsIn
            .filter { !it.tags.contains("essay") && !it.tags.contains("blogging") }
        val outOtherFile = resourcesDir.resolve("library-other.json").toFile()
        outOtherFile.writeText(format.encodeToString(othersToSave))

        val result = StringBuilder("\uD83D\uDCDA Library." +
                " Элиезер Юдковский, Грег Иган, Тед Чан, Питер Уоттс, Эмили Нагоски." +
                " Несколько сотен прочитанной художки.\n\n")
        val builder = StringBuilder()

        result.append("Штуки, которые могу порекомендовать.\n\n")
        val recommendations = writingsIn
            .filter { it.tags.contains("recommendation") }
            .sortedBy { it.rating }
            .groupBy { it.authors }
        builder.clear()
        recommendations.forEach { (authors, writings) ->
            if (builder.isNotEmpty()) {
                builder.append(" ")
            }
            builder.append(formatAuthors(authors, "ru"))
            builder.append(formatWritings(writings, "ru"))
        }
        result.append(builder)

        val entertaining = writingsIn
            .filter { it.tags.contains("entertaining") && !it.tags.contains("blogging") }
            .sortedBy { it.rating }
            .groupBy { it.authors }
        if (entertaining.isNotEmpty()) {
            result.append("\n\n* * *")
            result.append("\n\nПросто забавные штуки.\n\n")
            builder.clear()
            entertaining.forEach { (authors, writings) ->
                if (builder.isNotEmpty()) {
                    builder.append(" ")
                }
                builder.append(formatAuthors(authors, "ru"))
                builder.append(formatWritings(writings, "ru"))
            }
            result.append(builder)
        }

        result.append("\n\n* * *")
        result.append("\n\nИнтересные тексты в Интернетах.\n\n")
        val posts = writingsIn
            .filter { it.tags.contains("entertaining") && it.tags.contains("blogging") }
            .sortedBy { it.rating }
            .groupBy { it.authors }
        builder.clear()
        posts.forEach { (authors, writings) ->
            if (builder.isNotEmpty()) {
                builder.append(" ")
            }
            builder.append(formatAuthors(authors, "en"))
            builder.append(formatWritings(writings, "en"))
        }
        result.append(builder)

        result.append("\n\n* * *")
        result.append("\n\nЕщё я читал этих авторов.\n\n")
        val authors = writingsIn
            .filter { !it.tags.contains("hidden") && !it.tags.contains("blogging") }
            .sortedBy { it.rating }
            .groupBy { it.authors }
            .keys.toMutableList()
        authors.removeAll(recommendations.keys)
        authors.removeAll(entertaining.keys)
        result.append(authors.joinToString(
            separator = ", ",
            prefix = "",
            postfix = ".",
            transform = { it[0].names[0].name }))

        result.append("\n\n* * *")
        result.append("\n\nOther. \"Juuni Taisen\", \"Bokurano\", \"Psycho-Pass\"," +
                " \"Ghost in the Shell\", \"The Saga of Tanya the Evil\", \"Blame!\"," +
                " \"Akame ga Kill!\".")

        Utils().pagesSrcDir.resolve("library/index.txt").toFile()
            .writeText(result.toString())
    }
}