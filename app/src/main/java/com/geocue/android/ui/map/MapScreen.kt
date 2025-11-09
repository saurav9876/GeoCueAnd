package com.geocue.android.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.geocue.android.domain.model.GeofenceLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

@Composable
fun MapScreen(
    state: MapUiState,
    onCenterOnUser: () -> Unit,
    modifier: Modifier = Modifier
) {
    val initialLatLng = state.userLocation?.let { LatLng(it.latitude, it.longitude) }
        ?: LatLng(37.7749, -122.4194)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLatLng, 13f)
    }

    LaunchedEffect(state.userLocation) {
        state.userLocation?.let { location ->
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 15f))
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = state.userLocation != null),
            uiSettings = MapUiSettings(myLocationButtonEnabled = false)
        ) {
            state.geofences.forEach { geofence ->
                GeofenceMarker(geofence)
            }
        }

        FloatingActionButton(
            onClick = onCenterOnUser,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(imageVector = Icons.Outlined.MyLocation, contentDescription = null)
        }
    }
}

@Composable
private fun GeofenceMarker(geofence: GeofenceLocation) {
    val latLng = LatLng(geofence.latitude, geofence.longitude)
    Marker(
        state = rememberMarkerState(position = latLng),
        title = geofence.name,
        snippet = geofence.address.takeIf { it.isNotBlank() }
    )

    Circle(
        center = latLng,
        radius = geofence.radius.toDouble(),
        fillColor = Color(0x332196F3),
        strokeColor = Color(0xFF2196F3.toInt()),
        strokeWidth = 2f
    )
}
