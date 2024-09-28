import java.nio.file.Path

class Context(
    override val srcTxtDir: Path = UtilsAbsolute.srcTxtDir,
    override val srcRawDir: Path = UtilsAbsolute.srcRawDir,
    override val srcResDir: Path = UtilsAbsolute.srcResDir,
    override val srcGenDir: Path = UtilsAbsolute.srcGenDir,
    override val dstMainDir: Path = UtilsAbsolute.dstMainDir,
    override val dstTestDir: Path = UtilsAbsolute.dstTestDir) : ContextInterface