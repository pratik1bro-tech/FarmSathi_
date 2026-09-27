package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanDao {
    @Query("SELECT * FROM crop_scans WHERE userId = :userId ORDER BY timestamp DESC")
    fun getScansForUser(userId: String): Flow<List<ScanEntity>>

    @Query("SELECT * FROM crop_scans WHERE id = :id AND userId = :userId LIMIT 1")
    suspend fun getScanById(id: String, userId: String): ScanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: ScanEntity)

    @Query("DELETE FROM crop_scans WHERE id = :id AND userId = :userId")
    suspend fun deleteScan(id: String, userId: String)
}
