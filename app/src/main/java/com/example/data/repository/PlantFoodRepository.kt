package com.example.data.repository

import com.example.data.local.CalibrationLogDao
import com.example.data.local.PlantFoodDao
import com.example.data.local.PrepopulateSeedData
import com.example.data.model.CalibrationLog
import com.example.data.model.PlantFood
import com.example.data.remote.OpenFoodFactsApiService
import com.example.data.remote.WikipediaApiService
import com.example.data.science.SugarCalibrationEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PlantFoodRepository(
    private val plantFoodDao: PlantFoodDao,
    private val calibrationLogDao: CalibrationLogDao,
    private val wikipediaApi: WikipediaApiService,
    private val openFoodFactsApi: OpenFoodFactsApiService
) {
    val allFoodsFlow: Flow<List<PlantFood>> = plantFoodDao.observeAll()
    val calibrationLogsFlow: Flow<List<CalibrationLog>> = calibrationLogDao.observeAll()

    suspend fun ensureSeeded() = withContext(Dispatchers.IO) {
        if (plantFoodDao.count() == 0) {
            plantFoodDao.upsertAll(PrepopulateSeedData.initialFoods())
        }
    }

    suspend fun getFoodById(id: String): PlantFood? = withContext(Dispatchers.IO) {
        plantFoodDao.getById(id)
    }

    suspend fun enrichFromWikipediaIfNeeded(food: PlantFood): PlantFood = withContext(Dispatchers.IO) {
        if (!food.imageUrl.isNullOrBlank() && !food.wikipediaSummary.isNullOrBlank()) {
            return@withContext food
        }
        val title = food.wikipediaTitle.ifBlank { food.name.replace(" ", "_") }
        try {
            val summaryResp = wikipediaApi.getPageSummary(title)
            val resolvedImage = summaryResp.thumbnail?.source ?: summaryResp.originalimage?.source
            val resolvedExtract = summaryResp.extract
            if (!resolvedImage.isNullOrBlank() || !resolvedExtract.isNullOrBlank()) {
                val updated = food.copy(
                    imageUrl = resolvedImage ?: food.imageUrl,
                    wikipediaSummary = resolvedExtract ?: food.wikipediaSummary,
                    lastUpdatedMillis = System.currentTimeMillis()
                )
                plantFoodDao.updateWikiEnrichment(
                    id = food.id,
                    imageUrl = updated.imageUrl,
                    summary = updated.wikipediaSummary,
                    updatedAt = updated.lastUpdatedMillis
                )
                return@withContext updated
            }
        } catch (_: Exception) {
            // Offline-first: silently preserve local data when offline
        }
        food
    }

    fun searchLocal(query: String): Flow<List<PlantFood>> = plantFoodDao.searchLocal(query)

    suspend fun searchExternalDatabases(query: String): List<PlantFood> = withContext(Dispatchers.IO) {
        val clean = query.trim()
        if (clean.length < 2) return@withContext emptyList()

        coroutineScope {
            val wikiDeferred = async {
                try {
                    val resp = wikipediaApi.searchBotanicalPages(query = clean)
                    resp.query?.pages?.values.orEmpty().mapNotNull { page ->
                        val title = page.title?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                        val slug = "wiki_${title.lowercase().replace(Regex("[^a-z0-9]+"), "_")}"
                        SugarCalibrationEngine.estimateProfileFromExternalFood(
                            id = slug,
                            name = title,
                            scientificName = "Wikipedia Botanical Entry",
                            category = "Wikipedia Plant & Food",
                            naturalSugarPer100g = null,
                            summary = page.extract,
                            imageUrl = page.thumbnail?.source,
                            wikipediaTitle = title.replace(" ", "_"),
                            source = "Wikipedia"
                        )
                    }
                } catch (_: Exception) {
                    emptyList()
                }
            }

            val offDeferred = async {
                try {
                    val resp = openFoodFactsApi.searchFoods(query = clean)
                    resp.products.orEmpty().mapNotNull { prod ->
                        val pName = prod.productName?.trim()?.takeIf { it.isNotBlank() }
                            ?: return@mapNotNull null
                        val code = prod.code?.takeIf { it.isNotBlank() } ?: pName.hashCode().toString()
                        val cat = prod.categories?.split(",")?.firstOrNull()?.trim()
                            ?.takeIf { it.isNotBlank() } ?: "Food Database"
                        SugarCalibrationEngine.estimateProfileFromExternalFood(
                            id = "off_$code",
                            name = pName,
                            scientificName = cat,
                            category = "Open Food Facts",
                            naturalSugarPer100g = prod.nutriments?.sugars100g,
                            summary = "Listed in Open Food Facts global food database. Natural sugar baseline: ${"%.1f".format(prod.nutriments?.sugars100g ?: 0.0)}g per 100g.",
                            imageUrl = prod.imageFrontUrl ?: prod.imageSmallUrl,
                            wikipediaTitle = pName.replace(" ", "_"),
                            source = "OpenFoodFacts"
                        )
                    }
                } catch (_: Exception) {
                    emptyList()
                }
            }

            (wikiDeferred.await() + offDeferred.await()).distinctBy { it.name.lowercase() }
        }
    }

    suspend fun saveCustomOrExternalFood(food: PlantFood) = withContext(Dispatchers.IO) {
        plantFoodDao.upsert(food.copy(lastUpdatedMillis = System.currentTimeMillis()))
    }

    suspend fun saveCalibrationLog(log: CalibrationLog) = withContext(Dispatchers.IO) {
        calibrationLogDao.insert(log)
    }

    suspend fun deleteCalibrationLog(id: Long) = withContext(Dispatchers.IO) {
        calibrationLogDao.deleteById(id)
    }
}
