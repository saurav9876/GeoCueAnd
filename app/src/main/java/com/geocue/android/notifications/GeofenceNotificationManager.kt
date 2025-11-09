package com.geocue.android.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.geocue.android.MainActivity
import com.geocue.android.R
import com.geocue.android.data.local.GeoCueDatabase
import com.geocue.android.data.local.NotificationHistoryEntity
import com.geocue.android.domain.model.GeofenceLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

class GeofenceNotificationManager(
    private val context: Context,
    private val channels: NotificationChannels,
    private val database: GeoCueDatabase? = null
) {
    private val notificationManager = NotificationManagerCompat.from(context)
    private val throttledEvents = ConcurrentHashMap<String, Instant>()

    fun notifyEvent(location: GeofenceLocation, transitionType: GeofenceTransition) {
        if (!notificationManager.areNotificationsEnabled()) return

        channels.ensureChannels()

        val message = when (transitionType) {
            GeofenceTransition.ENTRY -> if (location.entryMessage.isNotBlank()) {
                location.entryMessage
            } else {
                "You arrived at ${location.name}"
            }
            GeofenceTransition.EXIT -> if (location.exitMessage.isNotBlank()) {
                location.exitMessage
            } else {
                "You left ${location.name}"
            }
        }

        val throttleKey = "${location.id}:${transitionType.name}"
        val lastSent = throttledEvents[throttleKey]
        if (lastSent != null && Instant.now().minusSeconds(60).isBefore(lastSent)) {
            return
        }

        throttledEvents[throttleKey] = Instant.now()

        // Deep link to notification history
        val deepLinkIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("geocue://notifications/${location.id}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            location.id.hashCode(),
            deepLinkIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = when (transitionType) {
            GeofenceTransition.ENTRY -> "Arrived at ${location.name}"
            GeofenceTransition.EXIT -> "Left ${location.name}"
        }

        val notification = NotificationCompat.Builder(context, channels.geofenceChannelId)
            .setSmallIcon(R.drawable.ic_stat_geofence)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(Random.nextInt(), notification)

        // Save to database
        saveNotificationToDatabase(location, transitionType, message, title)
    }

    private fun saveNotificationToDatabase(
        location: GeofenceLocation,
        transitionType: GeofenceTransition,
        message: String,
        title: String
    ) {
        database?.let { db ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val entity = NotificationHistoryEntity(
                        reminderId = location.id.toString(),
                        reminderName = location.name,
                        title = title,
                        message = message,
                        type = transitionType.name,
                        timestamp = System.currentTimeMillis()
                    )
                    db.notificationHistoryDao().insertNotification(entity)
                    
                    // Delete notifications older than 7 days
                    val sevenDaysAgoMs = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
                    db.notificationHistoryDao().deleteOldNotifications(sevenDaysAgoMs)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}

enum class GeofenceTransition { ENTRY, EXIT }
