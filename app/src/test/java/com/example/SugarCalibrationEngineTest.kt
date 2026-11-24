package com.example

import com.example.data.local.PrepopulateSeedData
import com.example.data.model.SugarPhaseState
import com.example.data.model.SugarUnit
import com.example.data.science.SugarCalibrationEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SugarCalibrationEngineTest {
    private val aloeVera = PrepopulateSeedData.initialFoods().first { it.id == "aloe_vera" }

    @Test
    fun aloeVeraOptimalCalibration_matchesDailyLifeAndMicroscopicMetrics() {
        val result = SugarCalibrationEngine.calculate(
            food = aloeVera,
            servingGrams = 100.0,
            densityMultiplier = 1.0
        )

        assertEquals(6.42, result.currentAddedSugarGrams, 0.01)
        assertEquals(6420.0, result.currentSugarMilligrams, 1.0)
        assertEquals(1.528, result.currentTeaspoons, 0.01)
        assertEquals(SugarPhaseState.BALANCED_SWEET, result.phaseState)
        assertTrue(result.molarityMicromolesPerMl > 150.0)
        assertTrue(result.brixDensity > 5.0)
        assertEquals(result.currentAddedSugarGrams, result.valueForUnit(SugarUnit.GRAMS), 0.001)
    }

    @Test
    fun sugarDensityMultiplier_transitionsPhaseStatesCorrectly() {
        val raw = SugarCalibrationEngine.calculate(aloeVera, 100.0, 0.3)
        val crisp = SugarCalibrationEngine.calculate(aloeVera, 100.0, 0.7)
        val balanced = SugarCalibrationEngine.calculate(aloeVera, 100.0, 1.0)
        val nectar = SugarCalibrationEngine.calculate(aloeVera, 100.0, 1.4)
        val syrup = SugarCalibrationEngine.calculate(aloeVera, 100.0, 1.9)

        assertEquals(SugarPhaseState.RAW_BITTER, raw.phaseState)
        assertEquals(SugarPhaseState.HERBAL_EDGE, crisp.phaseState)
        assertEquals(SugarPhaseState.BALANCED_SWEET, balanced.phaseState)
        assertEquals(SugarPhaseState.NECTAR_RICH, nectar.phaseState)
        assertEquals(SugarPhaseState.SYRUP_SATURATED, syrup.phaseState)
    }

    @Test
    fun changingServingQuantity_synchronouslyScalesSugarAndMillimoles() {
        val base100 = SugarCalibrationEngine.calculate(aloeVera, 100.0, 1.0)
        val double200 = SugarCalibrationEngine.calculate(aloeVera, 200.0, 1.0)
        val fiveHundred = SugarCalibrationEngine.calculate(aloeVera, 500.0, 1.0)

        assertEquals(base100.idealAddedSugarGrams * 2.0, double200.currentAddedSugarGrams, 0.001)
        assertEquals(base100.idealAddedSugarGrams * 5.0, fiveHundred.currentAddedSugarGrams, 0.001)
        assertEquals(base100.totalMillimoles * 5.0, fiveHundred.totalMillimoles, 0.01)
    }
}
