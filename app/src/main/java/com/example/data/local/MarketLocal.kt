package com.example.data.local

import androidx.room.*
import com.example.data.models.LogisticsRequest
import com.example.data.models.SellingRequest
import com.example.data.models.SyncQueueItem
import com.example.data.models.YieldForecast
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "selling_requests")
data class SellingRequestEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val farmerName: String,
    val cropName: String,
    val quantityQuintals: Double,
    val expectedPricePerQuintal: Double,
    val buyerId: String,
    val buyerName: String,
    val mandiName: String,
    val status: String,
    val createdAt: Long
) {
    fun toDomain() = SellingRequest(
        id = id,
        userId = userId,
        farmerName = farmerName,
        cropName = cropName,
        quantityQuintals = quantityQuintals,
        expectedPricePerQuintal = expectedPricePerQuintal,
        buyerId = buyerId,
        buyerName = buyerName,
        mandiName = mandiName,
        status = status,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(domain: SellingRequest) = SellingRequestEntity(
            id = domain.id,
            userId = domain.userId,
            farmerName = domain.farmerName,
            cropName = domain.cropName,
            quantityQuintals = domain.quantityQuintals,
            expectedPricePerQuintal = domain.expectedPricePerQuintal,
            buyerId = domain.buyerId,
            buyerName = domain.buyerName,
            mandiName = domain.mandiName,
            status = domain.status,
            createdAt = domain.createdAt
        )
    }
}

@Dao
interface SellingRequestDao {
    @Query("SELECT * FROM selling_requests WHERE userId = :userId ORDER BY createdAt DESC")
    fun getRequestsForUser(userId: String): Flow<List<SellingRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(entity: SellingRequestEntity)

    @Query("DELETE FROM selling_requests WHERE id = :id AND userId = :userId")
    suspend fun deleteRequest(id: String, userId: String)
}

@Entity(tableName = "logistics_requests")
data class LogisticsRequestEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val farmerName: String,
    val pickupLocation: String,
    val destinationMandi: String,
    val cropName: String,
    val quantityQuintals: Double,
    val vehicleType: String,
    val estimatedDistanceKm: Double,
    val estimatedCostInr: Double,
    val pickupDate: String,
    val status: String,
    val createdAt: Long
) {
    fun toDomain() = LogisticsRequest(
        id = id,
        userId = userId,
        farmerName = farmerName,
        pickupLocation = pickupLocation,
        destinationMandi = destinationMandi,
        cropName = cropName,
        quantityQuintals = quantityQuintals,
        vehicleType = vehicleType,
        estimatedDistanceKm = estimatedDistanceKm,
        estimatedCostInr = estimatedCostInr,
        pickupDate = pickupDate,
        status = status,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(domain: LogisticsRequest) = LogisticsRequestEntity(
            id = domain.id,
            userId = domain.userId,
            farmerName = domain.farmerName,
            pickupLocation = domain.pickupLocation,
            destinationMandi = domain.destinationMandi,
            cropName = domain.cropName,
            quantityQuintals = domain.quantityQuintals,
            vehicleType = domain.vehicleType,
            estimatedDistanceKm = domain.estimatedDistanceKm,
            estimatedCostInr = domain.estimatedCostInr,
            pickupDate = domain.pickupDate,
            status = domain.status,
            createdAt = domain.createdAt
        )
    }
}

@Dao
interface LogisticsRequestDao {
    @Query("SELECT * FROM logistics_requests WHERE userId = :userId ORDER BY createdAt DESC")
    fun getLogisticsForUser(userId: String): Flow<List<LogisticsRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogistics(entity: LogisticsRequestEntity)

    @Query("DELETE FROM logistics_requests WHERE id = :id AND userId = :userId")
    suspend fun deleteLogistics(id: String, userId: String)
}

@Entity(tableName = "yield_forecasts")
data class YieldForecastEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val cropId: String,
    val cropName: String,
    val variety: String,
    val farmAreaAcres: Double,
    val sowingDate: String,
    val estimatedYieldMinPerAcre: Double,
    val estimatedYieldMaxPerAcre: Double,
    val estimatedTotalMinQuintals: Double,
    val estimatedTotalMaxQuintals: Double,
    val harvestWindowStart: String,
    val harvestWindowEnd: String,
    val daysRemainingToHarvest: Int,
    val confidence: String,
    val influencingFactorsJoined: String,
    val timestamp: Long
) {
    fun toDomain() = YieldForecast(
        id = id,
        userId = userId,
        cropId = cropId,
        cropName = cropName,
        variety = variety,
        farmAreaAcres = farmAreaAcres,
        sowingDate = sowingDate,
        estimatedYieldMinPerAcre = estimatedYieldMinPerAcre,
        estimatedYieldMaxPerAcre = estimatedYieldMaxPerAcre,
        estimatedTotalMinQuintals = estimatedTotalMinQuintals,
        estimatedTotalMaxQuintals = estimatedTotalMaxQuintals,
        harvestWindowStart = harvestWindowStart,
        harvestWindowEnd = harvestWindowEnd,
        daysRemainingToHarvest = daysRemainingToHarvest,
        confidence = confidence,
        influencingFactors = if (influencingFactorsJoined.isBlank()) emptyList() else influencingFactorsJoined.split("|||"),
        timestamp = timestamp
    )

    companion object {
        fun fromDomain(domain: YieldForecast) = YieldForecastEntity(
            id = domain.id,
            userId = domain.userId,
            cropId = domain.cropId,
            cropName = domain.cropName,
            variety = domain.variety,
            farmAreaAcres = domain.farmAreaAcres,
            sowingDate = domain.sowingDate,
            estimatedYieldMinPerAcre = domain.estimatedYieldMinPerAcre,
            estimatedYieldMaxPerAcre = domain.estimatedYieldMaxPerAcre,
            estimatedTotalMinQuintals = domain.estimatedTotalMinQuintals,
            estimatedTotalMaxQuintals = domain.estimatedTotalMaxQuintals,
            harvestWindowStart = domain.harvestWindowStart,
            harvestWindowEnd = domain.harvestWindowEnd,
            daysRemainingToHarvest = domain.daysRemainingToHarvest,
            confidence = domain.confidence,
            influencingFactorsJoined = domain.influencingFactors.joinToString("|||"),
            timestamp = domain.timestamp
        )
    }
}

@Dao
interface YieldForecastDao {
    @Query("SELECT * FROM yield_forecasts WHERE userId = :userId ORDER BY timestamp DESC")
    fun getForecastsForUser(userId: String): Flow<List<YieldForecastEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForecast(entity: YieldForecastEntity)
}

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val actionType: String,
    val payloadSummary: String,
    val timestamp: Long,
    val isSynced: Boolean
) {
    fun toDomain() = SyncQueueItem(
        id = id,
        userId = userId,
        actionType = actionType,
        payloadSummary = payloadSummary,
        timestamp = timestamp,
        isSynced = isSynced
    )

    companion object {
        fun fromDomain(domain: SyncQueueItem) = SyncQueueEntity(
            id = domain.id,
            userId = domain.userId,
            actionType = domain.actionType,
            payloadSummary = domain.payloadSummary,
            timestamp = domain.timestamp,
            isSynced = domain.isSynced
        )
    }
}

@Dao
interface SyncQueueDao {
    @Query("SELECT * FROM sync_queue WHERE userId = :userId ORDER BY timestamp DESC")
    fun getSyncQueueForUser(userId: String): Flow<List<SyncQueueEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncItem(entity: SyncQueueEntity)

    @Query("UPDATE sync_queue SET isSynced = 1 WHERE userId = :userId")
    suspend fun markAllSynced(userId: String)
}

@Entity(tableName = "mandi_prices")
data class MandiPriceEntity(
    @PrimaryKey val id: String,
    val commodity: String,
    val variety: String,
    val mandiName: String,
    val district: String,
    val state: String,
    val minPrice: Double,
    val maxPrice: Double,
    val modalPrice: Double,
    val priceChange: Double,
    val arrivalQuantityQuintals: Double,
    val trend: String,
    val latitude: Double,
    val longitude: Double,
    val dataSource: String,
    val lastUpdated: Long
) {
    fun toDomain() = com.example.data.models.MandiPrice(
        id = id,
        commodity = commodity,
        variety = variety,
        mandiName = mandiName,
        district = district,
        state = state,
        minPrice = minPrice,
        maxPrice = maxPrice,
        modalPrice = modalPrice,
        priceChange = priceChange,
        arrivalQuantityQuintals = arrivalQuantityQuintals,
        trend = trend,
        latitude = latitude,
        longitude = longitude,
        dataSource = dataSource,
        lastUpdated = lastUpdated
    )

    companion object {
        fun fromDomain(domain: com.example.data.models.MandiPrice) = MandiPriceEntity(
            id = domain.id,
            commodity = domain.commodity,
            variety = domain.variety,
            mandiName = domain.mandiName,
            district = domain.district,
            state = domain.state,
            minPrice = domain.minPrice,
            maxPrice = domain.maxPrice,
            modalPrice = domain.modalPrice,
            priceChange = domain.priceChange,
            arrivalQuantityQuintals = domain.arrivalQuantityQuintals,
            trend = domain.trend,
            latitude = domain.latitude,
            longitude = domain.longitude,
            dataSource = domain.dataSource,
            lastUpdated = domain.lastUpdated
        )
    }
}

@Dao
interface MandiPriceDao {
    @Query("SELECT * FROM mandi_prices WHERE LOWER(commodity) = LOWER(:commodity) ORDER BY modalPrice DESC")
    fun getMandiPricesForCommodity(commodity: String): Flow<List<MandiPriceEntity>>

    @Query("SELECT * FROM mandi_prices ORDER BY lastUpdated DESC")
    fun getAllMandiPrices(): Flow<List<MandiPriceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMandiPrices(entities: List<MandiPriceEntity>)
}
