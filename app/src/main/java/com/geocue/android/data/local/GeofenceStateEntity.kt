package com.geocue.android.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tracks the current state of each geofence to prevent "drive-by" notifications.
 * This entity persists across app restarts and process deaths.
 */
@Entity(tableName = "geofence_states")
data class GeofenceStateEntity(
    @PrimaryKey val geofenceId: String,
    val hasDwelled: Boolean,  // True if user has dwelled (stayed for required time)
    val lastEnterTimestamp: Long,  // When user entered the geofence
    val lastExitTimestamp: Long   // When user exited the geofence
)

