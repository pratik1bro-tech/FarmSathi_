package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmDao {
    @Query("SELECT * FROM farms WHERE userId = :userId ORDER BY createdAt DESC")
    fun getFarmsForUser(userId: String): Flow<List<FarmEntity>>

    @Query("SELECT * FROM farms WHERE id = :id AND userId = :userId LIMIT 1")
    suspend fun getFarmById(id: String, userId: String): FarmEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarm(farm: FarmEntity)

    @Update
    suspend fun updateFarm(farm: FarmEntity)

    @Query("DELETE FROM farms WHERE id = :id AND userId = :userId")
    suspend fun deleteFarm(id: String, userId: String)
}
