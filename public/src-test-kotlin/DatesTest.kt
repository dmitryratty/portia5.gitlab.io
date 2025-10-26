import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

class DatesTest {

    @Test fun t1() {
        val locale = Locale.forLanguageTag("ru-RU")
        // MMMM - "Января", LLLL - "Январь", и при парсинге, и при выводе.
        val formatter = DateTimeFormatterBuilder().parseCaseInsensitive()
            .appendPattern("'█['yyyy',' LLLL',' d']█'").toFormatter(locale)
        val date = LocalDate.parse("█[2023, январь, 2]█", formatter)
        val formatted = date.format(DateTimeFormatter.ofPattern("yyyy, LLLL, d", locale))
        println(formatted)
        if (!formatted.equals("2023, январь, 2")) {
            throw IllegalStateException()
        }
    }

    @Test fun t7() {
        val locale = Locale.forLanguageTag("ru-RU")
        // MMMM - "Января", LLLL - "Январь", и при парсинге, и при выводе.
        val formatter = DateTimeFormatterBuilder().parseCaseInsensitive()
            .appendPattern("'█['yyyy',' LLLL',' d',' EEEE']█'").toFormatter(locale)
        // Если вместо "понедельник" ошибочно указать например "вторник", то получим ошибку
        // парсинга в виде исключения
        val date = LocalDate.parse("█[2023, январь, 2, понедельник]█", formatter)
        val formatted = date.format(DateTimeFormatter.ofPattern("'█['yyyy, LLLL, d, EEEE']█'", locale))
        if (!formatted.equals("█[2023, январь, 2, понедельник]█")) {
            throw IllegalStateException(formatted)
        }
    }

    @Test fun t2() {
        // Mon Jan 31 23:59:59 2000 +0300
        val formatter = DateTimeFormatter.ofPattern("MMM d HH:mm:ss yyyy", Locale.US)
        val dateTime = LocalDateTime.parse("Jan 31 23:59:59 2000", formatter)
        val formatted = dateTime.format(DateTimeFormatter.ofPattern("yyyy, LLLL, d", Locale.US))
        println(formatted)
    }

    @Test fun t3() {
        // Mon Jan 31 23:59:59 2000 +0300
        val formatter = DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss yyyy xxxx", Locale.US)
        val date = LocalDate.parse("Mon Jan 31 23:59:59 2000 +0300", formatter)
        val formatted = date.format(DateTimeFormatter.ofPattern("yyyy, LLLL, d", Locale.US))
        println(formatted)
    }

    fun inRangeExclusive(start: LocalDate, end: LocalDate, date: LocalDate): Boolean {
        return date.isAfter(start) && date.isBefore(end)
    }

    fun inRangeInclusive(start: LocalDate, end: LocalDate, date: LocalDate): Boolean {
        return !(date.isBefore(start) || date.isAfter(end))
    }

    @Test fun t4() {
        println("\nt4")
        val start = LocalDate.of(2000, 1, 10)
        val end = LocalDate.of(2000, 1, 12)
        println(!inRangeExclusive(start, end, LocalDate.of(2000, 1, 9)))
        println(!inRangeExclusive(start, end, LocalDate.of(2000, 1, 10)))
        println(inRangeExclusive(start, end, LocalDate.of(2000, 1, 11)))
        println(!inRangeExclusive(start, end, LocalDate.of(2000, 1, 12)))
        println(!inRangeExclusive(start, end, LocalDate.of(2000, 1, 13)))
        println()
        println(!inRangeInclusive(start, end, LocalDate.of(2000, 1, 9)))
        println(inRangeInclusive(start, end, LocalDate.of(2000, 1, 10)))
        println(inRangeInclusive(start, end, LocalDate.of(2000, 1, 11)))
        println(inRangeInclusive(start, end, LocalDate.of(2000, 1, 12)))
        println(!inRangeInclusive(start, end, LocalDate.of(2000, 1, 13)))
    }

    @Test fun t5 () {
        println("\nt5")
        val formatter1 = DateTimeFormatter.ofPattern("d'.'MM'.'yyyy")
        val formatter2 = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        val date = LocalDate.of(2000, 1, 1)
        var formatted = date.format(formatter1)
        println(formatted)
        if (!formatted.equals("1.01.2000")) {
            throw IllegalStateException()
        }
        formatted = date.format(formatter2)
        println(formatted)
        if (!formatted.equals("01.01.2000")) {
            throw IllegalStateException()
        }
    }

    @Test fun t6 () {
        println("\nt6 GO")
        val start = LocalDate.of(2000, 1, 1)
        val end = LocalDate.of(2000, 1, 1)
        assertEquals(0L, start.until(end, ChronoUnit.DAYS))
        assertEquals(1L, start.until(end.plusDays(1), ChronoUnit.DAYS))
        println("t6 OK")
    }
}