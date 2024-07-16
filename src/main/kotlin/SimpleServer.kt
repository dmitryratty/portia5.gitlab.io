import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import com.sun.net.httpserver.HttpsServer
import java.io.File
import java.io.FileInputStream
import java.net.InetSocketAddress
import java.nio.file.Path

/**
 * TODO.
 * - Read-write on redirectMap and serveMap may create race condition errors
 * if requests serving is multithreaded.
 */
class SimpleServer {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            SimpleServer().main()
        }
    }

    private val defaultPort = 8080
    private val defaultHome: Path = Utils().projectDir.resolve("public")
    private var redirectMap = mapOf<String, String>()
    private var serveMap = mapOf<String, File>()

    fun main() {
        start(defaultPort, defaultHome, false)
    }

    private fun updateMaps(home: Path) {
        val redirect = HashMap<String, String>()
        val serve = HashMap<String, File>()
        val homeFile = home.toFile()
        homeFile.walk().forEach {
            if (homeFile == it) {
                return@forEach
            }
            val name = it.name
            if (name.startsWith(".")) throw IllegalStateException(it.toString())
            var relativePath = it.relativeTo(home.toFile()).path
            if (relativePath == "index.html") {
                serve["/"] = it
                return@forEach
            }
            relativePath = "/$relativePath"
            if (relativePath.endsWith("/index.html")) {
                val stripped = relativePath.removeSuffix("/index.html")
                serve[stripped] = it
                redirect["$stripped/"] = stripped
            } else if (name.endsWith(".html")) {
                serve[relativePath] = it
                serve[relativePath.removeSuffix(".html")] = it
            } else if (name.endsWith(".css") || name.endsWith(".png") || name.endsWith(".svg")) {
                serve[relativePath] = it
            }
        }
        redirectMap = redirect
        serveMap = serve
    }

    fun start(port: Int, home: Path, https: Boolean) {
        println("Home: $home")
        val server = if (https) {
            HttpsServer.create(InetSocketAddress(port), 0)
        } else {
            HttpServer.create(InetSocketAddress(port), 0)
        }
        updateMaps(home)
        val context = server.createContext("/")
        context.setHandler { exchange ->
            println("Request: ${exchange.requestURI.path}")
            val redirectLocation = redirectMap[exchange.requestURI.path]
            if (redirectLocation != null) {
                exchange.responseHeaders["Location"] = redirectLocation
                exchange.sendResponseHeaders(302, 0)
            } else {
                var file = serveMap[exchange.requestURI.path]
                if (file == null) {
                    updateMaps(home)
                    file = serveMap[exchange.requestURI.path]
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
            // Will hang indefinitely without explicit "close()".
            exchange.responseBody.close()
        }
        server.executor = null
        println("Listening at http${if (https) "s" else ""}://localhost:$port/")
        server.start()
    }
}