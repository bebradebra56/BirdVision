package com.vision.birdvisionpr.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.vision.birdvisionpr.data.db.dao.*
import com.vision.birdvisionpr.data.db.entity.*

@Database(
    entities = [
        BehaviorLogEntity::class,
        EggLogEntity::class,
        TemperatureLogEntity::class,
        NightWatchEntity::class,
        ChecklistEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun behaviorLogDao(): BehaviorLogDao
    abstract fun eggLogDao(): EggLogDao
    abstract fun temperatureLogDao(): TemperatureLogDao
    abstract fun nightWatchDao(): NightWatchDao
    abstract fun checklistDao(): ChecklistDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bird_vision_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
