package com.geocue.android.location

import android.Manifest
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission
import com.geocue.android.domain.model.GeofenceLocation
import com.geocue.android.notifications.GeofenceEventReceiver
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

class GeofenceController(private val app: Application) {
    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(app)

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(app, GeofenceEventReceiver::class.java)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        PendingIntent.getBroadcast(app, 0, intent, flags)
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun registerGeofence(location: GeofenceLocation, useInitialTrigger: Boolean = true) {
        val geofence = buildGeofence(location)
        val initialTrigger = if (useInitialTrigger) {
            GeofencingRequest.INITIAL_TRIGGER_ENTER or GeofencingRequest.INITIAL_TRIGGER_EXIT
        } else 0
        val request = GeofencingRequest.Builder()
            .setInitialTrigger(initialTrigger)
            .addGeofence(geofence)
            .build()

        geofencingClient.addGeofences(request, geofencePendingIntent)
    }

    fun removeGeofence(location: GeofenceLocation) {
        geofencingClient.removeGeofences(listOf(location.id.toString()))
    }

    fun removeAll() {
        geofencingClient.removeGeofences(geofencePendingIntent)
    }

    private fun buildGeofence(location: GeofenceLocation): Geofence = Geofence.Builder()
        .setRequestId(location.id.toString())
        .setCircularRegion(location.latitude, location.longitude, location.radius)
        .setTransitionTypes(
            Geofence.GEOFENCE_TRANSITION_ENTER or
                Geofence.GEOFENCE_TRANSITION_EXIT or
                Geofence.GEOFENCE_TRANSITION_DWELL
        )
        .setLoiteringDelay(30_000) // 30 seconds - prevents drive-by notifications but fast enough for real use
        .setExpirationDuration(Geofence.NEVER_EXPIRE)
        .build()
}
