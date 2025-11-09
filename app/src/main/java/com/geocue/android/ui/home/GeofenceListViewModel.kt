package com.geocue.android.ui.home

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geocue.android.domain.GeofenceInteractor
import com.geocue.android.domain.model.GeofenceLocation
import com.geocue.android.domain.model.NotificationMode
import com.geocue.android.location.AndroidLocationClient
import com.geocue.android.permissions.PermissionChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class GeofenceListViewModel @Inject constructor(
    private val interactor: GeofenceInteractor,
    private val locationClient: AndroidLocationClient,
    private val permissionChecker: PermissionChecker
) : ViewModel() {

    private val _lastKnownLocation = MutableStateFlow<Location?>(null)
    val lastKnownLocation: StateFlow<Location?> = _lastKnownLocation

    val uiState: StateFlow<GeofenceListUiState> =
        interactor.observe()
            .combine(_lastKnownLocation) { geofences, location ->
                GeofenceListUiState(
                    active = geofences.filter { it.isEnabled },
                    inactive = geofences.filterNot { it.isEnabled },
                    canMonitorInBackground = permissionChecker.hasBackgroundLocationPermission(),
                    lastKnownLocation = location
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = GeofenceListUiState()
            )

    init {
        viewModelScope.launch {
            interactor.observe().collect { geofences ->
                interactor.syncMonitoring(geofences)
                
                // FIX: Start continuous location updates when there are active geofences
                // This allows Android's geofencing service to detect boundary crossings immediately
                // instead of waiting 5-10 minutes for a random location event
                if (geofences.any { it.isEnabled } && permissionChecker.hasLocationPermission()) {
                    locationClient.observeLocationUpdates()
                        .collect { location ->
                            // Update last known location for UI display
                            _lastKnownLocation.value = location
                        }
                }
            }
        }

        viewModelScope.launch {
            if (permissionChecker.hasLocationPermission()) {
                _lastKnownLocation.value = locationClient.getCurrentLocation()
            }
        }
    }

    fun refreshLocation() {
        viewModelScope.launch {
            if (permissionChecker.hasLocationPermission()) {
                _lastKnownLocation.value = locationClient.getCurrentLocation()
            }
        }
    }

    fun toggleEnabled(location: GeofenceLocation, enabled: Boolean) {
        viewModelScope.launch {
            interactor.update(location.copy(isEnabled = enabled))
        }
    }

    fun delete(location: GeofenceLocation) {
        viewModelScope.launch {
            interactor.remove(location)
        }
    }

    fun updateReminder(location: GeofenceLocation) {
        viewModelScope.launch {
            interactor.update(location)
        }
    }

    fun addReminder(request: CreateGeofenceRequest) {
        viewModelScope.launch {
            if (!permissionChecker.hasLocationPermission()) return@launch
            val baseLocation = request.location ?: locationClient.getCurrentLocation()
            if (baseLocation != null) {
                val geofenceLocation = GeofenceLocation(
                    name = request.name,
                    address = request.address,
                    latitude = request.latitude ?: baseLocation.latitude,
                    longitude = request.longitude ?: baseLocation.longitude,
                    radius = request.radius,
                    entryMessage = request.entryMessage,
                    exitMessage = request.exitMessage,
                    notifyOnEntry = request.notifyOnEntry,
                    notifyOnExit = request.notifyOnExit,
                    notificationMode = request.notificationMode,
                    isEnabled = true
                )
                interactor.add(geofenceLocation)
            }
        }
    }
}

data class GeofenceListUiState(
    val active: List<GeofenceLocation> = emptyList(),
    val inactive: List<GeofenceLocation> = emptyList(),
    val canMonitorInBackground: Boolean = false,
    val lastKnownLocation: Location? = null
)

data class CreateGeofenceRequest(
    val name: String,
    val address: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val radius: Float = 150f,
    val entryMessage: String = "",
    val exitMessage: String = "",
    val notifyOnEntry: Boolean = true,
    val notifyOnExit: Boolean = false,
    val notificationMode: NotificationMode = NotificationMode.NORMAL,
    val location: Location? = null
)
