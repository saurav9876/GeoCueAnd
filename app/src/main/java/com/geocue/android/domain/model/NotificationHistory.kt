package com.geocue.android.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

/**
 * Represents a notification event that was sent to the user
 */
data class NotificationHistoryItem(
    val id: UUID = UUID.randomUUID(),
    val reminderId: UUID,
    val reminderName: String,
    val title: String,
    val message: String,
    val type: NotificationType, // ENTRY or EXIT
    val timestamp: Instant = Instant.now()
) {
    /**
     * Get the local date of this notification
     */
    fun getLocalDate(): LocalDate {
        return timestamp.atZone(ZoneId.systemDefault()).toLocalDate()
    }

    /**
     * Get time ago string (e.g., "2 hours ago", "Yesterday")
     */
    fun getTimeAgoString(): String {
        val now = Instant.now()
        val durationSeconds = java.time.Duration.between(timestamp, now).seconds

        return when {
            durationSeconds < 60 -> "Just now"
            durationSeconds < 3600 -> "${durationSeconds / 60}m ago"
            durationSeconds < 86400 -> "${durationSeconds / 3600}h ago"
            durationSeconds < 172800 -> "Yesterday"
            durationSeconds < 604800 -> "${durationSeconds / 86400}d ago"
            else -> timestamp.atZone(ZoneId.systemDefault()).toLocalDate().toString()
        }
    }
}

enum class NotificationType {
    ENTRY,      // User entered geofence
    EXIT        // User exited geofence
}

/**
 * Group notification items by date
 */
data class NotificationDateGroup(
    val date: LocalDate,
    val displayName: String, // "TODAY", "YESTERDAY", "Dec 20"
    val items: List<NotificationHistoryItem>
)

/**
 * Helper function to group notifications by date
 */
fun groupNotificationsByDate(notifications: List<NotificationHistoryItem>): List<NotificationDateGroup> {
    if (notifications.isEmpty()) return emptyList()

    val today = LocalDate.now(ZoneId.systemDefault())
    val yesterday = today.minusDays(1)

    return notifications
        .groupBy { it.getLocalDate() }
        .toSortedMap(compareBy<LocalDate> { it }.reversed()) // Sort by date descending
        .map { (date, items) ->
            val displayName = when (date) {
                today -> "TODAY"
                yesterday -> "YESTERDAY"
                else -> date.toString() // Format as needed
            }
            NotificationDateGroup(date, displayName, items)
        }
}
