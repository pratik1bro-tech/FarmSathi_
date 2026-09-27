package com.example.data.repository

import android.content.Context
import com.example.data.local.FarmSathiDatabase
import com.example.data.local.MandiPriceEntity
import com.example.data.models.BuyerProfile
import com.example.data.models.MandiPrice
import com.example.service.MandiMarketEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class MarketRepository(private val context: Context) {

    private val db = FarmSathiDatabase.getInstance(context)
    private val mandiPriceDao = db.mandiPriceDao()

    fun getMandiPricesFlow(commodity: String): Flow<List<MandiPrice>> {
        return mandiPriceDao.getMandiPricesForCommodity(commodity).map { list ->
            if (list.isNotEmpty()) {
                list.map { it.toDomain() }
            } else {
                // Return regional fallback if local DB has not cached this commodity yet
                MandiMarketEngine.getRegionalMandiPrices(commodity)
            }
        }
    }

    suspend fun refreshMandiPrices(commodity: String): Result<List<MandiPrice>> = withContext(Dispatchers.IO) {
        try {
            // Fetch live/simulated market payload with coordinates
            val freshPrices = MandiMarketEngine.getRegionalMandiPrices(commodity)
            mandiPriceDao.insertMandiPrices(freshPrices.map { MandiPriceEntity.fromDomain(it) })
            Result.success(freshPrices)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Location-based Market Recommendation Engine
     */
    fun calculateBestMarketForLocation(
        prices: List<MandiPrice>,
        farmLat: Double = 22.7196,
        farmLng: Double = 75.8577,
        quantityQuintals: Double = 35.0,
        vehicleType: String = "Small Truck (Ace)",
        loadingCostInr: Double = 350.0
    ): MarketRecommendation {
        if (prices.isEmpty()) {
            val dummy = MandiPrice("m_0", "Crop", "FAQ", "Local Mandi", "Indore", "MP", 4500.0, 4800.0, 4650.0)
            return MarketRecommendation(
                bestOverall = dummy,
                highestPrice = dummy,
                closestMarket = dummy,
                highestNetProfitMarket = dummy,
                grossRevenue = 4650.0 * quantityQuintals,
                transportCost = 800.0,
                totalExpenses = 800.0 + loadingCostInr,
                netRevenue = (4650.0 * quantityQuintals) - 1150.0,
                netRevenuePerQuintal = 4650.0 - (1150.0 / quantityQuintals),
                distanceKm = 15.0,
                explanation = "Calculated using local market defaults."
            )
        }

        val sortedByPrice = prices.sortedByDescending { it.modalPrice }
        val highestPriceMandi = sortedByPrice.first()

        var closestMandi = prices.first()
        var minDistance = Double.MAX_VALUE

        var bestNetProfitMandi = prices.first()
        var maxNetRev = -Double.MAX_VALUE
        var bestTransportCost = 0.0
        var bestDistance = 0.0
        var bestGrossRev = 0.0

        for (item in prices) {
            val dist = calculateHaversineDistanceKm(farmLat, farmLng, item.latitude, item.longitude)
            if (dist < minDistance) {
                minDistance = dist
                closestMandi = item
            }

            val transportCost = MandiMarketEngine.calculateLogisticsCost(vehicleType, dist, quantityQuintals)
            val totalExp = transportCost + loadingCostInr
            val grossRev = item.modalPrice * quantityQuintals
            val netRev = grossRev - totalExp

            if (netRev > maxNetRev) {
                maxNetRev = netRev
                bestNetProfitMandi = item
                bestTransportCost = transportCost
                bestDistance = dist
                bestGrossRev = grossRev
            }
        }

        val bestNetPerQtl = maxNetRev / quantityQuintals
        val totalExpenses = bestTransportCost + loadingCostInr

        val explanation = if (bestNetProfitMandi.id == highestPriceMandi.id) {
            "${bestNetProfitMandi.mandiName} offers both the highest market price (₹${bestNetProfitMandi.modalPrice.toInt()}/Qtl) " +
                    "and highest estimated net profit of ₹${String.format("%.0f", maxNetRev)} for $quantityQuintals quintals " +
                    "after deducting ₹${String.format("%.0f", totalExpenses)} transport and loading expenses."
        } else {
            "${highestPriceMandi.mandiName} has a higher raw rate (₹${highestPriceMandi.modalPrice.toInt()}/Qtl), " +
                    "but due to its closer distance (${String.format("%.1f", bestDistance)} km), ${bestNetProfitMandi.mandiName} " +
                    "delivers ₹${String.format("%.0f", maxNetRev)} net revenue (₹${String.format("%.0f", bestNetPerQtl)}/Qtl) after transport costs."
        }

        return MarketRecommendation(
            bestOverall = bestNetProfitMandi,
            highestPrice = highestPriceMandi,
            closestMarket = closestMandi,
            highestNetProfitMarket = bestNetProfitMandi,
            grossRevenue = bestGrossRev,
            transportCost = bestTransportCost,
            totalExpenses = totalExpenses,
            netRevenue = maxNetRev,
            netRevenuePerQuintal = bestNetPerQtl,
            distanceKm = bestDistance,
            explanation = explanation
        )
    }

    private fun calculateHaversineDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val rawDist = r * c
        return if (rawDist < 1.0) 8.5 else Math.round(rawDist * 10.0) / 10.0
    }
}

data class MarketRecommendation(
    val bestOverall: MandiPrice,
    val highestPrice: MandiPrice,
    val closestMarket: MandiPrice,
    val highestNetProfitMarket: MandiPrice,
    val grossRevenue: Double,
    val transportCost: Double,
    val totalExpenses: Double,
    val netRevenue: Double,
    val netRevenuePerQuintal: Double,
    val distanceKm: Double,
    val explanation: String
)
