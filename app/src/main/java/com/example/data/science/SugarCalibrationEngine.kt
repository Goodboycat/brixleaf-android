package com.example.data.science

import com.example.data.model.PlantFood
import com.example.data.model.SugarPhaseState
import com.example.data.model.SugarUnit
import kotlin.math.exp
import kotlin.math.pow

data class TitrationPoint(
    val densityRatio: Double, // 0.0 to 2.0 of optimal
    val brix: Double,
    val palatabilityScore: Double, // 0.0 to 100.0
    val bitternessMaskedPct: Double // 0.0 to 100.0
)

data class SugarCalibrationResult(
    val food: PlantFood,
    val servingGrams: Double,
    val densityMultiplier: Double,
    val idealAddedSugarGrams: Double,
    val currentAddedSugarGrams: Double,
    val currentSugarMilligrams: Double,
    val currentSugarMicrograms: Double,
    val currentTeaspoons: Double,
    val totalMillimoles: Double,
    val molarityMicromolesPerMl: Double,
    val brixDensity: Double,
    val solutionDensityGPerCm3: Double,
    val receptorBalanceRatio: Double,
    val massPerGramMg: Double,
    val phaseState: SugarPhaseState,
    val palatabilityScore: Double,
    val titrationCurve: List<TitrationPoint>
) {
    fun valueForUnit(unit: SugarUnit): Double = when (unit) {
        SugarUnit.GRAMS -> currentAddedSugarGrams
        SugarUnit.TEASPOONS -> currentTeaspoons
        SugarUnit.MILLIGRAMS -> currentSugarMilligrams
        SugarUnit.MICROGRAMS -> currentSugarMicrograms
        SugarUnit.MICROMOLAR -> molarityMicromolesPerMl
        SugarUnit.BRIX -> brixDensity
    }

    fun idealValueForUnit(unit: SugarUnit): Double {
        val ratio = if (currentAddedSugarGrams > 0.0) idealAddedSugarGrams / currentAddedSugarGrams else 1.0
        return when (unit) {
            SugarUnit.GRAMS -> idealAddedSugarGrams
            SugarUnit.TEASPOONS -> idealAddedSugarGrams / 4.2
            SugarUnit.MILLIGRAMS -> idealAddedSugarGrams * 1000.0
            SugarUnit.MICROGRAMS -> idealAddedSugarGrams * 1_000_000.0
            SugarUnit.MICROMOLAR -> molarityMicromolesPerMl * ratio
            SugarUnit.BRIX -> food.optimalBrix
        }
    }
}

object SugarCalibrationEngine {
    private const val SUCROSE_MOLAR_MASS_G_PER_MOL = 342.30
    private const val GRAMS_PER_TEASPOON = 4.20

    fun calculate(
        food: PlantFood,
        servingGrams: Double,
        densityMultiplier: Double,
        personalSweetnessBias: Double = 1.0
    ): SugarCalibrationResult {
        val safeServing = servingGrams.coerceIn(5.0, 2000.0)
        val safeMultiplier = densityMultiplier.coerceIn(0.05, 2.2)
        val safeBias = personalSweetnessBias.coerceIn(0.7, 1.3)

        val idealAddedGrams = food.optimalAddedSugarPer100g * (safeServing / 100.0) * safeBias
        val currentAddedGrams = idealAddedGrams * safeMultiplier

        val naturalSugarGrams = food.naturalSugarPer100g * (safeServing / 100.0)
        val totalSolubleSugarGrams = naturalSugarGrams + currentAddedGrams
        val totalMassWithSugar = safeServing + currentAddedGrams

        // °Brix = grams of sucrose equivalent per 100g total solution
        val brix = ((totalSolubleSugarGrams / totalMassWithSugar.coerceAtLeast(1.0)) * 100.0)
            .coerceIn(0.0, 65.0)

        // Standard sucrose aqueous specific gravity approximation: 1 + 0.00386 * Brix + 0.000018 * Brix^2
        val solutionDensity = 1.0 + 0.00386 * brix + 0.000018 * brix.pow(2)

        // Microscopic molarity in µmol/mL (equivalent to mM)
        val waterVolumeMl = (safeServing * food.waterFraction.coerceIn(0.15, 0.995)) +
            (currentAddedGrams * 0.62) // partial molar volume of dissolved sucrose
        val totalMolesAdded = currentAddedGrams / SUCROSE_MOLAR_MASS_G_PER_MOL
        val micromolesPerMl = ((totalMolesAdded * 1_000_000.0) / waterVolumeMl.coerceAtLeast(1.0))

        // Bitter-to-sweet receptor balance ratio (T1R2/T1R3 activation relative to T2R bitter load)
        val bitterLoad = (food.bitternessIndex * 0.85 + ((7.0 - food.acidityPh).coerceAtLeast(0.0) * 0.9))
            .coerceAtLeast(0.8)
        val receptorBalanceRatio = (currentAddedGrams / (safeServing / 100.0)) / bitterLoad * 2.5

        val phaseState = when {
            safeMultiplier < 0.55 -> SugarPhaseState.RAW_BITTER
            safeMultiplier < 0.85 -> SugarPhaseState.HERBAL_EDGE
            safeMultiplier <= 1.20 -> SugarPhaseState.BALANCED_SWEET
            safeMultiplier <= 1.65 -> SugarPhaseState.NECTAR_RICH
            else -> SugarPhaseState.SYRUP_SATURATED
        }

        // Gaussian-bell palatability peak around densityMultiplier = 1.0
        val palatability = (100.0 * exp(-((safeMultiplier - 1.0).pow(2)) / (2.0 * 0.38.pow(2))))
            .coerceIn(8.0, 100.0)

        val curve = (0..20).map { step ->
            val ratio = step * 0.1 // 0.0 to 2.0
            val stepAdded = idealAddedGrams * ratio
            val stepBrix = (((naturalSugarGrams + stepAdded) / (safeServing + stepAdded).coerceAtLeast(1.0)) * 100.0)
                .coerceIn(0.0, 65.0)
            val stepPalatability = (100.0 * exp(-((ratio - 1.0).pow(2)) / (2.0 * 0.38.pow(2))))
                .coerceIn(5.0, 100.0)
            val maskedPct = (100.0 * (1.0 - exp(-2.4 * ratio))).coerceIn(0.0, 100.0)
            TitrationPoint(
                densityRatio = ratio,
                brix = stepBrix,
                palatabilityScore = stepPalatability,
                bitternessMaskedPct = maskedPct
            )
        }

        return SugarCalibrationResult(
            food = food,
            servingGrams = safeServing,
            densityMultiplier = safeMultiplier,
            idealAddedSugarGrams = idealAddedGrams,
            currentAddedSugarGrams = currentAddedGrams,
            currentSugarMilligrams = currentAddedGrams * 1000.0,
            currentSugarMicrograms = currentAddedGrams * 1_000_000.0,
            currentTeaspoons = currentAddedGrams / GRAMS_PER_TEASPOON,
            totalMillimoles = totalMolesAdded * 1000.0,
            molarityMicromolesPerMl = micromolesPerMl,
            brixDensity = brix,
            solutionDensityGPerCm3 = solutionDensity,
            receptorBalanceRatio = receptorBalanceRatio,
            massPerGramMg = (currentAddedGrams * 1000.0) / safeServing,
            phaseState = phaseState,
            palatabilityScore = palatability,
            titrationCurve = curve
        )
    }

    /**
     * Derives a scientific botanical calibration estimate for any newly discovered
     * Wikipedia plant/food or Open Food Facts item so all 1M+ foods get an instant,
     * physically plausible sugar balance calculation.
     */
    fun estimateProfileFromExternalFood(
        id: String,
        name: String,
        scientificName: String,
        category: String,
        naturalSugarPer100g: Double?,
        summary: String?,
        imageUrl: String?,
        wikipediaTitle: String,
        source: String
    ): PlantFood {
        val lower = "$name $category ${summary.orEmpty()}".lowercase()
        val inferredSugar = (naturalSugarPer100g ?: when {
            "berry" in lower || "fruit" in lower || "mango" in lower || "apple" in lower -> 8.5
            "citrus" in lower || "lemon" in lower || "lime" in lower -> 2.4
            "tea" in lower || "coffee" in lower || "leaf" in lower || "herb" in lower -> 0.2
            "cactus" in lower || "aloe" in lower || "succulent" in lower -> 0.4
            else -> 2.0
        }).coerceIn(0.0, 60.0)

        val inferredBitterness = when {
            "bitter" in lower || "aloe" in lower || "matcha" in lower || "espresso" in lower || "dandelion" in lower -> 7.2
            "cacao" in lower || "cocoa" in lower || "coffee" in lower || "kale" in lower || "arugula" in lower -> 6.4
            "lemon" in lower || "lime" in lower || "cranberry" in lower || "grapefruit" in lower || "hibiscus" in lower -> 5.8
            "tea" in lower || "herb" in lower || "spinach" in lower || "ginger" in lower -> 4.2
            inferredSugar > 10.0 -> 1.4
            else -> 3.0
        }

        val inferredPh = when {
            "lemon" in lower || "lime" in lower || "hibiscus" in lower || "cranberry" in lower -> 2.8
            "grapefruit" in lower || "orange" in lower || "berry" in lower || "aloe" in lower -> 4.3
            "tea" in lower || "coffee" in lower -> 5.2
            else -> 6.0
        }

        val water = when {
            "powder" in lower || "dried" in lower || "chocolate" in lower || "seed" in lower -> 0.25
            "tea" in lower || "juice" in lower || "aloe" in lower || "melon" in lower -> 0.96
            else -> 0.85
        }

        // Balance formula: higher bitterness & acidity need higher sucrose molarity, offset by natural sugar
        val targetTotalSweetness = (2.8 + inferredBitterness * 0.72 + (6.2 - inferredPh).coerceAtLeast(0.0) * 0.85)
        val optimalAdded = (targetTotalSweetness - inferredSugar * 0.75).coerceIn(0.5, 18.0)
        val optimalBrix = ((inferredSugar + optimalAdded) / (100.0 + optimalAdded) * 100.0)

        return PlantFood(
            id = id,
            name = name,
            scientificName = scientificName,
            category = category,
            naturalSugarPer100g = inferredSugar,
            bitternessIndex = inferredBitterness,
            acidityPh = inferredPh,
            waterFraction = water,
            optimalAddedSugarPer100g = optimalAdded,
            optimalBrix = optimalBrix,
            wikipediaTitle = wikipediaTitle,
            imageUrl = imageUrl,
            wikipediaSummary = summary,
            microscopicNote = "Calibrated to neutralize bitterness index ${"%.1f".format(inferredBitterness)} at pH ${"%.1f".format(inferredPh)} with target ${"%.1f".format(optimalBrix)} °Brix.",
            source = source
        )
    }
}
