package com.geocue.android.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.geocue.android.domain.model.GeofenceLocation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: GeofenceListUiState,
    onAddReminderClick: () -> Unit,
    onToggleReminder: (GeofenceLocation, Boolean) -> Unit,
    onDeleteReminder: (GeofenceLocation) -> Unit,
    onEditReminder: (GeofenceLocation) -> Unit,
    onShowNotificationHistory: () -> Unit = {},
    notificationCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "GeoCue",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(onClick = onShowNotificationHistory) {
                        BadgedBox(
                            badge = {
                                if (notificationCount > 0) {
                                    Badge {
                                        Text(
                                            text = notificationCount.toString(),
                                            fontSize = MaterialTheme.typography.labelSmall.fontSize
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notifications"
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                if (!state.canMonitorInBackground) {
                    PermissionBanner()
                }

                if (state.active.isEmpty() && state.inactive.isEmpty()) {
                    EmptyState(onAddTapped = onAddReminderClick)
                } else {
                    ReminderSection(
                        title = "Active reminders",
                        reminders = state.active,
                        onToggle = onToggleReminder,
                        onDelete = onDeleteReminder,
                        onEdit = onEditReminder
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    ReminderSection(
                        title = "Inactive reminders",
                        reminders = state.inactive,
                        onToggle = onToggleReminder,
                        onDelete = onDeleteReminder,
                        onEdit = onEditReminder
                    )
                }
            }

            FloatingActionButton(
                onClick = onAddReminderClick,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Icon(imageVector = Icons.Outlined.Add, contentDescription = null)
            }
        }
    }
}

@Composable
private fun ReminderSection(
    title: String,
    reminders: List<GeofenceLocation>,
    onToggle: (GeofenceLocation, Boolean) -> Unit,
    onDelete: (GeofenceLocation) -> Unit,
    onEdit: (GeofenceLocation) -> Unit,
) {
    if (reminders.isEmpty()) return

    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        modifier = Modifier.padding(vertical = 12.dp)
    )

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(reminders, key = { it.id }) { reminder ->
            GeofenceCard(reminder = reminder, onToggle = onToggle, onDelete = onDelete, onEdit = onEdit)
        }
    }
}

@Composable
private fun GeofenceCard(
    reminder: GeofenceLocation,
    onToggle: (GeofenceLocation, Boolean) -> Unit,
    onDelete: (GeofenceLocation) -> Unit,
    onEdit: (GeofenceLocation) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = reminder.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (reminder.address.isNotBlank()) {
                Text(
                    text = reminder.address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Radius: ${reminder.radius.toInt()} m",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = reminder.isEnabled,
                    onCheckedChange = { onToggle(reminder, it) }
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { onEdit(reminder) }) {
                    Icon(imageVector = Icons.Outlined.Edit, contentDescription = "Edit reminder")
                }
                IconButton(onClick = { onDelete(reminder) }) {
                    Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Delete reminder")
                }
            }
        }
    }
}

@Composable
private fun PermissionBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Outlined.LocationOff, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Enable background location",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "GeoCue needs \"Allow all the time\" access to trigger reminders while the app is closed.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun EmptyState(onAddTapped: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.NotificationsActive,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.height(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No reminders yet",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Create your first location reminder to get started.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        FilledIconButton(onClick = onAddTapped) {
            Icon(imageVector = Icons.Outlined.Add, contentDescription = null)
        }
    }
}
