package archive.factorio

import java.math.RoundingMode
import kotlin.math.ceil

/**
 * How is more effective to produce petroleum.
 *  90 / 100 = 0.9 petroleum for 1 crude oil.
 *
 *  20 * (30 / 40) = 15 light oil from heavy oil.
 *  (70 + 15) * (20 / 30) = 56.6
 *  30 + 56.6 = 86.6 - petroleum from advanced processing, so simple processing gives more
 *  petroleum.
 */
class AdvancedOilProcessing {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            AdvancedOilProcessing().main()
        }
    }

    fun main() {
        calc(1040, true, true, true)
    }

    private fun round2(d: Double): String {
        val res = d.toBigDecimal().setScale(1, RoundingMode.HALF_UP).toString()
        if (!res.endsWith("0")) {
            return res
        }
        for (i in 2..10) {
            val temp = d.toBigDecimal().setScale(i, RoundingMode.HALF_UP).toString()
            if (!temp.endsWith("0")) {
                return temp
            }
        }
        return res
    }

    private fun round(d: Double): String {
        return ceil(d).toInt().toString()
    }

    private fun calc(crudeIn: Int,
                     crudeProductivity: Boolean,
                     heavyToLightProductivity: Boolean,
                     lightToGasProductivity: Boolean) {
        val water = (crudeIn * (50.0 / 100.0))
        val gas = (crudeIn * (55.0 / 100.0)) * if (crudeProductivity) 1.3 else 1.0
        val light = (crudeIn * (45.0 / 100.0)) * if (crudeProductivity) 1.3 else 1.0
        val heavy = (crudeIn * (25.0 / 100.0)) * if (crudeProductivity) 1.3 else 1.0

        println("Processing $crudeIn crude oil per second" +
                " with productivity ${if (crudeProductivity) "+30%" else "+0%"}" +
                " consuming ${round(water)} water and producing" +
                " ${round(gas)} gas, ${round(light)} light, ${round(heavy)} heavy.")

        val lightFromHeavy = (heavy * (30.0 / 40.0)) * if (heavyToLightProductivity) 1.3 else 1.0
        val lightFromHeavyWater = (heavy * (30.0 / 40.0))
        val totalLight = light + lightFromHeavy
        println("${round(heavy)} heavy converted to ${round(lightFromHeavy)} light," +
                " consuming ${round(lightFromHeavyWater)} water," +
                " with productivity ${if (heavyToLightProductivity) "+30%" else "+0%"}," +
                " ${round(totalLight)} total light," +
                " ${round(water + lightFromHeavyWater)} total water.")

        val gasFromLight = (totalLight * (20.0 / 30.0)) * if (lightToGasProductivity) 1.3 else 1.0
        val gasFromLightWater = (totalLight * (30.0 / 30.0))
        println("${round(totalLight)} light converted to ${round(gasFromLight)} gas," +
                " consuming ${round(gasFromLightWater)} water," +
                " with productivity ${if (lightToGasProductivity) "+30%" else "+0%"}," +
                " ${round(gas + gasFromLight)} total gas," +
                " ${round(water + lightFromHeavyWater + gasFromLightWater)} total water.")
        println("${round(lightFromHeavyWater + gasFromLightWater)} water for light and gas.")

        // TODO. Modules.
        println()
        println("Light to solid fuel: ${round((totalLight / 11.0) * 10.0)}," +
                " light to rocket fuel: ${round((totalLight / 11.0) * 1.0)}")

        println()
        val solidFuelFromGas = solidFuelFromGas(100_000.0, true)
        val lightBufferToUnblockPipeline = round(lightToProduceRocketFuel(solidFuelFromGas, true))
        println("Чтобы производство ракетного топлива из дизеля могло переключиться" +
                " на нефтяной газ и дизель, поглотив из системы избыток нефтяного газа," +
                " блокирующего производство дизеля, при вместимости поезда в 100000" +
                " нефтяного газа, нужно $lightBufferToUnblockPipeline дизеля.")
    }

    private fun lightToProduceRocketFuel(solidFuel: Double, productivity: Boolean): Double {
        return solidFuel / 10.0
    }

    private fun solidFuelFromGas(gas: Double, productivity: Boolean): Double {
        val gasPerSolidFuel = 20.0
        return gas / gasPerSolidFuel + gas / gasPerSolidFuel * if (productivity) 0.3 else 0.0
    }

    private fun oilProcessing() {
        // 100 crude oil + 50 water = 25 heavy oil, 45 light oil, 55 petroleum gas
        val oil = 750
        val heavy = (oil / 100F) * 25
        // 40 heavy oil + 30 water = 30 light oil
        val lightFromHeavy = (heavy / 40F) * 30
        val light = (oil / 100F) * 45 + lightFromHeavy
        // 30 light oil + 30 water = 20 petroleum gas
        val gasFromLight = (light / 30F) * 20
        val gas = (oil / 100F) * 55 + gasFromLight
        print("Oil: $oil, heavy: $heavy, light: $light, gas: $gas.")
    }
}