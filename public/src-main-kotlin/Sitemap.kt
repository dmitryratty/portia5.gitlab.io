
import java.nio.file.Files
import java.nio.file.Path

class Sitemap(srcDirsPaths: Set<Path>, dstDirPath: Path) {

    val urls: List<RatUrl>
    val mapSrcAbsolutePath = Utils.srcGenDir.resolve("sitemap.txt")

    init {
        val urlsList = ArrayList<RatUrl>()
        srcDirsPaths.forEach { srcDirPath ->
            Files.walk(srcDirPath).use { stream ->
                stream.filter(Files::isRegularFile).forEach {
                    urlsList.add(RatUrl(it, srcDirPath.relativize(it), dstDirPath))
                }
            }
        }
        val mapSrcRelativePath = Utils.srcGenDir.relativize(mapSrcAbsolutePath)
        val mapUri = RatUrl(mapSrcAbsolutePath, mapSrcRelativePath, Utils.dstDir)
        if (!urlsList.contains(mapUri)) {
            urlsList.add(mapUri)
        }
        urls = urlsList.sortedBy { it.absoluteUrl }
    }

    fun main() {
        val map = StringBuilder()
        urls.filter { it.isPage }.forEach {
            if (map.isNotEmpty()) map.append('\n')
            map.append(it.absoluteUrl)
        }
        mapSrcAbsolutePath.toFile().writeText(map.toString())

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
        Utils.testGenDir.resolve("sitemap-full.txt").toFile()
            .writeText(mapFull.toString())
    }
}