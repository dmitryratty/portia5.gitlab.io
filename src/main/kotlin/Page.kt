import java.util.*
import kotlin.io.path.readText

data class Page(val url: RatUrl) {
    val raw: String = url.srcAbsolutePath.readText()
    val supsecs = mutableListOf<MutableList<MutableList<String>>>()

    var summaryParag: String? = null
    var summarySection: MutableList<String>? = null
    var summaryMax: MutableList<String>? = null

    var includeText: String = ""
    var beautyText: String = ""

    val navigation = !url.isRoot
    val htmlOutFile = url.dstAbsolutePath

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
        if (url.isRoot) {
            _title = "Well… Yes!"
            return
        }
        var name = url.relativeUrl.removePrefix("/")
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