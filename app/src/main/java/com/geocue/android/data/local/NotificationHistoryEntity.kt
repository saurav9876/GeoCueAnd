package com.geocue.android.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.geocue.android.domain.model.NotificationType
import java.time.Instant
import java.util.UUID

/**
 * Room entity for storing notification history
 * Stores all geofence notifications for the last 7 days
 */
@Entity(tableName = "notification_history")
data class NotificationHistoryEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val reminderId: String,
    val reminderName: String,
    val title: String,
    val message: String,
    val type: String, // "ENTRY" or "EXIT"
    val timestamp: Long // milliseconds since epoch
)
