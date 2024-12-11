import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class JsonTest {
    @Serializable
    data class Name(val name: String, val language: String, val link: String? = null)

    @Test fun main() {
        val test = Name("A", "B", "https://www.paulgraham.com/identity.html")
        val format = Json { prettyPrint = true }
        assertEquals(
"""{
    "name": "A",
    "language": "B",
    "link": "https://www.paulgraham.com/identity.html"
}""",
            format.encodeToString(test))
    }
}