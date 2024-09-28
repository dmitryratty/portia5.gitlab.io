import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilsAbsoluteTest {
    @Test fun test() {
        assertEquals("Hello!", rattyExecSimple(UtilsAbsolute.currentPath.toFile(), "echo Hello!"))
        val git = rattyExecSimple(UtilsAbsolute.currentPath.toFile(), "git --version")
        assertTrue { git!!.startsWith("git version ") }
    }
}