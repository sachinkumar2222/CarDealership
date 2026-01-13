package com.slt.cardealership.presentation.users

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.data.local.SessionManager
import com.slt.cardealership.domain.model.ChangePasswordRequest
import com.slt.cardealership.domain.model.Department
import com.slt.cardealership.domain.model.Designation
import com.slt.cardealership.domain.model.DetailedUserProfile
import com.slt.cardealership.domain.model.ManageUsers
import com.slt.cardealership.domain.repo.DealerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import java.io.ByteArrayOutputStream

@HiltViewModel
class UserViewModel @Inject constructor(
    private val dealerRepository: DealerRepository,
    private val sessionManager: SessionManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // --- FIX 1: State for the LIST screen ---
    private val _userListState = MutableStateFlow(UserScreenUiState())
    val userListState: StateFlow<UserScreenUiState> = _userListState.asStateFlow()

    // --- FIX 2: SEPARATE state for the EDIT screen ---
    private val _userDetailState = MutableStateFlow(EditUserUiState())
    val userDetailState: StateFlow<EditUserUiState> = _userDetailState.asStateFlow()

    private val _addUserUiState = MutableStateFlow(AddUserUiState())
    val addUserUiState: StateFlow<AddUserUiState> = _addUserUiState.asStateFlow()

    private val _changePasswordState = MutableStateFlow(ChangePasswordUiState())
    val changePasswordState: StateFlow<ChangePasswordUiState> = _changePasswordState.asStateFlow()

    // This is shared and correct
    private val _currentUserUsername = MutableStateFlow<String?>(null)
    val currentUserUsername: StateFlow<String?> = _currentUserUsername.asStateFlow()

    private val defaultRoleId = 9
    private val itemsPerPage = 20

    init {
        viewModelScope.launch {
            val loggedInUsername = sessionManager.getUsername()
            Log.d("UserViewModel", "Current Username from Session: $loggedInUsername")
            _currentUserUsername.value = loggedInUsername
        }
        loadUsers()
    }

    //=============================================
    // Functions for User List Screen
    //=============================================


    fun loadAddUserFormDependencies() {
        viewModelScope.launch {
            // Set loading state for the dropdowns
            _addUserUiState.value = _addUserUiState.value.copy(isLoading = true, error = null)

            // TODO: We will add getRoles and getDesignations here later

            dealerRepository.getDepartments()
                .onSuccess { departments ->
                    _addUserUiState.value = _addUserUiState.value.copy(
                        isLoading = false,
                        departments = departments
                    )
                }
                .onFailure { exception: Throwable ->
                    _addUserUiState.value = _addUserUiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load departments"
                    )
                }
        }
    }

    fun loadDesignations(departmentId: Int) {
        viewModelScope.launch {
            _addUserUiState.value = _addUserUiState.value.copy(isLoading = true, error = null)
            dealerRepository.getDesignations(departmentId)
                .onSuccess { designations ->
                    _addUserUiState.value = _addUserUiState.value.copy(
                        isLoading = false,
                        designations = designations
                    )
                }
                .onFailure { exception: Throwable ->
                    _addUserUiState.value = _addUserUiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load designations"
                    )
                }
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            // --- FIX 3: Update the correct state object ---
            _userListState.value = UserScreenUiState(isLoading = true)

            // Get dealerId from session
            val dealerIdInt = sessionManager.getDealerId()
            if (dealerIdInt == null) {
                _userListState.value = UserScreenUiState(
                    isLoading = false,
                    error = "Dealer ID not found. Please login again."
                )
                return@launch
            }
            val dealerId = dealerIdInt.toLong()

            dealerRepository.getUsers(
                page = 1,
                itemsPerPage = itemsPerPage,
                dealerId = dealerId,
                roleId = defaultRoleId
            ).onSuccess { response ->
                _userListState.value = UserScreenUiState(
                    isLoading = false,
                    users = response.list,
                    currentPage = 1,
                    totalUsers = response.pagination.total,
                    canLoadMore = response.list.size < response.pagination.total
                )
            }.onFailure { exception: Throwable ->
                _userListState.value = UserScreenUiState(
                    isLoading = false,
                    error = exception.message ?: "An unknown error occurred"
                )
            }
        }
    }

    fun loadMoreUsers() {
        // --- FIX 4: Get state from the correct object ---
        val currentState = _userListState.value
        if (currentState.isLoadingMore || !currentState.canLoadMore) return

        viewModelScope.launch {
            _userListState.value = currentState.copy(isLoadingMore = true)
            val nextPage = currentState.currentPage + 1

            // Get dealerId from session
            val dealerIdInt = sessionManager.getDealerId()
            if (dealerIdInt == null) {
                _userListState.value = currentState.copy(
                    isLoadingMore = false,
                    error = "Dealer ID not found."
                )
                return@launch
            }
            val dealerId = dealerIdInt.toLong()

            dealerRepository.getUsers(
                page = nextPage,
                itemsPerPage = itemsPerPage,
                dealerId = dealerId,
                roleId = defaultRoleId
            ).onSuccess { response ->
                val allUsers = currentState.users + response.list
                _userListState.value = currentState.copy(
                    isLoadingMore = false,
                    users = allUsers,
                    currentPage = nextPage,
                    totalUsers = response.pagination.total,
                    canLoadMore = allUsers.size < response.pagination.total
                )
            }.onFailure { exception: Throwable ->
                _userListState.value = currentState.copy(
                    isLoadingMore = false,
                    error = exception.message ?: "Failed to load more users"
                )
            }
        }
    }

    /**
     * Resets any error message on the LIST screen.
     */
    fun listErrorShown() {
        _userListState.value = _userListState.value.copy(error = null)
    }

    //=============================================
    // Functions for Edit User Screen
    //=============================================

    /**
     * Fetches details for a single user.
     * Call this from your EditUserScreen.
     */
    fun fetchUserDetails(userId: Long) { // <-- FIX 5: Pass userId as a parameter
        viewModelScope.launch {
            // --- FIX 6: Update the DETAIL state object ---
            _userDetailState.value = EditUserUiState(isLoading = true)

            dealerRepository.getUserDetails(userId) // <-- Use the passed-in userId
                .onSuccess { userProfile ->
                    _userDetailState.value = EditUserUiState(isLoading = false, user = userProfile)
                }
                .onFailure { exception: Throwable ->
                    _userDetailState.value = EditUserUiState(
                        isLoading = false,
                        detailError = exception.message ?: "Failed to load user"
                    )
                }
        }
    }

    fun saveUserChanges(
        firstName: String,
        lastName: String,
        phone: String,
        status: String,
        imageUri: Uri?
    ) {
        val currentState = _userDetailState.value
        val originalUser = currentState.user ?: return // Can't save if no user is loaded

        if (currentState.isSaving) return // Prevent multiple clicks

        viewModelScope.launch {
            _userDetailState.value = currentState.copy(isSaving = true, detailError = null)

            try {
                // Build the multipart map
                val parts = buildMultipartMap(originalUser, firstName, lastName, phone, status,imageUri)

                val updatedUser = dealerRepository.updateUser(originalUser.id, parts).getOrThrow()

                val localDraft = originalUser.copy(
                    firstName = firstName,
                    lastName = lastName,
                    phone = phone
                )
                val finalUpdatedUser = localDraft.copy(
                    imageUrl = updatedUser.imageUrl,
                    updatedOn = System.currentTimeMillis() / 1000
                )
                _userDetailState.value = currentState.copy(
                    isSaving = false,
                    isSaveSuccess = true, // Signal success
                    user = finalUpdatedUser // Update state with fresh data
                )

                // Refresh the main list so "UserScreen" is up to date when we go back
                loadUsers()
                // TODO: Add a "Save Success" event for a Snackbar

            } catch (e: Exception) {
                _userDetailState.value = currentState.copy(
                    isSaving = false,
                    detailError = e.message ?: "An unknown error occurred"
                )
            }
        }
    }

    fun addUser(
        firstName: String,
        lastName: String,
        username: String,
        password: String,
        phone: String?,
        imageUri: Uri?,
        departmentId: Int?,
        designationId: Int?
    ) {
        viewModelScope.launch {
            _addUserUiState.value = AddUserUiState(isLoading = true)

            if (firstName.isBlank() || lastName.isBlank() || username.isBlank() || password.isBlank()) {
                _addUserUiState.value = AddUserUiState(error = "Please fill in all required fields.")
                return@launch
            }
            if (designationId == null) {
                _addUserUiState.value = AddUserUiState(error = "Please select a designation.")
                return@launch
            }

            dealerRepository.addUser(
                firstName = firstName,
                lastName = lastName,
                username = username,
                password = password,
                phone = phone,
                imageUri = imageUri,
                departmentId = departmentId,
                designationId = designationId!! // Force unwrap since we checked for null
            ).onSuccess {
                _addUserUiState.value = AddUserUiState(isSuccess = true)
                loadUsers() // Refresh the user list after adding a new one
            }.onFailure { exception: Throwable ->
                _addUserUiState.value = AddUserUiState(
                    error = exception.message ?: "Failed to add user"
                )
            }
        }
    }

    fun clearAddUserState() {
        _addUserUiState.value = AddUserUiState()
    }

    /**
     * Helper to convert a String to RequestBody for multipart forms.
     */
    private fun String.toTextRequestBody(): RequestBody {
        return this.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    /**
     * Helper to build the complete multipart map, preserving all required fields.
     */
    private fun buildMultipartMap(
        originalUser: DetailedUserProfile,
        firstName: String,
        lastName: String,
        phone: String,
        status: String,
        imageUri: Uri?
    ): Map<String, RequestBody> {

        val map = mutableMapOf<String, RequestBody>()

        // 1. Add fields from the UI
        map["first_name"] = firstName.toTextRequestBody()
        map["last_name"] = lastName.toTextRequestBody()
        map["phone"] = phone.toTextRequestBody()

        // 2. Convert "Active" -> "true", "Inactive" -> "false"
        // map["is_active"] = status.lowercase().toTextRequestBody()

        // 3. Add all other fields from the original user object to match payload
        //    (Your API requires all fields, even ones you didn't change)
        map["username"] = originalUser.username.toTextRequestBody()
        map["role_id"] = originalUser.roleId.toString().toTextRequestBody()
        map["organization_id"] = originalUser.organizationId.toString().toTextRequestBody()
        map["department_id"] = originalUser.departmentId.toString().toTextRequestBody()
        map["designation_id"] = originalUser.designationId.toString().toTextRequestBody()
        map["dealer_id"] = originalUser.dealerId.toString().toTextRequestBody()

        // 4. Handle nullable fields and send them
        originalUser.imageUrl?.let { map["image_url"] = it.toTextRequestBody() }

        // 5. Send 'created' fields (as in your payload)
        // Your payload shows these, so we must send them
        originalUser.createdBy?.let { map["created_by"] = it.toString().toTextRequestBody() }
        originalUser.createdOn?.let { map["created_on"] = it.toString().toTextRequestBody() }

        // 6. Send 'updated' fields
        // Use the original 'updated_by' ID
        originalUser.updatedBy?.let { map["updated_by"] = it.toString().toTextRequestBody() }
        // Send a NEW 'updated_on' timestamp
        val updatedOnTimestamp = (System.currentTimeMillis() / 1000).toString()
        map["updated_on"] = updatedOnTimestamp.toTextRequestBody()

        // --- 6. ADD THE IMAGE COMPRESSION LOGIC ---
        if (imageUri != null) {
            // A new image was selected, compress and add it
            Log.d("UserViewModel", "New image selected. Compressing...")

            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, imageUri))
            } else {
                @Suppress("DEPRECATION")
                android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
            }

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream) // 80% quality
            val compressedFileBytes = outputStream.toByteArray()

            Log.d("UserViewModel", "Compressed image size: ${compressedFileBytes.size} bytes")

            val requestFile = compressedFileBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
            map["image_url\"; filename=\"user_image.jpg"] = requestFile

        } else {
            // No new image, send back the original URL
            originalUser.imageUrl?.let { map["image_url"] = it.toTextRequestBody() }
        }

        return map
    }

    /**
     * Clears the selected user's data when navigating away.
     */
    fun clearUserDetails() {
        _userDetailState.value = EditUserUiState()
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
                dealerRepository.changeUserPassword(userId, request).getOrThrow()
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

// --- STATE for User List Screen ---
data class UserScreenUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val users: List<ManageUsers> = emptyList(),
    val error: String? = null,
    val currentPage: Int = 1,
    val totalUsers: Int = 0,
    val canLoadMore: Boolean = true
)

// --- STATE for Edit User Screen ---
data class EditUserUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaveSuccess: Boolean = false, // <-- ADDED THIS
    val user: DetailedUserProfile? = null,
    val detailError: String? = null
)

data class ChangePasswordUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

data class AddUserUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val departments: List<Department> = emptyList(), // <-- ADDED // <-- For later
    val designations: List<Designation> = emptyList()
)