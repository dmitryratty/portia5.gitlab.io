import kotlin.io.path.Path
import kotlin.io.path.name

class ProcessLifecycle {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            ProcessLifecycle().main()
        }
    }

    val workingDir = Path(System.getProperty("user.dir"))

    private fun main() {
        if (workingDir.name != "ratty-den") {
            throw IllegalStateException("Illegal working dir $workingDir")
        }
        howToStopProcess()
    }

    fun howToStopProcess() {
        // Интерфейс Интеллиджи даёт кнопки остановки процесса, который мы из Интеллиджи запустили.
        // Если выходить из Интеллиджи пока процесс ещё запущен, то спрашивает, что делать,
        // остановить процесс или отцепиться от него. Если выбрать отцепление, то позже можно убить
        // процесс через механизмы управления процессами ОС.
        val thread = Thread {
            while (true) {
                println("Thread is alive.")
                workingDir.resolve("ratty-temp/${System.currentTimeMillis()}").toFile().createNewFile()
                Thread.sleep(1000)
            }
        }
        thread.start()
    }
}