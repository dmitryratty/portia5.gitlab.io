package image

import java.io.File
import java.io.FileFilter

class Utils {
    companion object {
        fun getDigitsSuffix(s: String): String {
            val builder = StringBuilder()
            for (i in s.length - 1 downTo 0) {
                val c = s[i]
                if (!c.isDigit()) {
                    break
                }
                builder.append(c)
            }
            if (builder.length == s.length) {
                return "";
            }
            return builder.reverse().toString()
        }

        fun stripSuffix(s: String, suffix: String): String {
            if (s.isEmpty() || suffix.isEmpty()) {
                return s
            }
            return if (s.endsWith(suffix)) {
                s.substring(0, s.length - suffix.length)
            } else s
        }

        fun naturalOrderCompare(x: String, y: String): Int {
            if (x.isEmpty() && y.isEmpty()) {
                return 0
            }
            if (x.isEmpty()) {
                return 1
            }
            if (y.isEmpty()) {
                return -1
            }
            val suffixX = getDigitsSuffix(x)
            val suffixY = getDigitsSuffix(y)
            val bodyX = stripSuffix(x, suffixX)
            val bodyY = stripSuffix(y, suffixY)
            val compareBody = compareValues(bodyX, bodyY)
            if (compareBody != 0) {
                return compareBody
            }
            return compareValues(suffixX.toIntOrNull(), suffixY.toIntOrNull())
        }

        fun listFilesNaturalOrder(dir: File, filter: FileFilter): List<File> {
            val unsorted = dir.listFiles(filter)
            return unsorted!!.sortedWith(Comparator { f1, f2 ->
                return@Comparator naturalOrderCompare(f1.nameWithoutExtension, f2.nameWithoutExtension)
            })
        }
    }
}