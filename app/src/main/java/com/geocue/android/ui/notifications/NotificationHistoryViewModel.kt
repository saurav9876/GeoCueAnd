package com.geocue.android.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geocue.android.data.NotificationHistoryRepository
import com.geocue.android.domain.model.groupNotificationsByDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationHistoryUiState(
    val isLoading: Boolean = false,
    val notificationGroups: List<com.geocue.android.domain.model.NotificationDateGroup> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class NotificationHistoryViewModel @Inject constructor(
    private val repository: NotificationHistoryRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(NotificationHistoryUiState())
    val uiState: StateFlow<NotificationHistoryUiState> = _uiState.asStateFlow()
    
    init {
        loadNotifications()
    }
    
    private fun loadNotifications() {
        viewModelScope.launch {
            repository.getNotificationsFromLastSevenDays()
                .onStart { _uiState.value = _uiState.value.copy(isLoading = true) }
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
                .collect { notifications ->
                    val groups = groupNotificationsByDate(notifications)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        notificationGroups = groups,
                        error = null
                    )
                }
        }
    }
    
    fun clearAllNotifications() {
        viewModelScope.launch {
            try {
                repository.clearAllNotifications()
                loadNotifications()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun deleteOldNotifications() {
        viewModelScope.launch {
            try {
                repository.deleteOldNotifications()
                loadNotifications()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun refresh() {
        loadNotifications()
    }
}
