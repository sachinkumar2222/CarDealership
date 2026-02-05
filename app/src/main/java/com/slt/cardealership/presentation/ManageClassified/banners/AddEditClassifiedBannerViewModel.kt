package com.slt.cardealership.presentation.ManageClassified.banners

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.data.local.SessionManager
import com.slt.cardealership.domain.repo.DealerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar
import javax.inject.Inject

sealed interface AddEditBannerUiState {
    data object Loading : AddEditBannerUiState
    data class Content(
        val title: String = "",
        val url: String = "",
        val startDate: Long? = null,
        val endDate: Long? = null,
        val imageUrl: String? = null,

        val imageUri: Uri? = null,
        val createdBy: String? = null,
        val isEditMode: Boolean = false,
        val isLoading: Boolean = false,
        val error: String? = null,
        val isSuccess: Boolean = false
    ) : AddEditBannerUiState
    data class Error(val message: String) : AddEditBannerUiState
}

@HiltViewModel
class AddEditClassifiedBannerViewModel @Inject constructor(
    private val repository: DealerRepository,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private var siteId: String? = savedStateHandle["siteId"]
    private var bannerId: String? = savedStateHandle["bannerId"]

    private val _uiState = MutableStateFlow<AddEditBannerUiState>(AddEditBannerUiState.Loading)
    val uiState: StateFlow<AddEditBannerUiState> = _uiState.asStateFlow()

    private var dealerId: Long? = null
    private var domainId: Int? = null

    // Removed init block to allow manual initialization from BottomSheet

    fun initializeViewModel(siteId: String, bannerId: String?) {
        this.siteId = siteId
        this.bannerId = bannerId
        initializeScreen()
    }

    private fun initializeScreen() {
        val id = siteId?.toIntOrNull()
        if (id == null) {
            _uiState.update { AddEditBannerUiState.Error("Invalid Site ID") }
            return
        }

        // Reset state for fresh init if needed
        if (bannerId == null) {
            _uiState.update { AddEditBannerUiState.Loading }
        }

        viewModelScope.launch {
            val domainResult = repository.getDomainDetails(id)

            domainResult.onSuccess { domainItem ->
                domainId = id
                if (domainItem.dealerId != null) {
                    dealerId = domainItem.dealerId.toLong()
                } else {
                    dealerId = sessionManager.getDealerId()?.toLong()
                }

                if (bannerId != null) {
                    loadBanner(bannerId!!)
                } else {
                    _uiState.update { AddEditBannerUiState.Content() }
                }
            }.onFailure { exception ->
                _uiState.update { AddEditBannerUiState.Error("Failed to fetch domain details") }
            }
        }
    }

    private suspend fun loadBanner(id: String) {
        val currentDealerId = dealerId ?: return
        repository.getBannerDetails(currentDealerId, id)
            .onSuccess { banner ->
                _uiState.update {
                    AddEditBannerUiState.Content(
                        title = banner.title ?: "",
                        url = banner.url ?: "",
                        startDate = banner.startDate?.times(1000), // Convert seconds to millis for date picker
                        endDate = banner.endDate?.times(1000),     // Convert seconds to millis for date picker
                        imageUrl = banner.imageUrl,
                        createdBy = banner.createdBy,
                        isEditMode = true
                    )
                }
            }
            .onFailure {
                _uiState.update { AddEditBannerUiState.Error("Failed to load banner details") }
            }
    }

    fun onTitleChange(newTitle: String) {
        updateContent { it.copy(title = newTitle) }
    }

    fun onUrlChange(newUrl: String) {
        updateContent { it.copy(url = newUrl) }
    }

    fun onStartDateChange(date: Long?) {
        updateContent {
            // Reset end date if start date changes and end date is before new start date
            val newEndDate = if (it.endDate != null && date != null && it.endDate < date) null else it.endDate
            it.copy(startDate = date, endDate = newEndDate)
        }
    }

    fun onEndDateChange(date: Long?) {
        updateContent { it.copy(endDate = date) }
    }

    fun onImageSelected(uri: Uri) {
        updateContent { it.copy(imageUri = uri) }
    }

    fun saveBanner() {
        val currentState = _uiState.value as? AddEditBannerUiState.Content ?: return
        val currentDealerId = dealerId ?: return
        val currentDomainId = domainId ?: return

        if (currentState.title.isBlank()) {
            updateContent { it.copy(error = "Title is required") }
            return
        }

        if (currentState.startDate == null) {
            updateContent { it.copy(error = "Start Date is required") }
            return
        }

        if (!currentState.isEditMode && currentState.imageUri == null) {
            updateContent { it.copy(error = "Image is required for new banner") }
            return
        }

        viewModelScope.launch {
            updateContent { it.copy(isLoading = true, error = null) }

            // Prepare Image File if selected
            var imageFile: File? = null
            currentState.imageUri?.let { uri ->
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val file = File(context.cacheDir, "banner_image_${System.currentTimeMillis()}.jpg")
                    val outputStream = FileOutputStream(file)
                    inputStream?.copyTo(outputStream)
                    inputStream?.close()
                    outputStream.close()
                    imageFile = file
                } catch (e: Exception) {
                    updateContent { it.copy(isLoading = false, error = "Failed to process image") }
                    return@launch
                }
            }

            // Backend expects seconds, input is millis
            val startSeconds = currentState.startDate / 1000
            val endSeconds = currentState.endDate?.div(1000)

            val currentBannerId = bannerId
            val result = if (currentState.isEditMode && currentBannerId != null) {
                repository.updateBanner(
                    dealerId = currentDealerId,
                    bannerId = currentBannerId,
                    domainId = currentDomainId,
                    title = currentState.title,
                    url = currentState.url,
                    startDate = startSeconds,
                    endDate = endSeconds,
                    imageFile = imageFile,
                    imageUrl = currentState.imageUrl,
                    createdBy = currentState.createdBy
                )
            } else {
                if (imageFile == null) {
                    updateContent { it.copy(isLoading = false, error = "Image is required") }
                    return@launch
                }
                repository.addBanner(
                    dealerId = currentDealerId,
                    domainId = currentDomainId,
                    title = currentState.title,
                    url = currentState.url,
                    startDate = startSeconds,
                    endDate = endSeconds,
                    imageFile = imageFile!!
                )
            }

            result.onSuccess {
                updateContent { it.copy(isLoading = false, isSuccess = true) }
            }.onFailure { e ->
                updateContent { it.copy(isLoading = false, error = e.message ?: "Failed to save banner") }
            }
        }
    }

    private fun updateContent(update: (AddEditBannerUiState.Content) -> AddEditBannerUiState.Content) {
        if (_uiState.value is AddEditBannerUiState.Content) {
            _uiState.update { update(it as AddEditBannerUiState.Content) }
        }
    }
}
