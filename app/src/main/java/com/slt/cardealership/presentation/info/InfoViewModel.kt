package com.slt.cardealership.presentation.info

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.data.local.SessionManager
import com.slt.cardealership.domain.model.DealerInfo
import com.slt.cardealership.domain.model.HourDetails
import com.slt.cardealership.domain.model.ModifyDealerRequest
import com.slt.cardealership.domain.model.UpdateHoursRequest
import com.slt.cardealership.domain.model.UserPayload
import com.slt.cardealership.domain.model.UserProfile
import com.slt.cardealership.domain.repo.DealerRepository
import com.slt.cardealership.domain.model.Make
import com.slt.cardealership.domain.model.DomainMakeSetting
import com.slt.cardealership.domain.model.SocialProfileItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
        val userProfile: UserProfile? = null,
        val socialProfiles: List<SocialProfileItem> = emptyList(),
        val allMakes: List<Make> = emptyList(),
        val selectedMakes: List<DomainMakeSetting> = emptyList(),
        val domainId: Int? = null,
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

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

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

            // Fetch both DealerInfo, UserProfile, and Social Profiles
            val dealerInfoResult = async { dealerRepository.getCombinedDealerInfo(dealerId) }
            val userProfileResult = async { dealerRepository.getUserAuthorization() }
            val socialProfilesResult = async { dealerRepository.getSocialProfiles(dealerId) }

            val dealerInfo = dealerInfoResult.await()
            val userProfile = userProfileResult.await()
            val socialProfiles = socialProfilesResult.await()

            if (dealerInfo.isSuccess) {
                _uiState.value = InfoUiState.Success(
                    dealerInfo = dealerInfo.getOrThrow(),
                    userProfile = userProfile.getOrNull(),
                    socialProfiles = socialProfiles.getOrDefault(emptyList())
                )
                // Now fetch makes (needs dealerId, but happens after initial load to avoid blocking basic info)
                fetchMakesData()
            } else {
                val error = dealerInfo.exceptionOrNull()
                Log.e(TAG, "Failed to fetch dealer info", error)
                _uiState.value = InfoUiState.Error(error?.message ?: "An unknown error occurred")
            }
        }
    }

    /**
     * This is the new, separate function for uploading the header image.
     * It is called by the UI when the user selects a file.
     */
    fun onHeaderImageSelected(uri: Uri) {
        val currentState = _uiState.value
        if (currentState !is InfoUiState.Success) {
            viewModelScope.launch { _events.send(InfoEvent.ShowError("Please wait for data to load.")) }
            return
        }

        // This is your logic: "we fill other data from get"
        val currentDealerInfo = currentState.dealerInfo
        _selectedImageUri.value = uri // Update UI for preview

        // Now, we call the master save function with the new image
        saveFullDealerInfo(
            dealerInfo = currentDealerInfo, // "we fill other data from get"
            newImageUri = uri, // "use upload image as file in binary"
            successMessage = "Header image updated!"
        )
    }

    /**
     * --- 5. THIS IS THE NEW "MASTER" SAVE FUNCTION ---
     * Both image updates and text updates will call this function.
     * It safely builds the *entire* payload every time, as required by your PUT API.
     */
    private fun saveFullDealerInfo(
        dealerInfo: DealerInfo,
        newImageUri: Uri? = null,
        successMessage: String
    ) {
        val currentState = _uiState.value
        if (currentState !is InfoUiState.Success) return

        val dealerId = currentDealerId
        if (dealerId == null) {
            viewModelScope.launch { _events.send(InfoEvent.ShowError("User session error.")) }
            return
        }

        _uiState.value = currentState.copy(isSaving = true)

        viewModelScope.launch {
            try {
                // The repository will build the full payload
                val result = dealerRepository.updateDealerInfoWithImage(
                    dealerInfo = dealerInfo,
                    newImageUri = newImageUri
                )

                result.onSuccess { updatedDealerInfo ->
                    // Success! Update the UI with the new, complete info from the server
                    // Preserve the userProfile and socialProfiles when updating dealer info
                    val currentUserProfile = (currentState as? InfoUiState.Success)?.userProfile
                    val currentSocialProfiles = (currentState as? InfoUiState.Success)?.socialProfiles ?: emptyList()
                    _uiState.value = InfoUiState.Success(
                        dealerInfo = updatedDealerInfo,
                        userProfile = currentUserProfile,
                        socialProfiles = currentSocialProfiles
                    )
                    _events.send(InfoEvent.ShowSuccess(successMessage))
                    _selectedImageUri.value = null // Clear temporary URI
                }.onFailure { error ->
                    Log.e(TAG, "Update failed", error)
                    _uiState.value = currentState.copy(isSaving = false)
                    _events.send(InfoEvent.ShowError(error.message ?: "Update failed"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Update failed with exception", e)
                _uiState.value = currentState.copy(isSaving = false)
                _events.send(InfoEvent.ShowError(e.message ?: "An unknown error occurred"))
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

            // Convert Map<String, Any> to Map<String, String> for FormUrlEncoded
            val stringMap = updateMap.mapValues { entry ->
                entry.value.toString()
            }

            Log.d(TAG, "Patching Dealer Info: $stringMap")

            dealerRepository.updateDealerInfo(dealerId, stringMap)
                .onSuccess {
                    Log.d(TAG, "Update successful")
                    _events.send(InfoEvent.ShowSuccess(successMessage))

                    // Optimistic Update: Update the local state immediately
                    if (updateMap.containsKey("is_virtual")) {
                        val newIsVirtual = updateMap["is_virtual"] as? Boolean
                        _uiState.value = currentState.copy(
                            isSaving = false,
                            dealerInfo = currentState.dealerInfo.copy(isVirtual = newIsVirtual)
                        )
                    } else {
                        _uiState.value = currentState.copy(isSaving = false)
                    }

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

    fun saveOpeningDate(month: String, year: String) {
        val updateMap = mapOf(
            "business_established_month" to month,
            "business_established_year" to year
        )
        Log.d(TAG, "Updating Opening Date: $updateMap")
        saveMetasUpdates(updateMap, "Opening Date updated")
    }


    fun updateHomeDelivery(isAvailable: Boolean, isNationWide: Boolean, radius: String) {
        val updateMap = mutableMapOf<String, Any?>()

        if (!isAvailable) {
            updateMap["home_delivery"] = "no"
            updateMap["home_delivery_radius"] = null
        } else if (isNationWide) {
            updateMap["home_delivery"] = "nation_wide"
            updateMap["home_delivery_radius"] = null
        } else {
            // Assumes "radius" is the correct string when not nationwide
            // If it's just the radius number, the API logic is strange
            updateMap["home_delivery"] = "radius" // You may need to verify this string
            updateMap["home_delivery_radius"] = radius.toIntOrNull() ?: 0
        }

        Log.d(TAG, "Updating Home Delivery: $updateMap")
        saveMetasUpdates(updateMap as Map<String, Any>, "Home Delivery updated")
    }

    /**
     * Specific function for Home Test Drive.
     * This is now correct based on your new logs.
     */
    fun updateHomeTestDrive(isAvailable: Boolean, radius: String) {
        val updateMap: Map<String, Any> = mapOf(
            "home_test_drive" to isAvailable, // <-- FIX: Key is "home_test_drive"
            "home_test_drive_radius" to (radius.toIntOrNull() ?: 0)
        )
        Log.d(TAG, "Updating Home Test Drive: $updateMap")
        saveMetasUpdates(updateMap, "Test Drive updated")
    }

    fun updateIsVirtual(newValue: Boolean) {
        val updateMap = mapOf("is_virtual" to newValue)
        Log.d(TAG, "Updating Virtual Dealership: $updateMap")
        saveDealerUpdates(updateMap, "Virtual Dealership updated")
    }

    fun updateVirtualAppointment(isAvailable: Boolean, link: String) {
        val updateMap = mutableMapOf<String, Any?>()

        updateMap["virtual_appointment"] = isAvailable
        // Only send the link if it's available, otherwise send null or empty string
        updateMap["virtual_appointment_link"] = if (isAvailable) link else null

        Log.d(TAG, "Updating Virtual Appointment: $updateMap")
        saveMetasUpdates(updateMap as Map<String, Any>, "Virtual Appointment updated")
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
            "wheelchair_accessible_entrance" to isEntrance,
            "wheelchair_accessible_seating" to isSeating,
            "wheelchair_accessible_restroom" to isRestroom
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
        viewModelScope.launch {
            Log.d(TAG, "--- updateBusinessHours ---")

            val currentState = _uiState.value
            if (currentState !is InfoUiState.Success) return@launch
            val dealerId = currentDealerId
            if (dealerId == null) {
                _events.send(InfoEvent.ShowError("User session error."))
                return@launch
            }

            _uiState.value = currentState.copy(isSaving = true)

            // Create the three request objects based on your logs
            val generalRequest = UpdateHoursRequest(hour_type = "general", days = general)
            val partsRequest = UpdateHoursRequest(hour_type = "parts", days = parts)
            val serviceRequest = UpdateHoursRequest(hour_type = "service", days = service)

            // Launch all three requests in parallel
            val deferredGeneral = async { dealerRepository.updateBusinessHours(dealerId, generalRequest) }
            val deferredParts = async { dealerRepository.updateBusinessHours(dealerId, partsRequest) }
            val deferredService = async { dealerRepository.updateBusinessHours(dealerId, serviceRequest) }

            // Wait for all of them to finish
            val results = awaitAll(deferredGeneral, deferredParts, deferredService)

            // Check if *any* of them failed
            val failedRequest = results.firstOrNull { it.isFailure }

            if (failedRequest != null) {
                // At least one failed
                val error = failedRequest.exceptionOrNull()?.message ?: "Failed to update hours"
                Log.e(TAG, "Update failed for business hours: $error")
                _uiState.value = currentState.copy(isSaving = false)
                _events.send(InfoEvent.ShowError(error))
            } else {
                // All succeeded
                Log.d(TAG, "Update successful for all hours")
                _events.send(InfoEvent.ShowSuccess("Business hours updated"))
                fetchDealerInfo() // Refresh data
            }
        }
    }

    fun requestDealerTypeChange(newDealerType: String) {
        viewModelScope.launch {
            Log.d(TAG, "--- requestDealerTypeChange: $newDealerType ---")

            val currentState = _uiState.value
            if (currentState !is InfoUiState.Success) return@launch
            val dealerId = currentDealerId
            if (dealerId == null) {
                _events.send(InfoEvent.ShowError("User session error."))
                return@launch
            }

            _uiState.value = currentState.copy(isSaving = true)

            // Get user info from SessionManager
            // TODO: You need to implement getFirstName(), getLastName(), getEmail() in SessionManager
            val user = UserPayload(
                first_name = sessionManager.getFirstName() ?: "Saichin", // Hardcoded fallback
                last_name = sessionManager.getLastName() ?: "Singh",   // Hardcoded fallback
                email = sessionManager.getEmail() ?: "sachinsingh@slt.work" // Hardcoded fallback
            )

            // Build the complex request from the current state, matching your log
            val request = ModifyDealerRequest(
                dealer_id = dealerId.toString(),
                dealer_key = "dealer_type",
                old_value = currentState.dealerInfo.dealerType ?: "Independent",
                new_value = newDealerType,
                dealer_name = currentState.dealerInfo.name ?: "",
                city_name = currentState.dealerInfo.city ?: "",
                state_name = currentState.dealerInfo.state ?: "",
                zip_code = currentState.dealerInfo.zipCode ?: "",
                user = user
            )

            Log.d(TAG, "Submitting dealer change request: $request")

            dealerRepository.requestDealerUpdate(request)
                .onSuccess {
                    Log.d(TAG, "Update request successful")
                    _events.send(InfoEvent.ShowSuccess("Dealer type change requested!"))
                    fetchDealerInfo() // Refresh data
                }
                .onFailure { error ->
                    Log.e(TAG, "Update request failed", error)
                    _uiState.value = currentState.copy(isSaving = false)
                }
        }
    }


    // --- Makes Logic ---

    fun fetchMakesData() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val dealerId = currentDealerId ?: return@launch

            // Avoid reloading if already loaded? No, let's refresh.
            // But we need to keep current state if it's Success
            if (currentState is InfoUiState.Success) {
                // You might want to show a small loading indicator for makes specifically,
                // but for now we'll just update silently or use isSaving if appropriate.
            }

            // 1. Fetch All Makes
            val allMakesResult = dealerRepository.getAllMakes()
            val allMakes = allMakesResult.getOrDefault(emptyList())

            // 2. Fetch Domain ID & Selected Makes
            // We need domainId first.
            val domainsResult = dealerRepository.getDomains(1, 1, dealerId)
            val domainId = domainsResult.getOrNull()?.list?.firstOrNull()?.id

            val selectedMakes = if (domainId != null) {
                // Fetch settings for "inventory", "default"
                dealerRepository.getDomainSettingMakes(domainId, "inventory", "default")
                    .getOrDefault(emptyList())
            } else {
                emptyList()
            }

            // Update State
            if (currentState is InfoUiState.Success) {
                _uiState.value = currentState.copy(
                    allMakes = allMakes,
                    selectedMakes = selectedMakes,
                    domainId = domainId
                )
            }
        }
    }

    fun saveMakes(selectedMakeIds: List<Int>) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState !is InfoUiState.Success) return@launch
            val domainId = currentState.domainId

            if (domainId == null) {
                _events.send(InfoEvent.ShowError("Domain ID not found. Cannot save makes."))
                return@launch
            }

            _uiState.value = currentState.copy(isSaving = true)
            Log.d(TAG, "Saving makes for domain $domainId: $selectedMakeIds")

            dealerRepository.saveDomainSettingMakes(domainId, "inventory", "default", selectedMakeIds)
                .onSuccess { updatedMakes ->
                    Log.d(TAG, "Makes updated successfully")
                    _events.send(InfoEvent.ShowSuccess("Makes updated"))
                    // Update UI with new selected makes
                    _uiState.value = currentState.copy(
                        isSaving = false,
                        selectedMakes = updatedMakes
                    )
                }
                .onFailure { error ->
                    Log.e(TAG, "Failed to update makes", error)
                    _uiState.value = currentState.copy(isSaving = false)
                    _events.send(InfoEvent.ShowError(error.message ?: "Failed to update makes"))
                }
        }
    }

    fun saveSocialProfiles(updatedProfiles: List<SocialProfileItem>) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState !is InfoUiState.Success) return@launch
            val dealerId = currentDealerId ?: return@launch

            _uiState.value = currentState.copy(isSaving = true)

            dealerRepository.saveSocialProfiles(dealerId, updatedProfiles)
                .onSuccess {
                    _events.send(InfoEvent.ShowSuccess("Social profiles updated"))
                    // Optimistically update UI or fetch again. Let's update UI directly.
                    _uiState.value = currentState.copy(
                        isSaving = false,
                        socialProfiles = updatedProfiles
                    )
                }
                .onFailure { error ->
                    Log.e(TAG, "Failed to save social profiles", error)
                    _uiState.value = currentState.copy(isSaving = false)
                    _events.send(InfoEvent.ShowError(error.message ?: "Update failed"))
                }
        }
    }
}