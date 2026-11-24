package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CalibrationLog
import kotlinx.coroutines.flow.Flow

@Dao
interface CalibrationLogDao {
    @Query("SELECT * FROM calibration_logs ORDER BY createdAtMillis DESC")
    fun observeAll(): Flow<List<CalibrationLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: CalibrationLog)

    @Query("DELETE FROM calibration_logs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM calibration_logs")
    suspend fun clearAll()
}
