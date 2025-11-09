package com.geocue.android.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.geocue.android.data.GeofenceRepository
import com.geocue.android.data.local.GeoCueDatabase
import com.geocue.android.data.local.GeofenceStateEntity
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GeofenceEventReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: GeofenceRepository
    @Inject lateinit var notificationManager: GeofenceNotificationManager
    @Inject lateinit var database: GeoCueDatabase

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val event = GeofencingEvent.fromIntent(intent) ?: return@runCatching
                if (event.hasError()) return@runCatching

                val geofences = repository.getGeofences().associateBy { it.id.toString() }
                val stateDao = database.geofenceStateDao()

                event.triggeringGeofences?.forEach { geofence ->
                    geofences[geofence.requestId]?.let { location ->
                        // Only process if the location is enabled
                        if (!location.isEnabled) return@let

                        val geofenceId = location.id.toString()
                        val currentTime = System.currentTimeMillis()

                        when (event.geofenceTransition) {
                            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                                // Record the ENTER event but don't notify yet
                                // This prevents drive-by notifications
                                stateDao.upsertState(
                                    GeofenceStateEntity(
                                        geofenceId = geofenceId,
                                        hasDwelled = false,
                                        lastEnterTimestamp = currentTime,
                                        lastExitTimestamp = 0L
                                    )
                                )
                            }

                            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                                // User has stayed long enough - this is a real "arrival"
                                if (location.notifyOnEntry) {
                                    notificationManager.notifyEvent(location, GeofenceTransition.ENTRY)
                                }
                                // Mark that we've dwelled in this location
                                stateDao.updateDwellState(geofenceId, true)
                            }

                            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                                // Only notify EXIT if we previously dwelled (stayed long enough)
                                val state = stateDao.getState(geofenceId)
                                if (state?.hasDwelled == true && location.notifyOnExit) {
                                    notificationManager.notifyEvent(location, GeofenceTransition.EXIT)
                                }
                                // Clean up the state after exit
                                stateDao.deleteState(geofenceId)
                            }
                        }
                    }
                }
            }
            pendingResult.finish()
        }
    }
}
