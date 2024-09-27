import java.nio.file.Path

interface Context {
    val srcTxtDir: Path
    val srcRawDir: Path
    val srcResDir: Path
    val srcGenDir: Path
    val dstMainDir: Path
    val dstTestDir: Path
}