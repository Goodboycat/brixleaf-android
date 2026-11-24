package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.CalibrationLog
import com.example.data.model.PlantFood

@Database(
    entities = [PlantFood::class, CalibrationLog::class],
    version = 1,
    exportSchema = false
)
abstract class BrixLeafDatabase : RoomDatabase() {
    abstract fun plantFoodDao(): PlantFoodDao
    abstract fun calibrationLogDao(): CalibrationLogDao

    companion object {
        @Volatile
        private var INSTANCE: BrixLeafDatabase? = null

        fun getInstance(context: Context): BrixLeafDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    BrixLeafDatabase::class.java,
                    "brixleaf_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
