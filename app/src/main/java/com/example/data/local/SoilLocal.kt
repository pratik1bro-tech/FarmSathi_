package com.example.data.local

import androidx.room.*
import com.example.data.models.SoilReading
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "soil_readings")
data class SoilEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val farmId: String,
    val farmName: String,
    val ph: Double,
    val nitrogen: Double,
    val phosphorus: Double,
    val potassium: Double,
    val soilMoisture: Double,
    val soilTemperature: Double,
    val electricalConductivity: Double,
    val organicMatter: Double,
    val soilType: String,
    val dataSource: String,
    val timestamp: Long
) {
    fun toDomain() = SoilReading(
        id = id,
        userId = userId,
        farmId = farmId,
        farmName = farmName,
        ph = ph,
        nitrogen = nitrogen,
        phosphorus = phosphorus,
        potassium = potassium,
        soilMoisture = soilMoisture,
        soilTemperature = soilTemperature,
        electricalConductivity = electricalConductivity,
        organicMatter = organicMatter,
        soilType = soilType,
        dataSource = dataSource,
        timestamp = timestamp
    )

    companion object {
        fun fromDomain(domain: SoilReading) = SoilEntity(
            id = domain.id,
            userId = domain.userId,
            farmId = domain.farmId,
            farmName = domain.farmName,
            ph = domain.ph,
            nitrogen = domain.nitrogen,
            phosphorus = domain.phosphorus,
            potassium = domain.potassium,
            soilMoisture = domain.soilMoisture,
            soilTemperature = domain.soilTemperature,
            electricalConductivity = domain.electricalConductivity,
            organicMatter = domain.organicMatter,
            soilType = domain.soilType,
            dataSource = domain.dataSource,
            timestamp = domain.timestamp
        )
    }
}

@Dao
interface SoilDao {
    @Query("SELECT * FROM soil_readings WHERE userId = :userId ORDER BY timestamp DESC")
    fun getSoilReadingsForUser(userId: String): Flow<List<SoilEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSoilReading(entity: SoilEntity)

    @Query("DELETE FROM soil_readings WHERE id = :id AND userId = :userId")
    suspend fun deleteSoilReading(id: String, userId: String)
}
