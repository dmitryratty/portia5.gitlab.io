import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import com.sun.net.httpserver.HttpsServer
import java.io.File
import java.io.FileInputStream
import java.net.InetSocketAddress
import java.nio.file.Path

/**
 * TODO.
 * - Read-write on serveMap and redirectMap may create race condition errors
 *   if requests serving is multithreaded.
 */
class SimpleServer {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            SimpleServer().main()
        }
    }

    private val secure = false
    private val port = 8080
    private val homePath: Path = Utils().projectDir.resolve("public")
    private val homeFile = homePath.toFile()
    private var redirectMap = mapOf<String, String>()
    private var serveMap = mapOf<String, File>()

    fun main() {
        println("Home: $homePath")
        val server = if (secure) {
            HttpsServer.create(InetSocketAddress(port), 0)
        } else {
            HttpServer.create(InetSocketAddress(port), 0)
        }
        updateMaps()
        println(generateMap(false))
        val context = server.createContext("/")
        context.setHandler { exchange ->
            handle(exchange, false)
            // Will hang indefinitely without explicit "close()".
            exchange.responseBody.close()
        }
        server.executor = null
        println("Listening at http${if (secure) "s" else ""}://localhost:$port/")
        server.start()
    }

    fun generateMap(update: Boolean): String {
        if (update) updateMaps()
        val toServe = serveMap.mapValues { "/" + it.value.relativeTo(homeFile).path }.toSortedMap()
        val toRedirect = redirectMap.toList()
            .groupBy { pair -> pair.second }
            .mapValues { entry ->
                entry.value.map { it.first }
            }.toSortedMap()
        if (!toServe.keys.containsAll(toRedirect.keys)) throw IllegalStateException()
        val htmlList = StringBuilder()
        val otherList = StringBuilder()
        toServe.forEach {
            if (it.value.endsWith(".html")) {
                val r = toRedirect[it.key].orEmpty().toMutableList()
                if (!r.remove(it.value)) {
                    throw IllegalStateException("$it $r")
                }
                if (htmlList.isNotEmpty()) {
                    htmlList.append('\n')
                }
                if (r.isEmpty()) {
                    htmlList.append("${it.key} -> ${it.value}")
                } else {
                    htmlList.append("${it.key} -> ${it.value} <<- $r")
                }
            } else {
                if (toRedirect[it.key] != null) {
                    throw IllegalStateException()
                }
                if (otherList.isNotEmpty()) {
                    otherList.append('\n')
                }
                otherList.append("${it.key} -> ${it.value}")
            }
        }
        return "$htmlList\n$otherList"
    }

    fun updateMaps() {
        val serve = HashMap<String, File>()
        val redirect = HashMap<String, String>()
        homeFile.walk().forEach {
            if (homeFile == it) {
                return@forEach
            }
            if (it.name.startsWith(".")) throw IllegalStateException(it.toString())
            var relativePath = it.relativeTo(homeFile).path
            if (relativePath == "index.html") {
                serve["/"] = it
                redirect["/index.html"] = "/"
                return@forEach
            }
            relativePath = "/$relativePath"
            if (relativePath.endsWith(".css")
                || relativePath.endsWith(".png")
                || relativePath.endsWith(".svg")) {
                serve[relativePath] = it
                return@forEach
            }
            if (relativePath.startsWith("/a/test/")) {
                var displayUrl = relativePath.removePrefix("/a")
                if (displayUrl.endsWith("/index.html")) {
                    displayUrl = displayUrl.removeSuffix("/index.html")
                    serve[displayUrl] = it
                    redirect[relativePath] = displayUrl
                    redirect["$displayUrl/"] = displayUrl
                    redirect[relativePath.removeSuffix("index.html")] = displayUrl
                    redirect[relativePath.removeSuffix("/index.html")] = displayUrl
                } else if (displayUrl.endsWith(".html")) {
                    displayUrl = displayUrl.removeSuffix(".html")
                    serve[displayUrl] = it
                    redirect[relativePath] = displayUrl
                    redirect[relativePath.removeSuffix(".html")] = displayUrl
                }
                return@forEach
            }
            if (relativePath.endsWith("/index.html")) {
                val displayUrl = relativePath.removeSuffix("/index.html")
                serve[displayUrl] = it
                redirect[relativePath] = displayUrl
                redirect["$displayUrl/"] = displayUrl
            } else if (relativePath.endsWith(".html")) {
                val displayUrl = relativePath.removeSuffix(".html")
                serve[displayUrl] = it
                redirect[relativePath] = displayUrl
            }
        }
        serveMap = serve
        redirectMap = redirect
    }

    private fun handle(exchange: HttpExchange, mapsUpdated: Boolean) {
        println("Request: ${exchange.requestURI.path}")
        val redirectLocation = redirectMap[exchange.requestURI.path]
        if (redirectLocation != null) {
            exchange.responseHeaders["Location"] = redirectLocation
            exchange.sendResponseHeaders(302, 0)
            return
        }
        val file = serveMap[exchange.requestURI.path]
        if (file == null && !mapsUpdated) {
            updateMaps()
            handle(exchange, true)
            return
        }
        if (file != null && file.exists()) {
            if (file.name.endsWith(".svg")) {
                // Chrome don't displays SVGs without proper Content-Type. PNGs without
                // specified Content-Type displayed normally.
                exchange.responseHeaders["Content-Type"] = listOf("image/svg+xml")
            }
            exchange.sendResponseHeaders(200, file.length())
            FileInputStream(file).use {
                it.copyTo(exchange.responseBody)
            }
        } else {
            exchange.sendResponseHeaders(404, 0)
        }
    }
}