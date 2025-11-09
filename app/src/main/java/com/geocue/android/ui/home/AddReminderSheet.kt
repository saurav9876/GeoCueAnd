package com.geocue.android.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.geocue.android.domain.model.GeofenceLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
 

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderSheet(
    state: AddReminderUiState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onQueryChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onSelectResult: (PlaceSearchResult) -> Unit,
    onRadiusChange: (Float) -> Unit,
    onEntryMessageChange: (String) -> Unit,
    onExitMessageChange: (String) -> Unit,
    onToggleEntry: (Boolean) -> Unit,
    onToggleExit: (Boolean) -> Unit,
    onUseCurrentLocation: () -> Unit,
    onSelectCoordinates: (Double, Double) -> Unit,
    editingReminder: GeofenceLocation? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (editingReminder != null) "Edit reminder" else "Create reminder",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = state.name,
                onValueChange = onNameChange,
                label = { Text("Reminder name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                label = { Text("Search for a place") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            SearchResultsSection(
                state = state,
                onSelectResult = onSelectResult,
                modifier = Modifier.fillMaxWidth()
            )

            if (state.currentLocation != null) {
                TextButton(onClick = onUseCurrentLocation) {
                    Text("Use my current location")
                }
            }

            MapPreview(state = state, onSelectCoordinates = onSelectCoordinates)
            Text(
                text = "Tap or long-press the map to drop a pin.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Radius: ${state.radius.toInt()} m", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = state.radius,
                    onValueChange = onRadiusChange,
                    valueRange = 50f..500f
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Notifications", style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Notify on entry", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "Play this reminder when you arrive.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = state.notifyOnEntry, onCheckedChange = onToggleEntry)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Notify on exit", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "Trigger when leaving the location.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = state.notifyOnExit, onCheckedChange = onToggleExit)
                }
            }

            OutlinedTextField(
                value = state.entryMessage,
                onValueChange = onEntryMessageChange,
                label = { Text("Entry message (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.exitMessage,
                onValueChange = onExitMessageChange,
                label = { Text("Exit message (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    enabled = state.selectedLocation != null && state.name.isNotBlank()
                ) {
                    Text("Save")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SearchResultsSection(
    state: AddReminderUiState,
    onSelectResult: (PlaceSearchResult) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.heightIn(max = 240.dp),
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        when {
            state.isSearching -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.height(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Searching...")
                }
            }
            state.results.isEmpty() -> {
                val message = when {
                    state.searchError != null -> state.searchError
                    state.query.length >= 3 -> "No places found. Try another search."
                    else -> "Type at least three characters to search for a place."
                }
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
            else -> {
                LazyColumn {
                    itemsIndexed(state.results) { index, result ->
                        PlaceRow(
                            result = result,
                            isSelected = result == state.selectedLocation,
                            onClick = { onSelectResult(result) }
                        )
                        if (index < state.results.lastIndex) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaceRow(
    result: PlaceSearchResult,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(imageVector = Icons.Outlined.LocationOn, contentDescription = null, tint = color)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = result.name, style = MaterialTheme.typography.bodyLarge.copy(color = color))
            result.address?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun MapPreview(
    state: AddReminderUiState,
    onSelectCoordinates: (Double, Double) -> Unit
) {
    val fallback = state.selectedLocation?.let { LatLng(it.latitude, it.longitude) }
        ?: state.currentLocation?.let { LatLng(it.latitude, it.longitude) }
        ?: LatLng(DEFAULT_LAT, DEFAULT_LNG)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(fallback, 14f)
    }

    LaunchedEffect(state.selectedLocation) {
        state.selectedLocation?.let { selection ->
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(selection.latitude, selection.longitude), 16f))
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            GoogleMap(
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = false),
                onMapClick = { latLng -> onSelectCoordinates(latLng.latitude, latLng.longitude) },
            onMapLongClick = { latLng -> onSelectCoordinates(latLng.latitude, latLng.longitude) },
            onPOIClick = { poi -> onSelectCoordinates(poi.latLng.latitude, poi.latLng.longitude) }
            ) {
                state.selectedLocation?.let { selection ->
                    val center = LatLng(selection.latitude, selection.longitude)
                    val markerState = androidx.compose.runtime.remember(selection.latitude, selection.longitude) {
                        MarkerState(position = center)
                    }
                    Marker(
                        state = markerState,
                        title = selection.name
                    )
                    Circle(
                        center = center,
                        radius = state.radius.toDouble(),
                        fillColor = Color(0x332196F3),
                        strokeColor = Color(0xFF2196F3.toInt()),
                        strokeWidth = 2f
                    )
                }
            }

            
        }
    }
}

private const val DEFAULT_LAT = 37.7749
private const val DEFAULT_LNG = -122.4194
