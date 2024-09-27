import java.nio.file.Path

class ContextImpl(
    override val srcTxtDir: Path = Utils.srcTxtDir,
    override val srcRawDir: Path = Utils.srcRawDir,
    override val srcResDir: Path = Utils.srcResDir,
    override val srcGenDir: Path = Utils.srcGenDir,
    override val dstMainDir: Path = Utils.dstMainDir,
    override val dstTestDir: Path = Utils.dstTestDir) : Context