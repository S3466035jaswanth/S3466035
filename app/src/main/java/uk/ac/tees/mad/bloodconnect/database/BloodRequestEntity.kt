package uk.ac.tees.mad.bloodconnect.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blood_requests")
data class BloodRequestEntity(
    @PrimaryKey val id: String,
    val bloodGroup: String,
    val requesterName: String,
    val contact: String,
    val unitsRequired: String,
    val latitude: Double,
    val longitude: Double,
    val hospitalName: String,
    val documentImage: String?,
    val timestamp: Long
)