package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        FarmEntity::class,
        CropEntity::class,
        ScanEntity::class,
        ChatMessageEntity::class,
        NotificationEntity::class,
        SoilEntity::class,
        TelemetryEntity::class,
        SellingRequestEntity::class,
        LogisticsRequestEntity::class,
        YieldForecastEntity::class,
        SyncQueueEntity::class,
        MandiPriceEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class FarmSathiDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun farmDao(): FarmDao
    abstract fun cropDao(): CropDao
    abstract fun scanDao(): ScanDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun notificationDao(): NotificationDao
    abstract fun soilDao(): SoilDao
    abstract fun telemetryDao(): TelemetryDao
    abstract fun sellingRequestDao(): SellingRequestDao
    abstract fun logisticsRequestDao(): LogisticsRequestDao
    abstract fun yieldForecastDao(): YieldForecastDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun mandiPriceDao(): MandiPriceDao

    companion object {
        @Volatile
        private var INSTANCE: FarmSathiDatabase? = null

        fun getInstance(context: Context): FarmSathiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FarmSathiDatabase::class.java,
                    "farmsathi_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
