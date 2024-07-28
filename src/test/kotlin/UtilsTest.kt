import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilsTest {
    @Test fun test() {
        assertEquals("Hello!", rattyExecSimple(Utils().currentPath.toFile(), "echo Hello!"))
        val git = rattyExecSimple(Utils().currentPath.toFile(), "git --version")
        assertTrue { git!!.startsWith("git version ") }
    }
}