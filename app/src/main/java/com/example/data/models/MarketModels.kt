package com.example.data.models

data class BuyerProfile(
    val id: String,
    val businessName: String,
    val contactName: String,
    val phone: String,
    val location: String,
    val district: String,
    val cropRequired: String,
    val quantityRequiredQuintals: Double,
    val offeredPricePerQuintal: Double,
    val pickupAvailable: Boolean = true,
    val demandPeriod: String = "Immediate Purchase (Next 7 Days)",
    val rating: Float = 4.8f,
    val verifiedStatus: Boolean = true
)

data class SellingRequest(
    val id: String,
    val userId: String,
    val farmerName: String,
    val cropName: String,
    val quantityQuintals: Double,
    val expectedPricePerQuintal: Double,
    val buyerId: String,
    val buyerName: String,
    val mandiName: String,
    val status: String = "Submitted", // "Submitted", "Negotiating", "Accepted", "Completed"
    val createdAt: Long = System.currentTimeMillis()
)

data class LogisticsRequest(
    val id: String,
    val userId: String,
    val farmerName: String,
    val pickupLocation: String,
    val destinationMandi: String,
    val cropName: String,
    val quantityQuintals: Double,
    val vehicleType: String, // "Tractor Trolley", "Small Truck (Ace)", "Medium Truck (Eicher)", "Heavy Truck"
    val estimatedDistanceKm: Double,
    val estimatedCostInr: Double,
    val pickupDate: String,
    val status: String = "Booked", // "Booked", "Driver Assigned", "In Transit", "Delivered"
    val createdAt: Long = System.currentTimeMillis()
)
