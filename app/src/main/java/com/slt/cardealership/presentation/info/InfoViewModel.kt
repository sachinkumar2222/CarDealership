package com.slt.cardealership.presentation.info

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.data.local.SessionManager
import com.slt.cardealership.domain.model.DealerInfo
import com.slt.cardealership.domain.model.HomeDelivery
import com.slt.cardealership.domain.repo.DealerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// --- Events for showing snackbars/toasts ---
sealed interface InfoEvent {
    data class ShowSuccess(val message: String) : InfoEvent
    data class ShowError(val message: String) : InfoEvent
}

sealed class InfoUiState {
    object Loading : InfoUiState()
    data class Success(
        val dealerInfo: DealerInfo,
        val isSaving: Boolean = false // Shows loading on the save button
    ) : InfoUiState()
    data class Error(val message: String) : InfoUiState()
}

@HiltViewModel
class InfoViewModel @Inject constructor(
    private val dealerRepository: DealerRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val TAG = "InfoViewModel"

    private val _uiState = MutableStateFlow<InfoUiState>(InfoUiState.Loading)
    val uiState: StateFlow<InfoUiState> = _uiState.asStateFlow()

    private val _events = Channel<InfoEvent>()
    val events = _events.receiveAsFlow()

    // Store the dealer ID to avoid passing it around
    private var currentDealerId: Long? = null

    init {
        fetchDealerInfo()
    }

    fun fetchDealerInfo() {
        viewModelScope.launch {
            _uiState.value = InfoUiState.Loading

            val dealerId = sessionManager.getDealerId()?.toLong()
            if (dealerId == null) {
                _uiState.value = InfoUiState.Error("Could not find saved Dealer ID.")
                return@launch
            }
            currentDealerId = dealerId // Save the dealer ID for later

            dealerRepository.getCombinedDealerInfo(dealerId)
                .onSuccess { combinedInfo ->
                    _uiState.value = InfoUiState.Success(combinedInfo)
                }
                .onFailure { error ->
                    Log.e(TAG, "Failed to fetch dealer info", error)
                    _uiState.value = InfoUiState.Error(error.message ?: "An unknown error occurred")
                }
        }
    }

    /**
     * Generic function to update a single field using the PATCH API.
     * @param fieldName The JSON key (e.g., "phone", "name", "is_virtual")
     * @param value The new value (e.g., "12345", "New Name", true)
     */
    fun updateField(fieldName: String, value: Any) {
        val updateMap = mapOf(fieldName to value)
        saveUpdates(updateMap, "Info updated")
    }

    /**
     * Specific function to update Home Delivery settings.
     * This is needed because the UI sends 3 separate values.
     */
    fun updateHomeDelivery(isAvailable: Boolean, isNationWide: Boolean, radius: String) {
        // Here you must decide what the API expects. Does it take a complex object
        // or individual fields? I'll assume individual fields based on the HomeDelivery model.
        val updateMap = mapOf(
            "home_delivery_available" to isAvailable, // Guessed API key
            "home_delivery_nationwide" to isNationWide, // Guessed API key
            "home_delivery_radius" to (radius.toIntOrNull() ?: 0) // Guessed API key
        )
        // TODO: You MUST confirm the API keys above (e.g., "home_delivery_available")
        Log.w(TAG, "updateHomeDelivery: API keys are guessed. Please verify them.")
        saveUpdates(updateMap, "Home Delivery updated")
    }

    // You would add another function here for updateHomeTestDrive, etc.

    /**
     * The master save function that all other update functions call.
     * It handles showing the loading spinner, making the API call,
     * and refreshing the data on success.
     */
    internal fun saveUpdates(updateMap: Map<String, Any>, successMessage: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState !is InfoUiState.Success) return@launch

            val dealerId = currentDealerId
            if (dealerId == null) {
                _events.send(InfoEvent.ShowError("User session error."))
                return@launch
            }

            // 1. Show loading spinner
            _uiState.value = currentState.copy(isSaving = true)
            Log.d(TAG, "Updating dealer info with: $updateMap")

            // 2. Call the repository
            dealerRepository.updateDealerInfo(dealerId, updateMap)
                .onSuccess {
                    Log.d(TAG, "Update successful")
                    _events.send(InfoEvent.ShowSuccess(successMessage))
                    // 3. Refresh all data on success
                    fetchDealerInfo() // This will reset isSaving to false
                }
                .onFailure { error ->
                    Log.e(TAG, "Update failed", error)
                    _uiState.value = currentState.copy(isSaving = false) // Stop loading on fail
                    _events.send(InfoEvent.ShowError(error.message ?: "Update failed"))
                }
        }
    }
}