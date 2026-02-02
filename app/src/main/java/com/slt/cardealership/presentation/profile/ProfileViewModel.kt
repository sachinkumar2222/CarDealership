package com.slt.cardealership.presentation.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.SavedStateHandle
import com.slt.cardealership.data.remote.network.ApiService
import com.slt.cardealership.domain.model.DetailedUserProfile
import com.slt.cardealership.domain.repo.DealerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import com.slt.cardealership.domain.model.ChangePasswordRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: DealerRepository,
    private val apiService: ApiService,
    @ApplicationContext private val context: Context,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val PROFILE_KEY = "editableProfile"
        private const val USER_ID_KEY = "currentUserId"
        private const val TAG = "ProfileViewModel_DEBUG"
    }

    // UI state for both profile view and edit screen
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val _changePasswordState = MutableStateFlow(ChangePasswordUiState())
    val changePasswordState: StateFlow<ChangePasswordUiState> = _changePasswordState.asStateFlow()

    // This flow will now automatically save and restore the profile
    private val _editableProfile = savedStateHandle.getStateFlow<DetailedUserProfile?>(PROFILE_KEY, null)
    val editableProfile: StateFlow<DetailedUserProfile?> = _editableProfile

    private val _selectedImageUri = MutableStateFlow<Uri?>(null) // This is temporary, fine to lose

    // RESTORE YOUR USER ID
    private var _currentUserId: Long? = savedStateHandle.get<Long>(USER_ID_KEY)


    init {
        Log.d(TAG, "-------------------------")
        Log.d(TAG, "ViewModel INIT block running...")

        // Let's check what SavedStateHandle is giving us BEFORE the 'if' check
        val restoredProfile: DetailedUserProfile? = savedStateHandle.get<DetailedUserProfile>(PROFILE_KEY)
        val restoredUserId: Long? = savedStateHandle.get<Long>(USER_ID_KEY)

        Log.d(TAG, "Restored Profile IS NULL: ${restoredProfile == null}")
        Log.d(TAG, "Restored UserID IS NULL: ${restoredUserId == null}")
        Log.d(TAG, "Restored UserID Value: $restoredUserId")


        if (restoredProfile != null && restoredUserId != null) {
            Log.d(TAG, "RESULT: Restoring data from SavedStateHandle.")
            _uiState.value = ProfileUiState.Success(restoredProfile)
        } else {
            Log.d(TAG, "RESULT: Data is null or missing. Fetching from network...")
            fetchFullUserProfile()
        }
        Log.d(TAG, "-------------------------")
    }

    fun fetchFullUserProfile() {
        Log.d(TAG, "fetchFullUserProfile() CALLED.")
        _uiState.value = ProfileUiState.Loading
        viewModelScope.launch {
            try {
                // First, get the user's ID
                val authorizationResponse = apiService.getUserAuthorization()
                Log.d("DealerRepo", "Authorization Response: ${authorizationResponse.dealerId}")
                _currentUserId = authorizationResponse.userId
                savedStateHandle[USER_ID_KEY] = _currentUserId // SAVE THE ID

                // Then, fetch the detailed profile using the ID
                val userProfile = apiService.getDetailedUserProfile(_currentUserId!!) // Using the specific ID

                savedStateHandle[PROFILE_KEY] = userProfile // SAVE THE PROFILE
                _uiState.value = ProfileUiState.Success(userProfile)
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.localizedMessage ?: "Failed to load profile")
            }
        }
    }

    fun reload() {
        fetchFullUserProfile()
    }

    private fun String.toTextRequestBody(): RequestBody {
        return this.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    fun onImageSelected(uri: Uri?) {
        if (uri == null) return
        _selectedImageUri.value = uri // Save the Uri

        // Update the UI preview immediately *on the saved state*
        // This will save the local URI string to be restored
        savedStateHandle[PROFILE_KEY] = _editableProfile.value?.copy(imageUrl = uri.toString())

        // Automatically call onSaveProfile to upload the new image
        onSaveProfile()
    }

    fun onSaveProfile() {
        val userId = _currentUserId
        val currentDraft = _editableProfile.value // Get from the flow

        if (userId == null || currentDraft == null) {
            _uiState.value = ProfileUiState.Error("User ID or profile data is missing for update.")
            return
        }

        _uiState.value = ProfileUiState.Saving
        viewModelScope.launch {
            try {
                // --- Build the multipart map ---
                val parts = mutableMapOf<String, RequestBody>()

                parts["first_name"] = currentDraft.firstName.toTextRequestBody()
                parts["last_name"] = currentDraft.lastName.toTextRequestBody()
                parts["phone"] = currentDraft.phone.orEmpty().toTextRequestBody()
                parts["address"] = currentDraft.address.orEmpty().toTextRequestBody()
                parts["dob"] = (currentDraft.dob?.toTimestampInSeconds() ?: 0L).toString().toTextRequestBody()
                parts["doj"] = (currentDraft.doj?.toTimestampInSeconds() ?: 0L).toString().toTextRequestBody()
                parts["username"] = currentDraft.username.toTextRequestBody()
                parts["role_id"] = currentDraft.roleId.toString().toTextRequestBody()
                parts["created_by"] = (currentDraft.createdBy ?: 0L).toString().toTextRequestBody()
                parts["created_on"] = (currentDraft.createdOn ?: 0L).toString().toTextRequestBody()
                parts["updated_by"] = userId.toString().toTextRequestBody()
                parts["updated_on"] = (System.currentTimeMillis() / 1000).toString().toTextRequestBody()
                parts["organization_id"] = currentDraft.organizationId.toString().toTextRequestBody()
                parts["department_id"] = (currentDraft.departmentId ?: 0).toString().toTextRequestBody()
                parts["designation_id"] = (currentDraft.designationId ?: 0).toString().toTextRequestBody()
                parts["dealer_id"] = (currentDraft.dealerId ?: 0L).toString().toTextRequestBody()
                parts["dealer_name"] = currentDraft.dealerName.orEmpty().toTextRequestBody()
                parts["gender"] = currentDraft.gender.orEmpty().toTextRequestBody()
                parts["language"] = currentDraft.language.orEmpty().toTextRequestBody()
                val isActiveValue = currentDraft.isActive.toString() // This will be "true"
                Log.d(TAG, "onSaveProfile: Sending 'is_active' with value: $isActiveValue")
                // parts["is_active"] = isActiveValue.toTextRequestBody()

                // --- 6. UPDATED IMAGE COMPRESSION CODE ---
                val imageUri = _selectedImageUri.value
                if (imageUri != null) {
                    Log.d(TAG, "onSaveProfile: New image found. Compressing...")

                    // 1. Get Bitmap from Uri
                    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, imageUri))
                    } else {
                        @Suppress("DEPRECATION")
                        android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
                    }

                    // 2. Compress the Bitmap
                    val outputStream = ByteArrayOutputStream()
                    // 80% JPEG quality. Adjust '80' lower if 1.59MB was still too large.
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                    val compressedFileBytes = outputStream.toByteArray()

                    Log.d(TAG, "onSaveProfile: Compressed image size: ${compressedFileBytes.size} bytes")

                    // 3. Create RequestBody from compressed bytes
                    val requestFile = compressedFileBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())

                    parts["image_url\"; filename=\"profile.jpg"] = requestFile

                    // Clear the selected URI so we don't re-upload it by mistake
                    _selectedImageUri.value = null

                } else {
                    Log.d(TAG, "onSaveProfile: No new image selected. Sending original URL.")
                    // No new image, send back the original URL
                    parts["image_url"] = currentDraft.imageUrl.orEmpty().toTextRequestBody()
                }

                repository.updateUser(userId, parts)
                    .onSuccess { partialUpdateResponse ->

                        // --- 6. FIXING THE "VALUES NOT UPDATING" BUG ---
                        val updatedProfile = currentDraft.copy(
                            imageUrl = partialUpdateResponse.imageUrl
                        )
                        savedStateHandle[PROFILE_KEY] = updatedProfile // <-- SAVE IT

                        _uiState.value = ProfileUiState.SaveSuccess("Profile updated successfully!")
                        _uiState.value = ProfileUiState.Success(updatedProfile)
                        _eventFlow.emit(UiEvent.ShowToast("Profile updated successfully!"))
                        _eventFlow.emit(UiEvent.NavigateBack)// Update UI
                        _selectedImageUri.value = null // Clear the selected image
                    }
                    .onFailure { error: Throwable ->
                        _uiState.value = ProfileUiState.SaveError(error.localizedMessage ?: "Failed to update profile.")
                        _eventFlow.emit(UiEvent.ShowToast(error.localizedMessage ?: "Failed to update profile."))
                    }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.SaveError(e.localizedMessage ?: "Failed to update profile.")
            }
        }
    }

    fun updateProfileField(update: (DetailedUserProfile) -> DetailedUserProfile) {
        savedStateHandle[PROFILE_KEY] = _editableProfile.value?.let(update)
    }

    private fun String.toTimestampInSeconds(): Long {
        // This function converts "dd-MM-yyyy" to a Long timestamp IN SECONDS
        return try {
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC") // Or your server's timezone

            // 1. Get time in milliseconds
            val milliseconds = sdf.parse(this)?.time ?: 0L

            // 2. Convert to seconds
            milliseconds / 1000L
        } catch (e: Exception) {
            0L // Return 0 or handle error
        }
    }

    fun changePassword(userId: Long, pass: String, confirmPass: String) {
        viewModelScope.launch {
            // 1. Validation
            if (pass.isBlank() || confirmPass.isBlank()) {
                _changePasswordState.value = ChangePasswordUiState(error = "Fields cannot be empty")
                return@launch
            }
            if (pass != confirmPass) {
                _changePasswordState.value = ChangePasswordUiState(error = "Passwords do not match")
                return@launch
            }

            // 2. Set loading
            _changePasswordState.value = ChangePasswordUiState(isLoading = true)

            val request = ChangePasswordRequest(password = pass)

            // 3. Make API Call
            try {
                // Using dealerRepository from constructor
                repository.changeMyPassword(userId, request).getOrThrow()
                _changePasswordState.value = ChangePasswordUiState(isSuccess = true)
            } catch (exception: Throwable) {
                _changePasswordState.value = ChangePasswordUiState(
                    error = exception.message ?: "Failed to change password"
                )
            }
        }
    }

    fun clearChangePasswordState() {
        _changePasswordState.value = ChangePasswordUiState()
    }
}

// ... (Your ProfileUiState sealed class) ...
sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val userProfile: DetailedUserProfile) : ProfileUiState()
    object Saving : ProfileUiState()
    data class SaveSuccess(val message: String) : ProfileUiState()
    data class SaveError(val message: String) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

data class ChangePasswordUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

sealed interface UiEvent {
    data class ShowToast(val message: String) : UiEvent
    object NavigateBack : UiEvent
}