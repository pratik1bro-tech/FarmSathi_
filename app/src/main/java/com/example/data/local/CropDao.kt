package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CropDao {
    @Query("SELECT * FROM crops WHERE userId = :userId ORDER BY cropName ASC")
    fun getCropsForUser(userId: String): Flow<List<CropEntity>>

    @Query("SELECT * FROM crops WHERE farmId = :farmId AND userId = :userId")
    fun getCropsForFarm(farmId: String, userId: String): Flow<List<CropEntity>>

    @Query("SELECT * FROM crops WHERE id = :id AND userId = :userId LIMIT 1")
    suspend fun getCropById(id: String, userId: String): CropEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrop(crop: CropEntity)

    @Update
    suspend fun updateCrop(crop: CropEntity)

    @Query("DELETE FROM crops WHERE id = :id AND userId = :userId")
    suspend fun deleteCrop(id: String, userId: String)
}
