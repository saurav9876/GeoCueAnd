package com.geocue.android.data

import com.geocue.android.data.local.GeoCueDatabase
import com.geocue.android.data.local.NotificationHistoryEntity
import com.geocue.android.domain.model.NotificationHistoryItem
import com.geocue.android.domain.model.NotificationType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing notification history
 */
@Singleton
class NotificationHistoryRepository @Inject constructor(
    private val database: GeoCueDatabase
) {
    
    /**
     * Get all notifications from the last 7 days
     */
    fun getNotificationsFromLastSevenDays(): Flow<List<NotificationHistoryItem>> {
        val sevenDaysAgoMs = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
        return database.notificationHistoryDao()
            .getNotificationsFromLastSevenDays(sevenDaysAgoMs)
            .map { entities ->
                entities.map { it.toDomainModel() }
            }
    }
    
    /**
     * Get notifications for a specific reminder
     */
    fun getNotificationsForReminder(reminderId: UUID): Flow<List<NotificationHistoryItem>> {
        return database.notificationHistoryDao()
            .getNotificationsForReminder(reminderId.toString())
            .map { entities ->
                entities.map { it.toDomainModel() }
            }
    }
    
    /**
     * Clear all notifications
     */
    suspend fun clearAllNotifications() {
        database.notificationHistoryDao().clearAllNotifications()
    }
    
    /**
     * Delete old notifications (older than 7 days)
     */
    suspend fun deleteOldNotifications() {
        val sevenDaysAgoMs = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
        database.notificationHistoryDao().deleteOldNotifications(sevenDaysAgoMs)
    }
}

/**
 * Extension function to convert entity to domain model
 */
private fun NotificationHistoryEntity.toDomainModel(): NotificationHistoryItem {
    return NotificationHistoryItem(
        id = UUID.fromString(this.id),
        reminderId = UUID.fromString(this.reminderId),
        reminderName = this.reminderName,
        title = this.title,
        message = this.message,
        type = NotificationType.valueOf(this.type),
        timestamp = Instant.ofEpochMilli(this.timestamp)
    )
}
