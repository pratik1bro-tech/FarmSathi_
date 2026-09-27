package com.example.data.local

import androidx.room.*
import com.example.data.models.TelemetryData
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "telemetry_readings")
data class TelemetryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val farmId: String,
    val deviceId: String,
    val airTemperature: Double,
    val humidity: Double,
    val soilMoisture: Double,
    val soilTemperature: Double,
    val rainfall: Double,
    val windSpeed: Double,
    val lightIntensity: Double,
    val leafWetness: Double,
    val dataSource: String,
    val timestamp: Long
) {
    fun toDomain() = TelemetryData(
        id = id,
        userId = userId,
        farmId = farmId,
        deviceId = deviceId,
        airTemperature = airTemperature,
        humidity = humidity,
        soilMoisture = soilMoisture,
        soilTemperature = soilTemperature,
        rainfall = rainfall,
        windSpeed = windSpeed,
        lightIntensity = lightIntensity,
        leafWetness = leafWetness,
        dataSource = dataSource,
        timestamp = timestamp
    )

    companion object {
        fun fromDomain(domain: TelemetryData) = TelemetryEntity(
            id = domain.id,
            userId = domain.userId,
            farmId = domain.farmId,
            deviceId = domain.deviceId,
            airTemperature = domain.airTemperature,
            humidity = domain.humidity,
            soilMoisture = domain.soilMoisture,
            soilTemperature = domain.soilTemperature,
            rainfall = domain.rainfall,
            windSpeed = domain.windSpeed,
            lightIntensity = domain.lightIntensity,
            leafWetness = domain.leafWetness,
            dataSource = domain.dataSource,
            timestamp = domain.timestamp
        )
    }
}

@Dao
interface TelemetryDao {
    @Query("SELECT * FROM telemetry_readings WHERE userId = :userId ORDER BY timestamp DESC")
    fun getTelemetryForUser(userId: String): Flow<List<TelemetryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTelemetry(entity: TelemetryEntity)

    @Query("DELETE FROM telemetry_readings WHERE id = :id AND userId = :userId")
    suspend fun deleteTelemetry(id: String, userId: String)
}
