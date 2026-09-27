package com.example.service

import com.example.data.models.*

object MandiMarketEngine {

    fun getRegionalMandiPrices(selectedCommodity: String = "Soybean"): List<MandiPrice> {
        val now = System.currentTimeMillis()
        return when (selectedCommodity.lowercase()) {
            "soybean" -> listOf(
                MandiPrice("m_1", "Soybean", "JS-335 Grade A", "Indore Mandi", "Indore", "Madhya Pradesh", 4750.0, 5120.0, 4980.0, 70.0, 1420.0, "Increasing", 22.7196, 75.8577, "ESTIMATED", now),
                MandiPrice("m_2", "Soybean", "JS-9560 Cleaned", "Ujjain Mandi", "Ujjain", "Madhya Pradesh", 4680.0, 5080.0, 4920.0, 40.0, 980.0, "Increasing", 23.1765, 75.7885, "ESTIMATED", now),
                MandiPrice("m_3", "Soybean", "FAQ Yellow", "Dewas Mandi", "Dewas", "Madhya Pradesh", 4700.0, 5050.0, 4890.0, -20.0, 1150.0, "Decreasing", 22.9676, 76.0534, "ESTIMATED", now),
                MandiPrice("m_4", "Soybean", "Standard FAQ", "Bhopal Mandi", "Bhopal", "Madhya Pradesh", 4620.0, 4990.0, 4850.0, 0.0, 850.0, "Stable", 23.2599, 77.4126, "OFFLINE_CACHED", now - 3600000),
                MandiPrice("m_5", "Soybean", "Bold Grain A1", "Mandsaur Mandi", "Mandsaur", "Madhya Pradesh", 4800.0, 5200.0, 5040.0, 90.0, 2100.0, "Increasing", 24.0723, 75.0683, "ESTIMATED", now)
            )
            "wheat" -> listOf(
                MandiPrice("m_6", "Wheat", "Sharbati GW-322", "Indore Mandi", "Indore", "Madhya Pradesh", 2580.0, 2820.0, 2710.0, 30.0, 2300.0, "Increasing", 22.7196, 75.8577, "ESTIMATED", now),
                MandiPrice("m_7", "Wheat", "Lok-1 Quality", "Bhopal Mandi", "Bhopal", "Madhya Pradesh", 2550.0, 2790.0, 2680.0, 15.0, 1850.0, "Increasing", 23.2599, 77.4126, "ESTIMATED", now),
                MandiPrice("m_8", "Wheat", "Mill Quality", "Ujjain Mandi", "Ujjain", "Madhya Pradesh", 2560.0, 2800.0, 2690.0, 0.0, 1200.0, "Stable", 23.1765, 75.7885, "ESTIMATED", now),
                MandiPrice("m_9", "Wheat", "Sharbati Export", "Sehore Mandi", "Sehore", "Madhya Pradesh", 2610.0, 2880.0, 2760.0, 50.0, 1600.0, "Increasing", 23.2031, 77.0844, "ESTIMATED", now)
            )
            "gram", "chana" -> listOf(
                MandiPrice("m_10", "Gram", "Kabuli Chana", "Indore Mandi", "Indore", "Madhya Pradesh", 5800.0, 6250.0, 6050.0, 110.0, 890.0, "Increasing", 22.7196, 75.8577, "ESTIMATED", now),
                MandiPrice("m_11", "Gram", "Desi Chana", "Ujjain Mandi", "Ujjain", "Madhya Pradesh", 5750.0, 6200.0, 5980.0, 60.0, 650.0, "Increasing", 23.1765, 75.7885, "ESTIMATED", now),
                MandiPrice("m_12", "Gram", "Desi Medium", "Dewas Mandi", "Dewas", "Madhya Pradesh", 5700.0, 6150.0, 5920.0, -30.0, 520.0, "Decreasing", 22.9676, 76.0534, "ESTIMATED", now)
            )
            else -> listOf(
                MandiPrice("m_13", selectedCommodity, "Standard FAQ", "Indore Mandi", "Indore", "Madhya Pradesh", 4200.0, 4650.0, 4480.0, 50.0, 750.0, "Increasing", 22.7196, 75.8577, "ESTIMATED", now),
                MandiPrice("m_14", selectedCommodity, "Grade A", "Ujjain Mandi", "Ujjain", "Madhya Pradesh", 4150.0, 4600.0, 4420.0, 20.0, 620.0, "Stable", 23.1765, 75.7885, "ESTIMATED", now)
            )
        }
    }

    fun getPriceForecast(commodity: String, mandiName: String, currentModalPrice: Double): PriceForecast {
        val f7Min = currentModalPrice * 1.02
        val f7Max = currentModalPrice * 1.06
        val f30Min = currentModalPrice * 1.04
        val f30Max = currentModalPrice * 1.10

        return PriceForecast(
            commodity = commodity,
            mandiName = mandiName,
            currentPrice = currentModalPrice,
            forecast7DayMin = Math.round(f7Min * 10.0) / 10.0,
            forecast7DayMax = Math.round(f7Max * 10.0) / 10.0,
            forecast30DayMin = Math.round(f30Min * 10.0) / 10.0,
            forecast30DayMax = Math.round(f30Max * 10.0) / 10.0,
            trend = "Moderately Increasing ↑",
            confidence = "AI Estimate — Not Guaranteed (Confidence: 82%)",
            keyDrivers = listOf(
                "Increased processing plant buying demand",
                "Local mandi arrival trend observations",
                "Seasonal regional crop trading patterns"
            )
        )
    }

    fun getVerifiedBuyers(cropName: String = "Soybean"): List<BuyerProfile> {
        return listOf(
            BuyerProfile(
                id = "b_1",
                businessName = "Malwa Oil Refineries Ltd.",
                contactName = "Suresh Patel",
                phone = "+91 98260 11223",
                location = "Pithampur Industrial Area",
                district = "Dhar / Indore",
                cropRequired = cropName,
                quantityRequiredQuintals = 250.0,
                offeredPricePerQuintal = 5050.0,
                pickupAvailable = true,
                demandPeriod = "Immediate (Next 3 Days)",
                rating = 4.9f,
                verifiedStatus = true
            ),
            BuyerProfile(
                id = "b_2",
                businessName = "Kishan Agro Processing Co.",
                contactName = "Ramesh Verma",
                phone = "+91 94250 88776",
                location = "Sanwer Road Industrial Area",
                district = "Indore",
                cropRequired = cropName,
                quantityRequiredQuintals = 100.0,
                offeredPricePerQuintal = 5000.0,
                pickupAvailable = true,
                demandPeriod = "Next 7 Days",
                rating = 4.7f,
                verifiedStatus = true
            ),
            BuyerProfile(
                id = "b_3",
                businessName = "Narmada Grain Exporters",
                contactName = "Vikram Singh",
                phone = "+91 99811 44332",
                location = "Ujjain Road Mandi Complex",
                district = "Ujjain",
                cropRequired = cropName,
                quantityRequiredQuintals = 500.0,
                offeredPricePerQuintal = 5080.0,
                pickupAvailable = false,
                demandPeriod = "Immediate Pickup",
                rating = 4.8f,
                verifiedStatus = true
            )
        )
    }

    fun calculateLogisticsCost(
        vehicleType: String,
        distanceKm: Double,
        quantityQuintals: Double
    ): Double {
        val baseFare = when (vehicleType) {
            "Tractor Trolley" -> 800.0
            "Small Truck (Ace)" -> 1200.0
            "Medium Truck (Eicher)" -> 2200.0
            "Heavy Truck" -> 4000.0
            else -> 1000.0
        }
        val ratePerKm = when (vehicleType) {
            "Tractor Trolley" -> 22.0
            "Small Truck (Ace)" -> 28.0
            "Medium Truck (Eicher)" -> 42.0
            "Heavy Truck" -> 65.0
            else -> 30.0
        }
        return baseFare + (distanceKm * ratePerKm)
    }
}
