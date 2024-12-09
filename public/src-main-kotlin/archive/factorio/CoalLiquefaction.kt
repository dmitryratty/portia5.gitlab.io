package archive.factorio

import java.math.RoundingMode
import kotlin.math.ceil
import kotlin.math.roundToInt

class CoalLiquefaction {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            CoalLiquefaction().main()
        }
    }

    fun main() {
        coalLiquefactionEnergy()
        coalLiquefactionPlants()
    }

    private fun coalLiquefactionEnergy() {
        // Сжижение угля. Просто жечь один экспресс-конвейер угля: получаем 180 MW.
        // Сжижать уголь, делать твёрдое топливо, жечь его: получаем 360 MW.
        val inputJoule = coalToJoule(10) + heavyOilToJoule(25) + steamCostInJoule(50) +
                oilRefineryWorkInJoule(5)
        val outputJoule = heavyOilToJoule(90) + lightOilToJoule(20) + petroleumGasToJoule(10)
        println(
            "Coal liquefaction input ${printJoules(inputJoule)}," +
                    " output ${printJoules(outputJoule)}."
        )
        // Coal liquefaction input 65085 kJ, output 106009 kJ.
    }

    private fun petroleumGasToJoule(i: Int): Double {
        // 20 petroleum gas = 1 solud fuel via 2 seconds in chemical plant
        return i * ((soludFuelToJoule(1) - chemicalPlantWorkCostInJoule(2)) / 20.0)
    }

    private fun lightOilToJoule(i: Int): Double {
        // 10 light oil = 1 solud fuel via 2 seconds in chemical plant
        return i * ((soludFuelToJoule(1) - chemicalPlantWorkCostInJoule(2)) / 10.0)
    }

    private fun heavyOilToJoule(i: Int): Double {
        // 40 heavy oil = 30 light oil via 2 seconds in chemical plant
        return i * ((lightOilToJoule(30) - chemicalPlantWorkCostInJoule(2)) / 40.0)
    }

    private fun oilRefineryWorkInJoule(s: Int): Int = s * 434_000

    private fun chemicalPlantWorkCostInJoule(s: Int): Int = s * 217_000

    private fun steamCostInJoule(i: Int): Double = i * (1_800_000 / 60.0)

    private fun soludFuelToJoule(i: Int): Int = i * 12_000_000

    private fun coalToJoule(i: Int): Int = i * 4_000_000

    private fun printJoules(j: Double): String = (j / 1000.0).roundToInt().toString() + " kJ"

    private fun coalLiquefactionPlants() {
        // Если ставить 15 Chemical plant for cracking Heavy oil to Light oil,
        // то при моей схеме постройки Oil refinery остаются без Heavy oil.
        // ((25 / 5) * 22.5) / (90 / 5) = 6.25 Oil refinery на снабжение 22.5 Oil refinery,
        // не целое число, неудобно для постройки.
        //coalLiquefactionPlant1("Transport belt", 15)
        //coalLiquefactionPlant1("Fast transport belt", 30)
        //coalLiquefactionPlant1("Express transport belt", 45)
        coalLiquefactionPlant2("Transport belt", 15)
        coalLiquefactionPlant2("Fast transport belt", 30)
        coalLiquefactionPlant2("Express transport belt", 45)
    }

    private fun coalLiquefactionPlant1(belt: String, coal: Int) {
        val refineries = coal / (10 / 5.0)
        val boilers = ((50 / 5.0) * refineries) / 60.0
        val plantsLightToHeavy = (((90 - 25) / 5.0) * refineries) / (40 / 2.0)
        val lightPerSecond = ((20 / 5.0) * refineries) + ((30 / 2.0) * plantsLightToHeavy)
        val plantsLightToFuel = lightPerSecond / (10 / 2.0)
        val plantsPetroleumToFuel = ((10 / 5.0) * refineries) / (20 / 2.0)

        val fuelPerSecondForBoilers = (boilers * 1.8) / 12
        val fuelPerSecondToBurn =
            ((plantsLightToFuel + plantsPetroleumToFuel) / 2) - fuelPerSecondForBoilers
        val boilersForElectricity = (fuelPerSecondToBurn * 12) / 1.8
        println(
            "For 1 $belt with $coal coal per second we need:"
                    + " ${ceil(refineries).toInt()} ($refineries) Oil refinery"
                    + ", ${ceil(boilers).toInt()} ($boilers) Boiler"
                    + ", ${ceil(plantsLightToHeavy).toInt()} ($plantsLightToHeavy)"
                    + " Chemical plant for cracking Heavy oil to Light oil"
                    + ", ${ceil(plantsLightToFuel).toInt()} ($plantsLightToFuel)"
                    + " Chemical plant for Light oil to Solid fuel"
                    + ", ${ceil(plantsPetroleumToFuel).toInt()} ($plantsPetroleumToFuel)"
                    + " Chemical plant for Petroleum gas to Solid fuel"
                    + ", ${ceil(boilersForElectricity).toInt()} ($boilersForElectricity)"
                    + " Boiler for Steam turbine."
        )
    }

    private fun round(d: Double): String {
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

    private fun coalLiquefactionPlant2(belt: String, coal: Int) {
        val coalPerSecondForBoiler = 1.8 / 4
        val coalPerSecondForSteam = coalPerSecondForBoiler / 60
        val refineries = coal / ((10 + (coalPerSecondForSteam * 50)) / 5.0)
        val refineriesSelfHeavy = ((25 / 5.0) * refineries) / (90 / 5.0)
        val boilersForRefineries = ((50 / 5.0) * refineries) / 60.0
        val plantsLightToHeavy = (((90 - 25) / 5.0) * refineries) / (40 / 2.0)
        val lightPerSecond = ((20 / 5.0) * refineries) + ((30 / 2.0) * plantsLightToHeavy)
        val plantsLightToFuel = lightPerSecond / (10 / 2.0)
        val plantsPetroleumToFuel = ((10 / 5.0) * refineries) / (20 / 2.0)

        val fuelPerSecondToBurn = (plantsLightToFuel + plantsPetroleumToFuel) / 2.0
        val boilersForTurbines = (fuelPerSecondToBurn * 12) / 1.8
        println(
            "To consume 1 $belt with $coal coal per second we need:"
                    + " ${ceil(refineries).toInt()} (${round(refineries)}) Oil refinery"
                    + " with ${ceil(boilersForRefineries).toInt()} (${round(boilersForRefineries)})"
                    + " Boiler burning part of coal input"
                    + " and ${ceil(refineriesSelfHeavy).toInt()} (${round(refineriesSelfHeavy)})"
                    + " Oil refinery is sufficient to supply all Oil refinery with Heavy oil"
                    + ", ${ceil(plantsLightToHeavy).toInt()} (${round(plantsLightToHeavy)})"
                    + " Chemical plant for Heavy oil to Light oil"
                    + ", ${ceil(plantsLightToFuel).toInt()} (${round(plantsLightToFuel)})"
                    + " Chemical plant for Light oil to Solid fuel"
                    + ", ${ceil(plantsPetroleumToFuel).toInt()} (${round(plantsPetroleumToFuel)})"
                    + " Chemical plant for Petroleum gas to Solid fuel"
                    + ", ${ceil(boilersForTurbines).toInt()} (${round(boilersForTurbines)})"
                    + " Boiler for Steam turbine."
        )
    }
}