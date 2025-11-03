package com.slt.cardealership.presentation.info

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.data.local.SessionManager
import com.slt.cardealership.domain.model.DealerInfo
import com.slt.cardealership.domain.model.HomeTestDrive
import com.slt.cardealership.domain.model.HourDetails
import com.slt.cardealership.domain.repo.DealerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Locale
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
            currentDealerId = dealerId

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
     * Updates fields on the main /dealer-api/Dealers/{id} endpoint (FormUrlEncoded).
     * Use this for: name, phone, website_url, description, address, city_name
     */
    fun saveDealerUpdates(updateMap: Map<String, Any>, successMessage: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState !is InfoUiState.Success) return@launch

            val dealerId = currentDealerId
            if (dealerId == null) {
                _events.send(InfoEvent.ShowError("User session error."))
                return@launch
            }

            _uiState.value = currentState.copy(isSaving = true)

            // Convert to Map<String, String> for FormUrlEncoded
            val stringUpdateMap = updateMap.mapValues { it.value.toString() }

            Log.d(TAG, "Updating dealer info (FormUrlEncoded): $stringUpdateMap")

            dealerRepository.updateDealerInfo(dealerId, stringUpdateMap)
                .onSuccess {
                    Log.d(TAG, "Update successful")
                    _events.send(InfoEvent.ShowSuccess(successMessage))
                    fetchDealerInfo() // Refresh data
                }
                .onFailure { error ->
                    Log.e(TAG, "Update failed", error)
                    _uiState.value = currentState.copy(isSaving = false)
                    _events.send(InfoEvent.ShowError(error.message ?: "Update failed"))
                }
        }
    }

    /**
     * Updates fields on the /dealer-api/dealer-metas/{id} endpoint (Multipart).
     * Use this for: amenities, home_delivery, home_test_drive, is_virtual
     */
    fun saveMetasUpdates(updateMap: Map<String, Any>, successMessage: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState !is InfoUiState.Success) return@launch
            val dealerId = currentDealerId
            if (dealerId == null) {
                _events.send(InfoEvent.ShowError("User session error."))
                return@launch
            }

            _uiState.value = currentState.copy(isSaving = true)

            // --- JSON-BASED FIXED VERSION ---

            // 1. Start with the original map
            val mutableUpdateMap = updateMap.toMutableMap()

            // 2. Add required fields
            val userId = sessionManager.getDealerId() ?: 630 // prefer dynamic user id
            mutableUpdateMap["created_by"] = userId
            mutableUpdateMap["updated_by"] = userId
            mutableUpdateMap["updated_on"] = System.currentTimeMillis() / 1000L

            Log.d(TAG, "Updating dealer metas (JSON): $mutableUpdateMap")

            // 3. Directly send the JSON body (no RequestBody conversion)
            dealerRepository.updateDealerMetas(dealerId, mutableUpdateMap)
                .onSuccess {
                    Log.d(TAG, "Update successful")
                    _events.send(InfoEvent.ShowSuccess(successMessage))
                    fetchDealerInfo() // Refresh after save
                }
                .onFailure { error ->
                    Log.e(TAG, "Update failed", error)
                    _uiState.value = currentState.copy(isSaving = false)
                    _events.send(InfoEvent.ShowError(error.message ?: "Update failed"))
                }
        }
    }


    /**
     * Specific function for Home Delivery.
     * Uses the 'home_delivery' key from your log.
     */
    fun updateHomeDelivery(isAvailable: Boolean, isNationWide: Boolean, radius: String) {
        val value = if (!isAvailable) "not_available" else if (isNationWide) "nation_wide" else radius
        // Your log shows "home_delivery" as the only key.
        // This is strange. Let's try sending ONLY that key first.
        // If it fails, we will add created_by/updated_by here too.
        val updateMap = mapOf("home_delivery" to (value ?: "not_available"))

        Log.d(TAG, "Updating Home Delivery: $updateMap")
        saveMetasUpdates(updateMap, "Home Delivery updated")
    }

    /**
     * Specific function for Home Test Drive.
     */
    fun updateHomeTestDrive(isAvailable: Boolean, radius: String) {
        // TODO: Verify these API keys! I am guessing.
        val updateMap = mapOf(
            "home_test_drive_available" to isAvailable,
            "home_test_drive_radius" to (radius.toIntOrNull() ?: 0)
        )
        Log.w(TAG, "updateHomeTestDrive: API keys are guessed. Please verify them.")
        saveMetasUpdates(updateMap, "Test Drive updated")
    }

    /**
     * Specific function for Amenities.
     */
    fun updateAmenities(wifi: Boolean,
                        parking: Boolean,
                        kidsArea: Boolean,
                        isEntrance: Boolean,  // <-- ADDED
                        isSeating: Boolean, // <-- ADDED
                        isRestroom: Boolean // <-- ADDED
        ) {
        val updateMap: Map<String, Any> = mapOf(
            "wifi" to wifi,
            "parking" to parking,
            "kids_play_area" to kidsArea,
            "is_entrance" to isEntrance, // <-- ADDED (Guessed API Key)
            "is_seating" to isSeating, // <-- ADDED (Guessed API Key)
            "is_restroom" to isRestroom // <-- ADDED (Guessed API Key)
        )
        Log.d(TAG, "Updating Amenities: $updateMap")
        // This will now call saveMetasUpdates, which adds the required _by and _on fields.
        saveMetasUpdates(updateMap, "Amenities updated")
    }

    fun updateBusinessHours(
        general: List<HourDetails>,
        parts: List<HourDetails>,
        service: List<HourDetails>
    ) {
        Log.d(TAG, "--- updateBusinessHours ---")
        val updateMap = mutableMapOf<String, Any>()

        // Helper to add hours for a specific type (general, parts, service)
        fun addHoursToMap(type: String, hours: List<HourDetails>) {
            hours.forEach { day ->
                val dayKey = day.day?.lowercase(Locale.ROOT) ?: return@forEach
                updateMap["${type}_${dayKey}_open_time"] = day.openTime ?: ""
                updateMap["${type}_${dayKey}_close_time"] = day.closeTime ?: ""
                updateMap["${type}_${dayKey}_is_close"] = day.isClose ?: false
            }
        }

        // Add all 3 types to the map
        addHoursToMap("general", general)
        addHoursToMap("parts", parts)
        addHoursToMap("service", service)

        Log.d(TAG, "Updating Business Hours with map: $updateMap")
        // Call the 'metas' endpoint, as hours are metadata
        saveMetasUpdates(updateMap, "Business hours updated")
    }
}