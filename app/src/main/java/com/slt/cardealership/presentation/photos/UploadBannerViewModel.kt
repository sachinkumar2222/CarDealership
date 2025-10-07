//package com.slt.cardealership.presentation.photos
//
//import android.net.Uri
//import android.util.Log
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.setValue
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.slt.cardealership.data.local.SessionManager
//import com.slt.cardealership.data.repo.BannerRepository
//import com.slt.cardealership.domain.model.Banner
//import com.slt.cardealership.utils.DateFormatter
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.MutableSharedFlow
//import kotlinx.coroutines.flow.asSharedFlow
//import kotlinx.coroutines.launch
//import java.io.File
//import java.text.SimpleDateFormat
//import java.util.Calendar
//import java.util.Locale
//import javax.inject.Inject
//
//private const val TAG = "UploadBannerDebug"
//
//@HiltViewModel
//class UploadBannerViewModel @Inject constructor(
//    private val bannerRepository: BannerRepository,
//    private val sessionManager: SessionManager // To get dealer slug
//) : ViewModel() {
//
//    var showDialog by mutableStateOf(false)
//        private set
//    var isEditing by mutableStateOf(false)
//        private set
//    var currentBannerId by mutableStateOf<String?>(null)
//        private set
//
//    var title by mutableStateOf("")
//        private set
//    var url by mutableStateOf("")
//        private set
//    var startDate by mutableStateOf("")
//        private set
//    var addEndDate by mutableStateOf(false)
//        private set
//    var endDate by mutableStateOf("")
//        private set
//    var selectedImageUri by mutableStateOf<Uri?>(null)
//        private set
//    var existingImageUrl by mutableStateOf<String?>(null) // To display existing image during edit
//
//    var isLoading by mutableStateOf(false)
//        private set
//    var errorMessage by mutableStateOf<String?>(null)
//        private set
//
//    private val _bannerOperationEvent = MutableSharedFlow<BannerOperationEvent>()
//    val bannerOperationEvent = _bannerOperationEvent.asSharedFlow()
//
//    fun showAddDialog() {
//        resetForm()
//        isEditing = false
//        showDialog = true
//    }
//
//    fun showEditDialog(banner: Banner) {
//        resetForm()
//        isEditing = true
//        currentBannerId = banner.id // Assuming banner.id is available
//        title = banner.title ?: ""
//        url = banner.url ?: ""
//        startDate = DateFormatter.formatDateForDisplay(banner.createdOn) // Using createdOn as start date for now
//        // If your API has actual start/end dates for banners, use those here
//        // For example: startDate = DateFormatter.formatDateForDisplay(banner.startDate)
//
//        // If you have an actual endDate from API, load it
//        // endDate = DateFormatter.formatDateForDisplay(banner.endDate)
//        // addEndDate = !endDate.isNullOrBlank()
//
//        existingImageUrl = banner.imageUrl
//        selectedImageUri = null // Clear selected image when opening edit, user must re-select if changing
//
//        showDialog = true
//    }
//
//    fun dismissDialog() {
//        showDialog = false
//        errorMessage = null
//        resetForm()
//    }
//
//    private fun resetForm() {
//        title = ""
//        url = ""
//        startDate = ""
//        addEndDate = false
//        endDate = ""
//        selectedImageUri = null
//        existingImageUrl = null
//        currentBannerId = null
//        isLoading = false
//        errorMessage = null
//    }
//
//    fun onTitleChange(value: String) {
//        title = value
//    }
//
//    fun onUrlChange(value: String) {
//        url = value
//    }
//
//    fun onStartDateChange(year: Int, month: Int, dayOfMonth: Int) {
//        val calendar = Calendar.getInstance().apply {
//            set(year, month, dayOfMonth)
//        }
//        startDate = SimpleDateFormat(DateFormatter.DISPLAY_DATE_FORMAT, Locale.getDefault()).format(calendar.time)
//    }
//
//    fun onAddEndDateToggle(checked: Boolean) {
//        addEndDate = checked
//        if (!checked) endDate = "" // Clear end date if toggled off
//    }
//
//    fun onEndDateChange(year: Int, month: Int, dayOfMonth: Int) {
//        val calendar = Calendar.getInstance().apply {
//            set(year, month, dayOfMonth)
//        }
//        endDate = SimpleDateFormat(DateFormatter.DISPLAY_DATE_FORMAT, Locale.getDefault()).format(calendar.time)
//    }
//
//    fun onImageSelected(uri: Uri?) {
//        selectedImageUri = uri
//        existingImageUrl = null // New image selected, clear existing one
//    }
//
//    fun onImageRemoved() {
//        selectedImageUri = null
//        existingImageUrl = null
//    }
//
//    fun uploadOrUpdateBanner(imageFile: File?) {
//        Log.d(TAG, "--- uploadOrUpdateBanner called ---")
//        errorMessage = null
//        if (title.isBlank() || url.isBlank() || startDate.isBlank()) {
//            errorMessage = "Please fill in all required fields (Title, URL, Start Date)."
//            Log.d(TAG, "Validation FAILED: Required fields are blank. Title='${title}', URL='${url}', StartDate='${startDate}'")
//            return
//        }
//        if (!isEditing && selectedImageUri == null && imageFile == null) {
//            errorMessage = "Please select an image for the new banner."
//            Log.d(TAG, "Validation FAILED: No image file provided for a new banner.")
//            return
//        }
//        if (addEndDate && endDate.isBlank()) {
//            errorMessage = "Please select an end date or uncheck 'Add End Date'."
//            Log.d(TAG, "Validation FAILED: End date is required but not provided.")
//            return
//        }
//
//        Log.d(TAG, "Validation Passed. Setting isLoading = true and launching coroutine.")
//        isLoading = true
//        viewModelScope.launch {
//            val dealerSlug = sessionManager.getDealerSlug() ?: run {
//                errorMessage = "Dealer slug not found. Cannot proceed."
//                Log.d(TAG, "Coroutine FAILED: dealerSlug is null.")
//                isLoading = false
//                return@launch
//            }
//            Log.d(TAG, "Successfully retrieved dealerSlug: $dealerSlug")
//
//            val formattedStartDate = DateFormatter.formatDisplayDateToApiDate(startDate)
//            val formattedEndDate = if (addEndDate) DateFormatter.formatDisplayDateToApiDate(endDate) else null
//
//            if (formattedStartDate == null) {
//                errorMessage = "Invalid Start Date format."
//                isLoading = false
//                Log.d(TAG, "Coroutine FAILED: startDate could not be formatted.")
//                return@launch
//            }
//
//            Log.d(TAG, "Dates formatted successfully.")
//
//            val result = if (isEditing && currentBannerId != null) {
//                Log.d(TAG, "Attempting to UPDATE banner with ID: $currentBannerId")
//                // Update existing banner
//                bannerRepository.updateBanner(
//                    dealerSlug = dealerSlug,
//                    bannerId = currentBannerId!!,
//                    title = title,
//                    url = url,
//                    startDate = formattedStartDate,
//                    endDate = formattedEndDate,
//                    imageFile = imageFile // Pass the actual File object
//                )
//            } else {
//                Log.d(TAG, "Attempting to UPLOAD new banner.")
//                // Upload new banner
//                if (imageFile == null) {
//                    errorMessage = "Image file is missing for upload."
//                    isLoading = false
//                    Log.d(TAG, "Coroutine FAILED: imageFile is null right before repository call.")
//                    return@launch
//                }
//                bannerRepository.uploadBanner(
//                    dealerSlug = dealerSlug,
//                    title = title,
//                    url = url,
//                    startDate = formattedStartDate,
//                    endDate = formattedEndDate,
//                    imageFile = imageFile // Pass the actual File object
//                )
//            }
//
//            Log.d(TAG, "Repository call finished. Result success: ${result.isSuccess}")
//            result.onSuccess {
//                _bannerOperationEvent.emit(BannerOperationEvent.Success(if (isEditing) "Banner updated successfully!" else "Banner uploaded successfully!"))
//                dismissDialog()
//            }.onFailure { e ->
//                errorMessage = e.message ?: "An unknown error occurred."
//                Log.e(TAG, "Repository call FAILED", e)
//            }
//            isLoading = false
//        }
//    }
//
//    fun deleteBanner(bannerId: String) {
//        viewModelScope.launch {
//            isLoading = true
//            val dealerSlug = sessionManager.getDealerSlug() ?: run {
//                errorMessage = "Dealer slug not found. Cannot proceed."
//                isLoading = false
//                return@launch
//            }
//
//            bannerRepository.deleteBanner(dealerSlug, bannerId)
//                .onSuccess {
//                    _bannerOperationEvent.emit(BannerOperationEvent.Success("Banner deleted successfully!"))
//                }
//                .onFailure { e ->
//                    errorMessage = e.message ?: "Failed to delete banner."
//                }
//            isLoading = false
//        }
//    }
//}
//
//sealed class BannerOperationEvent {
//    data class Success(val message: String) : BannerOperationEvent()
//    data class Error(val message: String) : BannerOperationEvent()
//}