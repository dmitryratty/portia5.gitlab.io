import kotlin.test.Test
import kotlin.test.assertEquals

class CollectionsTest {

    @Test fun map1() {
        val a: Map<Int, String> = mapOf(1 to "AAA", 2 to "BBB", 3 to "BBB")
        assertEquals("{1=AAA, 2=BBB, 3=BBB}", a.toString())
        val b: Map<String, List<Pair<Int, String>>> = a.toList().groupBy { pair -> pair.second }
        assertEquals("{AAA=[(1, AAA)], BBB=[(2, BBB), (3, BBB)]}", b.toString())
        val c: Map<String, List<Int>> = b.mapValues { entry ->
            entry.value.map { e: Pair<Int, String> -> e.first }
        }
        assertEquals("{AAA=[1], BBB=[2, 3]}", c.toString())
    }

    @Test fun map2() {
        val a: Map<Int, String> = mapOf(1 to "AAA", 2 to "BBB", 3 to "BBB")
        assertEquals("{1=MAAA, 2=MBBB, 3=MBBB}", a.mapValues { "M${it.value}" }.toString())
        val b = mapOf(1 to listOf("AAA"), 2 to listOf("BBB"), 3 to listOf("BBB", "CCC"))
        assertEquals("{1=[AAA], 2=[BBB], 3=[BBB, CCC]}", b.toString())
        assertEquals("{1=[X-AAA], 2=[X-BBB], 3=[X-BBB, X-CCC]}",
            b.mapValues { entry -> entry.value.map { "X-${it}" } }.toString())
    }
}