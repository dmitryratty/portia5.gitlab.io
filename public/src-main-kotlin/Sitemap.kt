
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class Sitemap(c: Context) : Context by c {
    val srcDirsPaths: Set<Path> = setOf(srcTxtDir, srcRawDir, srcGenDir)
    val urls: MutableList<RatUrl> = mutableListOf()
    val pages: MutableMap<String, Page> = mutableMapOf()
    private var _map: Page? = null
    fun getMap(): Page { return _map!! }
    private var _mapOrder: Page? = null
    fun getMapOrder(): Page { return _mapOrder!! }
    private var _mapChaos: Page? = null
    fun getMapChaos(): Page { return _mapChaos!! }

    fun updateUrls() {
        urls.clear()
        srcDirsPaths.forEach { srcDirPath ->
            Files.walk(srcDirPath).use { stream ->
                stream.filter(Files::isRegularFile).forEach {
                    urls.add(RatUrl(it, srcDirPath.relativize(it), dstMainDir))
                }
            }
        }
        urls.sortBy { it.relativeUrl }
        pages.clear()
        pages.putAll(urls.filter { !it.isRaw }.associate { it.relativeUrl to Page(it) })
    }

    fun genStep(map: SortedMap<String, TreeSet<String>>,
                url: String, suburls: TreeSet<String>?, level: Int): String {
        val builder = StringBuilder()
        if (level > 0) builder.append('\n')
        for (i in 1..level) {
            builder.append("    ")
        }
        if (level > 0) builder.append("$level ")
        builder.append(url)
        suburls?.forEach { builder.append(genStep(map, it, map[it], level + 1)) }
        map.remove(url)
        return builder.toString()
    }

    fun genOrder(map: SortedMap<String, TreeSet<String>>): String {
        val roots = setOf(urls.find { it.isRoot }!!.absoluteUrl)
        val builder = StringBuilder()
        roots.forEach {
            if (builder.isNotEmpty()) builder.append("\n")
            builder.append(genStep(map, it, map[it], 0))
        }
        return builder.toString()
    }

    fun genChaos(map: SortedMap<String, TreeSet<String>>): String {
        val roots = map.keys.toSet()
        val builder = StringBuilder()
        roots.forEach {
            if (builder.isNotEmpty()) builder.append("\n")
            builder.append(genStep(map, it, map[it], 0))
        }
        return builder.toString()
    }

    fun updateMaps(mapOfLinks: SortedMap<String, TreeSet<String>>) {
        // Recreate map page to allow it regeneration in reflective phase.
        val mapUrl = urls.find { it.relativeUrl == RelativeUtils.MAP_RELATIVE_URL }!!
        _map = Page(mapUrl)
        pages[RelativeUtils.MAP_RELATIVE_URL] = getMap()

        val mapOrderSrcRelativePath = Path.of(RelativeUtils.MAP_ORDER_RELATIVE_PATH)
        val mapOrderSrcAbsolutePath: Path = srcGenDir.resolve(mapOrderSrcRelativePath)
        val mapOrderUri = RatUrl(mapOrderSrcAbsolutePath, mapOrderSrcRelativePath, dstMainDir)
        if (urls.contains(mapOrderUri)) throw IllegalStateException()
        urls.add(mapOrderUri)
        val mapChaosSrcRelativePath = Path.of(RelativeUtils.MAP_CHAOS_RELATIVE_PATH)
        val mapChaosSrcAbsolutePath: Path = srcGenDir.resolve(mapChaosSrcRelativePath)
        val mapChaosUri = RatUrl(mapChaosSrcAbsolutePath, mapChaosSrcRelativePath, dstMainDir)
        if (urls.contains(mapChaosUri)) throw IllegalStateException()

        urls.sortBy { it.relativeUrl }
        urls.forEach {
            if (it.isRaw && it.dstRelativePathString.endsWith(".html")) {
                mapOfLinks[it.absoluteUrl] = null
            }
            if (!it.isRaw && !mapOfLinks.contains(it.absoluteUrl)) {
                mapOfLinks[it.absoluteUrl] = null
            }
        }



        mapOrderSrcAbsolutePath.toFile().writeText(genOrder(mapOfLinks))
        _mapOrder = Page(mapOrderUri)
        pages[mapOrderUri.relativeUrl] = getMapOrder()
        mapChaosSrcAbsolutePath.toFile().writeText(genChaos(mapOfLinks))
        _mapChaos = Page(mapChaosUri)
        pages[mapChaosUri.relativeUrl] = getMapChaos()
        testMap()
    }

    private fun testMap() {
        val mapFull = StringBuilder()
        urls.filter { it.isDirectory }.forEach {
            if (mapFull.isNotEmpty()) mapFull.append('\n')
            mapFull.append("[1] ${it.relativeUrl} ${it.redirects}")
        }
        urls.filter { !it.isPage }.forEach {
            if (it.redirects.isNotEmpty()) throw IllegalStateException()
            if (mapFull.isNotEmpty()) mapFull.append('\n')
            mapFull.append("[2] ${it.relativeUrl}")
        }
        urls.filter { it.isPage && !it.isDirectory }.forEach {
            if (mapFull.isNotEmpty()) mapFull.append('\n')
            if (it.redirects.size != 1) throw IllegalStateException()
            if (it.redirects.first() != "${it.relativeUrl}.html") throw IllegalStateException()
            mapFull.append("[3] ${it.relativeUrl}[.html]")
        }
        dstTestDir.resolve("sitemap-full.txt").toFile()
            .writeText(mapFull.toString())
    }
}