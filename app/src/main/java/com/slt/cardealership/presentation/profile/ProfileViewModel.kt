package com.slt.cardealership.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.data.remote.network.ApiService
import com.slt.cardealership.domain.model.DetailedUserProfile
import com.slt.cardealership.domain.model.UserProfileUpdateRequest // Import the request model
import com.slt.cardealership.domain.repo.DealerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update // Import update for MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: DealerRepository,
    private val apiService: ApiService
) : ViewModel() {

    // UI state for both profile view and edit screen (Loading, Success, Error, Saving, SaveSuccess, SaveError)
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow() // Expose as read-only

    // Holds the currently displayed/editable profile data.
    // When editing, this acts as the "draft" that the UI modifies.
    private val _editableProfile = MutableStateFlow<DetailedUserProfile?>(null)
    val editableProfile: StateFlow<DetailedUserProfile?> = _editableProfile.asStateFlow() // Expose as read-only

    private var _currentUserId: Long? = null // To store the user ID fetched from authorization

    init {
        fetchFullUserProfile()
    }

    fun fetchFullUserProfile() {
        _uiState.value = ProfileUiState.Loading
        viewModelScope.launch {
            try {
                // First, get the user's ID
                val authorizationResponse = apiService.getUserAuthorization()
                _currentUserId = authorizationResponse.userId

                // Then, fetch the detailed profile using the ID
                val userProfile = apiService.getDetailedUserProfile(_currentUserId!!) // Using the specific ID

                _editableProfile.value = userProfile // Set the initial editable draft
                _uiState.value = ProfileUiState.Success(userProfile)
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.localizedMessage ?: "Failed to load profile")
            }
        }
    }

    fun onSaveProfile() {
        // Ensure we have a user ID and editable profile data before saving
        val userId = _currentUserId
        val currentDraft = _editableProfile.value

        if (userId == null || currentDraft == null) {
            _uiState.value = ProfileUiState.Error("User ID or profile data is missing for update.")
            return
        }

        _uiState.value = ProfileUiState.Saving // Indicate that saving is in progress
        viewModelScope.launch {
            try {
                // Construct the update request from the current editable draft
                val request = UserProfileUpdateRequest(
                    firstName = currentDraft.firstName,
                    lastName = currentDraft.lastName,
                    username = currentDraft.username,
                    roleId = currentDraft.roleId,
                    createdBy = currentDraft.createdBy ?: 0L, // Default to 0 if null
                    createdOn = currentDraft.createdOn ?: (System.currentTimeMillis() / 1000), // Default if null
                    updatedBy = userId, // The user performing the update
                    updatedOn = System.currentTimeMillis() / 1000, // Current timestamp
                    organizationId = currentDraft.organizationId,
                    departmentId = currentDraft.departmentId,
                    designationId = currentDraft.designationId,
                    imageUrl = currentDraft.imageUrl.orEmpty(),
                    dealerId = currentDraft.dealerId,
                    dealerName = currentDraft.dealerName.orEmpty(),
                    isActive = currentDraft.isActive,
                    gender = currentDraft.gender,
                    language = currentDraft.language,
                    phone = currentDraft.phone,
                    address = currentDraft.address,
                    dob = currentDraft.dob,
                    doj = currentDraft.doj
                )

                repository.updateUserProfile(userId, request)
                    .onSuccess { partialUpdateResponse -> // Renamed for clarity

                        // --- THIS IS THE FIX ---
                        // Don't replace the whole profile.
                        // Instead, update the current draft with the *few* fields the server returned.
                        _editableProfile.update { currentProfile ->
                            currentProfile?.copy(
                                firstName = partialUpdateResponse.firstName,
                                lastName = partialUpdateResponse.lastName,
                                imageUrl = partialUpdateResponse.imageUrl
                            )
                        }

                        // Now set the success states
                        _uiState.value = ProfileUiState.SaveSuccess("Profile updated successfully!")

                        // And update the main Success state with our *newly merged* full draft
                        _editableProfile.value?.let {
                            _uiState.value = ProfileUiState.Success(it)
                        }
                    }
                    .onFailure { error ->
                        _uiState.value = ProfileUiState.SaveError(error.localizedMessage ?: "Failed to update profile.")
                    }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.SaveError(e.localizedMessage ?: "Failed to update profile.")
            }
        }
    }

    /**
     * Generic function to update a specific field in the editable profile draft.
     * Use this from your UI (EditProfileScreen) to modify the profile data.
     * Example: `viewModel.updateProfileField { it.copy(firstName = "New Name") }`
     */
    fun updateProfileField(update: (DetailedUserProfile) -> DetailedUserProfile) {
        _editableProfile.update { currentProfile ->
            // Apply the update function if currentProfile is not null
            currentProfile?.let(update)
        }
    }
}

// Updated UI State to include saving states
sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val userProfile: DetailedUserProfile) : ProfileUiState()
    object Saving : ProfileUiState() // State for when an update is in progress
    data class SaveSuccess(val message: String) : ProfileUiState() // State for successful update
    data class SaveError(val message: String) : ProfileUiState() // State for failed update
    data class Error(val message: String) : ProfileUiState()
}