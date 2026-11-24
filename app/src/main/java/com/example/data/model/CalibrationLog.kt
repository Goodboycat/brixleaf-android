package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calibration_logs")
data class CalibrationLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val foodId: String,
    val foodName: String,
    val servingGrams: Double,
    val recommendedSugarGrams: Double,
    val userAdjustedSugarGrams: Double,
    val brixDensity: Double,
    val micromolesPerMl: Double,
    val phaseStateName: String,
    val note: String,
    val createdAtMillis: Long = System.currentTimeMillis()
)
