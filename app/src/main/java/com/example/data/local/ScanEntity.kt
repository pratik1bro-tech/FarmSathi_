package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.models.CropScan

@Entity(tableName = "crop_scans")
data class ScanEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val cropId: String,
    val cropName: String,
    val imagePathOrBase64: String,
    val possibleIssue: String,
    val severity: String,
    val confidence: String,
    val observationsJoined: String,
    val recommendedActionsJoined: String,
    val warningsJoined: String,
    val needsExpert: Boolean,
    val timestamp: Long
) {
    fun toDomain() = CropScan(
        id = id,
        userId = userId,
        cropId = cropId,
        cropName = cropName,
        imagePathOrBase64 = imagePathOrBase64,
        possibleIssue = possibleIssue,
        severity = severity,
        confidence = confidence,
        observations = if (observationsJoined.isBlank()) emptyList() else observationsJoined.split("|||"),
        recommendedActions = if (recommendedActionsJoined.isBlank()) emptyList() else recommendedActionsJoined.split("|||"),
        warnings = if (warningsJoined.isBlank()) emptyList() else warningsJoined.split("|||"),
        needsExpert = needsExpert,
        timestamp = timestamp
    )

    companion object {
        fun fromDomain(scan: CropScan) = ScanEntity(
            id = scan.id,
            userId = scan.userId,
            cropId = scan.cropId,
            cropName = scan.cropName,
            imagePathOrBase64 = scan.imagePathOrBase64,
            possibleIssue = scan.possibleIssue,
            severity = scan.severity,
            confidence = scan.confidence,
            observationsJoined = scan.observations.joinToString("|||"),
            recommendedActionsJoined = scan.recommendedActions.joinToString("|||"),
            warningsJoined = scan.warnings.joinToString("|||"),
            needsExpert = scan.needsExpert,
            timestamp = scan.timestamp
        )
    }
}
