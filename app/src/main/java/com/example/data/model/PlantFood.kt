package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plant_foods")
data class PlantFood(
    @PrimaryKey val id: String,
    val name: String,
    val scientificName: String,
    val category: String,
    val naturalSugarPer100g: Double,
    val bitternessIndex: Double,
    val acidityPh: Double,
    val waterFraction: Double,
    val optimalAddedSugarPer100g: Double,
    val optimalBrix: Double,
    val wikipediaTitle: String,
    val imageUrl: String? = null,
    val wikipediaSummary: String? = null,
    val microscopicNote: String,
    val source: String = "Curated",
    val isUserCreated: Boolean = false,
    val lastUpdatedMillis: Long = System.currentTimeMillis()
)
