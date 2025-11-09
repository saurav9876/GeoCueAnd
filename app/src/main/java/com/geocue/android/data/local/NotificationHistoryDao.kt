package com.geocue.android.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO for accessing notification history from the database
 */
@Dao
interface NotificationHistoryDao {
    
    /**
     * Insert a new notification record
     */
    @Insert
    suspend fun insertNotification(notification: NotificationHistoryEntity)
    
    /**
     * Get all notifications from the last 7 days
     */
    @Query("""
        SELECT * FROM notification_history 
        WHERE timestamp > :sevenDaysAgoMs 
        ORDER BY timestamp DESC
    """)
    fun getNotificationsFromLastSevenDays(sevenDaysAgoMs: Long): Flow<List<NotificationHistoryEntity>>
    
    /**
     * Get all notifications for a specific reminder
     */
    @Query("""
        SELECT * FROM notification_history 
        WHERE reminderId = :reminderId 
        ORDER BY timestamp DESC
    """)
    fun getNotificationsForReminder(reminderId: String): Flow<List<NotificationHistoryEntity>>
    
    /**
     * Delete old notifications (older than 7 days)
     */
    @Query("DELETE FROM notification_history WHERE timestamp < :sevenDaysAgoMs")
    suspend fun deleteOldNotifications(sevenDaysAgoMs: Long)
    
    /**
     * Clear all notifications
     */
    @Query("DELETE FROM notification_history")
    suspend fun clearAllNotifications()
}
