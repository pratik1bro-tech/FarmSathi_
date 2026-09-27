package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.models.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,
    val name: String,
    val email: String,
    val phone: String,
    val preferredLanguage: String,
    val location: String,
    val farmingExperience: String,
    val optionalFarmInfo: String
) {
    fun toDomain() = User(
        uid = uid,
        name = name,
        email = email,
        phone = phone,
        preferredLanguage = preferredLanguage,
        location = location,
        farmingExperience = farmingExperience,
        optionalFarmInfo = optionalFarmInfo
    )

    companion object {
        fun fromDomain(user: User) = UserEntity(
            uid = user.uid,
            name = user.name,
            email = user.email,
            phone = user.phone,
            preferredLanguage = user.preferredLanguage,
            location = user.location,
            farmingExperience = user.farmingExperience,
            optionalFarmInfo = user.optionalFarmInfo
        )
    }
}
