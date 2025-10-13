
package com.slt.cardealership.presentation.photos

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.data.local.SessionManager
import com.slt.cardealership.domain.model.Banner
import com.slt.cardealership.domain.model.GalleryImage
import com.slt.cardealership.domain.repo.DealerRepository
import com.slt.cardealership.utils.uriToFile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

// --- REFACTORED: Consolidated state for managing the dialog and deletion ---
data class PhotosUiState(
    val selectedTab: Int = 1,
    val banners: List<Banner> = emptyList(),
    val galleryImages: List<GalleryImage> = emptyList(),
    val isBannersLoading: Boolean = false, // <-- ADD THIS
    val isGalleryLoading: Boolean = false, // <-- ADD THIS
    val error: String? = null,

    // State for the "Manage Banner" Dialog (Add & Edit)
    val isManageBannerDialogVisible: Boolean = false,
    val isEditingBanner: Boolean = false,
    val bannerIdToEdit: String? = null,
    val bannerTitle: String = "",
    val bannerUrl: String = "",
    val bannerImageUri: Uri? = null,       // For a newly selected image
    val bannerExistingImageUrl: String? = null, // For an existing image in edit mode

    // State for the Delete Confirmation Dialog
    val showDeleteConfirmDialog: Boolean = false,
    val bannerToDeleteId: String? = null
)

@HiltViewModel
class PhotosViewModel @Inject constructor(
    private val dealerRepository: DealerRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PhotosUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchBanners()
        fetchGalleryImages()
    }

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun fetchBanners() {
        viewModelScope.launch {
            _uiState.update { it.copy(isBannersLoading = true) }
            val dealerId = sessionManager.getDealerSlug()?.toLongOrNull() ?: return@launch
            dealerRepository.getBanners(dealerId)
                .onSuccess { banners -> _uiState.update { it.copy(isBannersLoading = false, banners = banners) } }
                .onFailure { error -> _uiState.update { it.copy(isBannersLoading = false, error = error.message) } }
        }
    }

    fun fetchGalleryImages() {
        Log.d("GalleryImages", "Attempting to fetch images...")
        viewModelScope.launch {
            _uiState.update { it.copy(isGalleryLoading = true) }
            val dealerId = sessionManager.getDealerSlug()?.toLongOrNull()

            if (dealerId == null) {
                _uiState.update { it.copy(isGalleryLoading = true, error = "Dealer ID not found.") }
                Log.e("GalleryImages", "Fetch failed: Dealer ID was null.")
                return@launch
            }

            dealerRepository.getGalleryImages(dealerId)
                .onSuccess { images ->
                    // This log will tell us if the call succeeded and how many images were parsed
                    Log.d("GalleryImages", "SUCCESS: Fetched and parsed ${images.size} images.")
                    _uiState.update { it.copy(isGalleryLoading = false, galleryImages = images) }
                }
                .onFailure { error ->
                    // This log will show the exact error if something went wrong
                    Log.e("GalleryImages", "FAILURE: Could not fetch gallery images.", error)
                    _uiState.update { it.copy(isGalleryLoading = false, error = error.message) }
                }
        }
    }

    // --- REFACTORED: Functions to manage the Add/Edit dialog ---

    fun onAddBannerClicked() {
        _uiState.update {
            it.copy(
                isManageBannerDialogVisible = true,
                isEditingBanner = false,
                bannerIdToEdit = null, bannerTitle = "", bannerUrl = "",
                bannerImageUri = null, bannerExistingImageUrl = null, error = null
            )
        }
    }

    fun onEditBannerClicked(bannerId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBannersLoading = true) }
            val dealerId = sessionManager.getDealerSlug()?.toLongOrNull() ?: return@launch
            dealerRepository.getBannerDetails(dealerId, bannerId)
                .onSuccess { banner ->
                    _uiState.update {
                        it.copy(
                            isBannersLoading = true,
                            isManageBannerDialogVisible = true,
                            isEditingBanner = true,
                            bannerIdToEdit = banner.id,
                            bannerTitle = banner.title ?: "",
                            bannerUrl = banner.url ?: "",
                            bannerExistingImageUrl = banner.imageUrl,
                            bannerImageUri = null,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isBannersLoading = true, error = error.message) }
                }
        }
    }

    fun onDismissManageBannerDialog() {
        _uiState.update { it.copy(isManageBannerDialogVisible = false) }
    }

    fun onBannerTitleChanged(title: String) { _uiState.update { it.copy(bannerTitle = title) } }
    fun onBannerUrlChanged(url: String) { _uiState.update { it.copy(bannerUrl = url) } }
    fun onBannerImageSelected(uri: Uri) { _uiState.update { it.copy(bannerImageUri = uri) } }

    // --- ADDED: Functions to manage the Delete dialog ---

    fun onDeleteBannerClicked(bannerId: String) {
        _uiState.update { it.copy(showDeleteConfirmDialog = true, bannerToDeleteId = bannerId) }
    }

    fun onDismissDeleteDialog() {
        _uiState.update { it.copy(showDeleteConfirmDialog = false, bannerToDeleteId = null) }
    }

//    fun confirmDeleteBanner() {
//        viewModelScope.launch {
//            val dealerId = sessionManager.getDealerSlug()?.toLongOrNull()
//            val bannerId = _uiState.value.bannerToDeleteId
//            if (dealerId == null || bannerId == null) return@launch
//
//            _uiState.update { it.copy(isLoading = true) }
//            dealerRepository.deleteBanner(dealerId, bannerId)
//                .onSuccess {
//                    onDismissDeleteDialog()
//                    fetchBanners()
//                }
//                .onFailure { error ->
//                    _uiState.update { it.copy(isLoading = false, error = error.message) }
//                }
//        }
//    }

    // --- ADDED: Single save function that decides to add or update ---
    fun onSaveBanner(context: Context) {
        if (_uiState.value.isEditingBanner) {
            updateBanner(context)
        } else {
            addBanner(context)
        }
    }

    private fun addBanner(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isManageBannerDialogVisible = true) }
        }
    }

    private fun updateBanner(context: Context) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val dealerId = sessionManager.getDealerSlug()?.toLongOrNull()
            val bannerId = currentState.bannerIdToEdit

            if (dealerId == null || bannerId == null || currentState.bannerTitle.isBlank() || currentState.bannerUrl.isBlank()) {
                _uiState.update { it.copy(error = "Title and URL are required.") }
                return@launch
            }

            _uiState.update { it.copy(isBannersLoading = true) }
            val imageFile = currentState.bannerImageUri?.let { uriToFile(context, it) }
            val startDate = (System.currentTimeMillis() / 1000).toString()

            dealerRepository.updateBanner(
                dealerId = dealerId, bannerId = bannerId, title = currentState.bannerTitle,
                url = currentState.bannerUrl, startDate = startDate, imageFile = imageFile
            ).onSuccess {
                onDismissManageBannerDialog()
                fetchBanners()
            }.onFailure { error ->
                _uiState.update { it.copy(isBannersLoading = true, error = error.message) }
            }
        }
    }

    fun uploadGalleryImages(context: Context, uris: List<Uri>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGalleryLoading = true) }
            val dealerId = sessionManager.getDealerSlug()?.toLongOrNull()
            if (dealerId == null) {
                _uiState.update { it.copy(isGalleryLoading = true, error = "Dealer ID not found.") }
                return@launch
            }

            // This will track if any upload fails
            var didFail = false

            // Upload each file
            uris.forEach { uri ->
                val file = uriToFile(context, uri)
                if (file != null) {
                    dealerRepository.addGalleryImage(dealerId, file)
                        .onFailure { error ->
                            didFail = true
                            _uiState.update { it.copy(error = error.message) }
                        }
                }
            }

            // After all uploads are attempted, refresh the list
            fetchGalleryImages()
        }
    }

}