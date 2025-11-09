package com.geocue.android

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.geocue.android.domain.model.GeofenceLocation
import com.geocue.android.permissions.PermissionChecker
import com.geocue.android.ui.home.AddReminderSheet
import com.geocue.android.ui.home.AddReminderViewModel
import com.geocue.android.ui.home.GeofenceListViewModel
import com.geocue.android.ui.home.HomeScreen
import com.geocue.android.ui.map.MapScreen
import com.geocue.android.ui.map.MapViewModel
import com.geocue.android.ui.notifications.NotificationHistorySheet
import com.geocue.android.ui.settings.SettingsScreen
import com.geocue.android.ui.settings.SettingsViewModel
import com.geocue.android.ui.theme.GeoCueTheme
import com.geocue.android.ui.notifications.NotificationHistoryViewModel
import java.util.UUID
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun GeoCueApp() {
    GeoCueTheme {
        val snackbarHostState = remember { SnackbarHostState() }
        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        val permissionChecker = remember {
            EntryPointAccessors.fromApplication<PermissionCheckerEntryPoint>(context.applicationContext)
                .permissionChecker()
        }

        val locationPermissionsLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val granted = permissions.values.any { it }
            if (!granted) {
                scope.launch {
                    snackbarHostState.showSnackbar("Location permission is required for geofencing")
                }
            }
        }

        val notificationLauncher = rememberLauncherForActivityResult(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityResultContracts.RequestPermission()
            } else {
                ActivityResultContracts.RequestPermission()
            }
        ) { granted ->
            if (!granted) {
                scope.launch {
                    snackbarHostState.showSnackbar("Enable notifications to receive reminders")
                }
            }
        }

        LaunchedEffect(Unit) {
            if (!permissionChecker.hasLocationPermission()) {
                locationPermissionsLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !permissionChecker.hasNotificationsPermission()) {
                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val tabs = remember {
            listOf(
                GeoCueTab("home", "Home", Icons.Filled.Home),
                GeoCueTab("map", "Map", Icons.Filled.Map),
                GeoCueTab("settings", "Settings", Icons.Filled.Settings)
            )
        }

        val (selectedTab, setSelectedTab) = rememberSaveable { mutableStateOf(tabs.first().id) }

        val homeViewModel: GeofenceListViewModel = hiltViewModel()
        val homeState by homeViewModel.uiState.collectAsStateWithLifecycle()

        val mapViewModel: MapViewModel = hiltViewModel()
        val mapState by mapViewModel.uiState.collectAsStateWithLifecycle()

        val settingsViewModel: SettingsViewModel = hiltViewModel()
        val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()

        val addReminderViewModel: AddReminderViewModel = hiltViewModel()
        val addReminderState by addReminderViewModel.state.collectAsStateWithLifecycle()

        val notificationHistoryViewModel: NotificationHistoryViewModel = hiltViewModel()
        val notificationHistoryState by notificationHistoryViewModel.uiState.collectAsStateWithLifecycle()

        var showAddReminder by rememberSaveable { mutableStateOf(false) }
        var editingReminder by remember { mutableStateOf<GeofenceLocation?>(null) }
        var showNotificationHistory by rememberSaveable { mutableStateOf(false) }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = tab.id == selectedTab,
                            onClick = { setSelectedTab(tab.id) },
                            icon = { androidx.compose.material3.Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            when (selectedTab) {
                "home" -> HomeScreen(
                    state = homeState,
                    onAddReminderClick = {
                        addReminderViewModel.reset()
                        addReminderViewModel.prepare()
                        showAddReminder = true
                    },
                    onToggleReminder = { location, enabled -> homeViewModel.toggleEnabled(location, enabled) },
                    onDeleteReminder = { location -> homeViewModel.delete(location) },
                    onEditReminder = { location ->
                        editingReminder = location
                        addReminderViewModel.loadReminderForEditing(location)
                        showAddReminder = true
                    },
                    onShowNotificationHistory = { showNotificationHistory = true },
                    notificationCount = notificationHistoryState.notificationGroups.sumOf { it.items.size },
                    modifier = Modifier.padding(innerPadding)
                )
                "map" -> MapScreen(
                    state = mapState,
                    onCenterOnUser = { mapViewModel.refreshLocation() },
                    modifier = Modifier.padding(innerPadding)
                )
                "settings" -> SettingsScreen(
                    state = settingsState,
                    onRefresh = { settingsViewModel.refreshPermissions() },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }

        if (showAddReminder) {
            AddReminderSheet(
                state = addReminderState,
                onDismiss = {
                    showAddReminder = false
                    editingReminder = null
                    addReminderViewModel.reset()
                },
                onConfirm = {
                    val request = addReminderViewModel.buildRequest()
                    if (request != null) {
                        if (editingReminder != null) {
                            // Update existing reminder
                            homeViewModel.updateReminder(
                                editingReminder!!.copy(
                                    name = request.name,
                                    address = request.address,
                                    latitude = request.latitude ?: editingReminder!!.latitude,
                                    longitude = request.longitude ?: editingReminder!!.longitude,
                                    radius = request.radius,
                                    entryMessage = request.entryMessage,
                                    exitMessage = request.exitMessage,
                                    notifyOnEntry = request.notifyOnEntry,
                                    notifyOnExit = request.notifyOnExit
                                )
                            )
                        } else {
                            // Create new reminder
                            homeViewModel.addReminder(request)
                        }
                        showAddReminder = false
                        editingReminder = null
                        addReminderViewModel.reset()
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("Select a location and name your reminder")
                        }
                    }
                },
                onQueryChange = addReminderViewModel::onQueryChange,
                onNameChange = addReminderViewModel::updateName,
                onSelectResult = addReminderViewModel::selectResult,
                onRadiusChange = addReminderViewModel::updateRadius,
                onEntryMessageChange = addReminderViewModel::updateEntryMessage,
                onExitMessageChange = addReminderViewModel::updateExitMessage,
                onToggleEntry = addReminderViewModel::toggleNotifyOnEntry,
                onToggleExit = addReminderViewModel::toggleNotifyOnExit,
                onUseCurrentLocation = addReminderViewModel::useCurrentLocation,
                onSelectCoordinates = addReminderViewModel::selectCoordinates,
                editingReminder = editingReminder
            )
        }

        if (showNotificationHistory) {
            NotificationHistorySheet(
                onDismiss = { showNotificationHistory = false },
                notifications = notificationHistoryState.notificationGroups,
                isLoading = notificationHistoryState.isLoading,
                onClearAll = { notificationHistoryViewModel.clearAllNotifications() },
                onDeleteOld = { notificationHistoryViewModel.deleteOldNotifications() }
            )
        }
    }
}

data class GeoCueTab(val id: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface PermissionCheckerEntryPoint {
    fun permissionChecker(): PermissionChecker
}
