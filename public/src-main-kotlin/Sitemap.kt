
import java.nio.file.Files
import java.nio.file.Path

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

    fun updateMaps() {
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
        val mapGenBuilder = StringBuilder()
        urls.filter { it.isPage }.forEach {
            if (mapGenBuilder.isNotEmpty()) mapGenBuilder.append('\n')
            mapGenBuilder.append(it.absoluteUrl)
        }
        mapOrderSrcAbsolutePath.toFile().writeText(mapGenBuilder.toString())
        _mapOrder = Page(mapOrderUri)
        pages[mapOrderUri.relativeUrl] = getMapOrder()
        mapChaosSrcAbsolutePath.toFile().writeText(mapGenBuilder.toString())
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