import kotlin.test.Test

class LibraryTest {

    @Test fun run1() {
        val library = Library()
        val res = UtilsAbsolute.testResDir.resolve("library")
        val writings = library.loadWritings(res)
        writings.forEach { println(it) }
        library.writeWritings(res, writings)
    }

}