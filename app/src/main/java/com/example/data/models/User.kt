package com.example.data.models

data class User(
    val uid: String,
    val name: String,
    val email: String,
    val phone: String = "",
    val preferredLanguage: String = "English", // "English", "Hindi", "Hinglish"
    val location: String = "Madhya Pradesh, India",
    val farmingExperience: String = "3-5 years",
    val optionalFarmInfo: String = ""
)
