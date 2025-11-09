package com.geocue.android.domain

import com.geocue.android.data.GeofenceRepository
import com.geocue.android.domain.model.GeofenceLocation
import com.geocue.android.location.GeofenceController
import com.geocue.android.permissions.PermissionChecker
import javax.inject.Inject

import kotlinx.coroutines.flow.Flow

class GeofenceInteractor @Inject constructor(
    private val repository: GeofenceRepository,
    private val controller: GeofenceController,
    private val permissionChecker: PermissionChecker
) {
    fun observe(): Flow<List<GeofenceLocation>> = repository.observeGeofences()

    suspend fun add(location: GeofenceLocation) {
        repository.upsert(location)
        if (location.isEnabled && permissionChecker.hasBackgroundLocationPermission()) {
            controller.registerGeofence(location)
        }
    }

    suspend fun update(location: GeofenceLocation) {
        repository.upsert(location)
        controller.removeGeofence(location)
        if (location.isEnabled && permissionChecker.hasBackgroundLocationPermission()) {
            controller.registerGeofence(location)
        }
    }

    suspend fun remove(location: GeofenceLocation) {
        repository.delete(location)
        controller.removeGeofence(location)
    }

    suspend fun syncMonitoring(geofences: List<GeofenceLocation>) {
        controller.removeAll()
        if (!permissionChecker.hasBackgroundLocationPermission()) return
        geofences
            .filter { it.isEnabled }
            .forEach { controller.registerGeofence(it, useInitialTrigger = false) }
    }
}
