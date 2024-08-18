import java.util.*

data class Page(val path: String, val raw: String) {
    var includeShortText: String? = null
    var includeFullText: String? = null
    var includeText: String = ""
    lateinit var beautyfiedText: String
    val bottomNavigation = path != "index.txt"

    private var _title: String? = null
    val title: String
        get() {
            initializeTitle()
            return _title ?: throw IllegalStateException()
        }
    private fun initializeTitle() {
        if (_title != null) {
            return
        }
        if (path == "index.txt") {
            _title = "Well… Yes!"
            return
        }
        var name = if (path.endsWith("/index.txt")) {
            path.removeSuffix("/index.txt")
        } else {
            path.removeSuffix(".txt")
        }
        if (name.contains('/')) {
            _title = "Well… \"$name\"!"
        } else {
            name = name.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString()
            }
            _title = "Well… $name!"
        }
    }
}