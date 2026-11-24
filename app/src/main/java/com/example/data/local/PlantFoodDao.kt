package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.PlantFood
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantFoodDao {
    @Query("SELECT * FROM plant_foods ORDER BY isUserCreated DESC, lastUpdatedMillis DESC")
    fun observeAll(): Flow<List<PlantFood>>

    @Query("SELECT * FROM plant_foods WHERE name LIKE '%' || :query || '%' OR scientificName LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchLocal(query: String): Flow<List<PlantFood>>

    @Query("SELECT * FROM plant_foods WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): PlantFood?

    @Query("SELECT COUNT(*) FROM plant_foods")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<PlantFood>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: PlantFood)

    @Query("UPDATE plant_foods SET imageUrl = :imageUrl, wikipediaSummary = :summary, lastUpdatedMillis = :updatedAt WHERE id = :id")
    suspend fun updateWikiEnrichment(id: String, imageUrl: String?, summary: String?, updatedAt: Long)
}
