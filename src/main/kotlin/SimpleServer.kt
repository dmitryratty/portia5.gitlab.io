import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import com.sun.net.httpserver.HttpsServer
import java.io.File
import java.io.FileInputStream
import java.net.InetSocketAddress
import java.nio.file.Path

class SimpleServer {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            SimpleServer().main()
        }
    }

    data class DirMap(val dirPath: Path) {
        private val dirFile: File = dirPath.toFile()
        private val redirect = mutableMapOf<String, String>()
        private val serve = mutableMapOf<String, File>()
        init {
            dirFile.walk().forEach {
                if (dirFile == it || it.name.startsWith(".")) {
                    return@forEach
                }
                var relativePath = it.relativeTo(dirFile).path
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
        }

        override fun toString(): String {
            return toList().sorted().joinToString("\n")
        }

        fun toList(): List<String> {
            val toServe = serve.mapValues { "/" + it.value.relativeTo(dirFile).path }
            val toRedirect = redirect.toList()
                .groupBy { pair -> pair.second }
                .mapValues { entry: Map.Entry<String, List<Pair<String, String>>> ->
                    entry.value.map { e: Pair<String, String> -> e.first }
                }
            if (!toServe.keys.containsAll(toRedirect.keys)) throw IllegalStateException()
            val result = mutableListOf<String>()
            toServe.forEach {
                if (it.value.endsWith(".html")) {
                    val r = toRedirect[it.key].orEmpty().toMutableList()
                    if (r.isEmpty()) throw IllegalStateException()
                    if (r.remove("${it.key}.html")) {
                        if (r.isNotEmpty()) throw IllegalStateException()
                        if ("${it.key}.html" != it.value) throw IllegalStateException()
                        result.add("[4] ${it.key}[.html]")
                        return@forEach
                    }
                    var display = it.key
                    if (r.remove("${it.key}/")) {
                        display = "$display[/]"
                    }
                    if (r.remove("${it.key}/index.html")) {
                        display = "$display[/index.html]"
                    }
                    if (r.isEmpty()) {
                        if ("${it.key}/index.html" != it.value) throw IllegalStateException()
                        result.add("[2] $display")
                    } else {
                        result.add("[1] $display -> ${it.value} R: $r")
                    }
                } else {
                    if (toRedirect[it.key] != null) throw IllegalStateException()
                    if (it.key != it.value) throw IllegalStateException()
                    result.add("[3] ${it.key}")
                }
            }
            return result
        }

        fun handle(exchange: HttpExchange): Boolean {
            val redirectResult = redirect[exchange.requestURI.path]
            if (redirectResult != null) {
                exchange.responseHeaders["Location"] = redirectResult
                exchange.sendResponseHeaders(302, 0)
                return true
            }
            val serveResult = serve[exchange.requestURI.path]
            if (serveResult != null && serveResult.exists()) {
                if (serveResult.name.endsWith(".svg")) {
                    // Chrome don't displays SVGs without proper Content-Type. PNGs without
                    // specified Content-Type displayed normally.
                    exchange.responseHeaders["Content-Type"] = listOf("image/svg+xml")
                }
                exchange.sendResponseHeaders(200, serveResult.length())
                FileInputStream(serveResult).use {
                    it.copyTo(exchange.responseBody)
                }
                return true
            }
            return false
        }
    }

    private val secure = false
    private val port = 8080
    private val mainPath: Path = Utils().projectDir.resolve("public")
    private var mainDirMap = DirMap(mainPath)
    private val develPath = Utils().resourcesDir.resolve("other")
    private var develDirMap = DirMap(develPath)

    fun main() {
        println("Home: $mainPath")
        val server = if (secure) {
            HttpsServer.create(InetSocketAddress(port), 0)
        } else {
            HttpServer.create(InetSocketAddress(port), 0)
        }
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

    private fun handle(exchange: HttpExchange, mapsUpdated: Boolean) {
        println("Request: ${exchange.requestURI.path}")
        if (!develDirMap.handle(exchange)) {
            if (!mainDirMap.handle(exchange)) {
                if (mapsUpdated) {
                    exchange.sendResponseHeaders(404, 0)
                } else {
                    mainDirMap = DirMap(mainPath)
                    develDirMap = DirMap(develPath)
                    handle(exchange, true)
                }
            }
        }
    }

    fun generateMap(): String {
        val mainMap = mainDirMap.toList()
        val develMap = develDirMap.toList()
        if (!mainMap.containsAll(develMap)) {
            throw IllegalStateException("$mainDirMap\n\n$develDirMap")
        }
        return mainMap.sorted().joinToString("\n")
    }
}