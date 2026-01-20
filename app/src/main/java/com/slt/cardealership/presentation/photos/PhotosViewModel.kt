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

// --- CORRECTED STATE CLASS ---
data class PhotosUiState(
    val selectedTab: Int = 1,
    val banners: List<Banner> = emptyList(),
    val galleryImages: List<GalleryImage> = emptyList(),
    val isBannersLoading: Boolean = false,
    val isGalleryLoading: Boolean = false,
    val error: String? = null,
    val isManageBannerDialogVisible: Boolean = false,
    val isEditingBanner: Boolean = false,
    val bannerIdToEdit: String? = null,
    val bannerTitle: String = "",
    val bannerUrl: String = "",
    val bannerImageUri: Uri? = null,
    val bannerExistingImageUrl: String? = null,
    // States for both Banner and Gallery delete confirmations
    val showDeleteConfirmDialog: Boolean = false,
    val itemToDeleteId: String? = null,
    val isDeletingBanner: Boolean = true // To differentiate which delete dialog is for
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
            dealerRepository.getBanners(dealerId, 1, 100, null)
                .onSuccess { response -> _uiState.update { it.copy(isBannersLoading = false, banners = response.list) } }
                .onFailure { error -> _uiState.update { it.copy(isBannersLoading = false, error = error.message) } }
        }
    }

    fun fetchGalleryImages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGalleryLoading = true) }
            val dealerId = sessionManager.getDealerSlug()?.toLongOrNull() ?: return@launch
            dealerRepository.getGalleryImages(dealerId)
                .onSuccess { images -> _uiState.update { it.copy(isGalleryLoading = false, galleryImages = images) } }
                .onFailure { error -> _uiState.update { it.copy(isGalleryLoading = false, error = error.message) } }
        }
    }

    fun onAddBannerClicked() {
        _uiState.update {
            it.copy(
                isManageBannerDialogVisible = true, isEditingBanner = false, bannerIdToEdit = null,
                bannerTitle = "", bannerUrl = "", bannerImageUri = null, bannerExistingImageUrl = null, error = null
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
                            // --- FIX: SET LOADING TO FALSE ---
                            isBannersLoading = false,
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
                    // --- FIX: SET LOADING TO FALSE ---
                    _uiState.update { it.copy(isBannersLoading = false, error = error.message) }
                }
        }
    }

    fun onDismissManageBannerDialog() {
        _uiState.update { it.copy(isManageBannerDialogVisible = false) }
    }

    fun onBannerTitleChanged(title: String) { _uiState.update { it.copy(bannerTitle = title) } }
    fun onBannerUrlChanged(url: String) { _uiState.update { it.copy(bannerUrl = url) } }
    fun onBannerImageSelected(uri: Uri) { _uiState.update { it.copy(bannerImageUri = uri) } }

    fun onSaveBanner(context: Context) {
        if (_uiState.value.isEditingBanner) {
            updateBanner(context)
        } else {
            addBanner(context)
        }
    }

    private fun addBanner(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBannersLoading = true) }
            val currentState = _uiState.value
            val dealerId = sessionManager.getDealerSlug()?.toLongOrNull()
            val imageFile = currentState.bannerImageUri?.let { uriToFile(context, it) }

            if (dealerId == null || imageFile == null || currentState.bannerTitle.isBlank()) {
                _uiState.update { it.copy(isBannersLoading = false, error = "Title and Image are required.") }
                return@launch
            }
            val startDate = System.currentTimeMillis() / 1000

            dealerRepository.addBanner(
                dealerId = dealerId,
                domainId = 0, // Default/Placeholder domain ID
                title = currentState.bannerTitle,
                url = currentState.bannerUrl,
                startDate = startDate,
                endDate = null,
                imageFile = imageFile
            )
                .onSuccess {
                    onDismissManageBannerDialog()
                    fetchBanners()
                }.onFailure { error ->
                    _uiState.update { it.copy(isBannersLoading = false, error = error.message) }
                }
        }
    }

    private fun updateBanner(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBannersLoading = true) }
            val currentState = _uiState.value
            val dealerId = sessionManager.getDealerSlug()?.toLongOrNull()
            val bannerId = currentState.bannerIdToEdit

            if (dealerId == null || bannerId == null) {
                _uiState.update { it.copy(isBannersLoading = false, error = "Cannot update banner.") }
                return@launch
            }
            val imageFile = currentState.bannerImageUri?.let { uriToFile(context, it) }
            val startDate = System.currentTimeMillis() / 1000

            dealerRepository.updateBanner(
                dealerId = dealerId,
                bannerId = bannerId,
                domainId = 0, // Default/Placeholder domain ID
                title = currentState.bannerTitle,
                url = currentState.bannerUrl,
                startDate = startDate,
                endDate = null,
                imageFile = imageFile,
                imageUrl = currentState.bannerExistingImageUrl,
                createdBy = null
            )
                .onSuccess {
                    onDismissManageBannerDialog()
                    fetchBanners()
                }.onFailure { error ->
                    // --- FIX: SET LOADING TO FALSE ON FAILURE ---
                    _uiState.update { it.copy(isBannersLoading = false, error = error.message) }
                }
        }
    }

    // --- ADDED: Full Delete Functionality ---
    fun onDeleteBannerClicked(bannerId: String) {
        _uiState.update { it.copy(showDeleteConfirmDialog = true, itemToDeleteId = bannerId, isDeletingBanner = true) }
    }

    fun onDeleteGalleryImageClicked(imageId: String) {
        Log.d("PhotosViewModel", "onDeleteGalleryImageClicked: $imageId")
        _uiState.update { it.copy(showDeleteConfirmDialog = true, itemToDeleteId = imageId, isDeletingBanner = false) }
    }

    fun onDismissDeleteDialog() {
        _uiState.update { it.copy(showDeleteConfirmDialog = false, itemToDeleteId = null) }
    }

    fun confirmDelete() {
        if (_uiState.value.isDeletingBanner) {
            confirmDeleteBanner()
        } else {
            confirmDeleteGalleryImage()
        }
    }

    private fun confirmDeleteBanner() {
        viewModelScope.launch {
            val dealerId = sessionManager.getDealerSlug()?.toLongOrNull()
            val bannerId = _uiState.value.itemToDeleteId
            if (dealerId == null || bannerId == null) return@launch

            _uiState.update { it.copy(isBannersLoading = true) }
            dealerRepository.deleteBanner(dealerId, bannerId)
                .onSuccess {
                    onDismissDeleteDialog()
                    fetchBanners()
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isBannersLoading = false, error = error.message) }
                }
        }
    }

    private fun confirmDeleteGalleryImage() {
        viewModelScope.launch {
            val dealerId = sessionManager.getDealerSlug()?.toLongOrNull()
            val imageId = _uiState.value.itemToDeleteId
            if (dealerId == null || imageId == null) return@launch

            _uiState.update { it.copy(isGalleryLoading = true) }
            dealerRepository.deleteGalleryImage(dealerId, imageId)
                .onSuccess {
                    onDismissDeleteDialog()
                    fetchGalleryImages()
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isGalleryLoading = false, error = error.message) }
                }
        }
    }

    // In: presentation/photos/PhotosViewModel.kt

    fun uploadGalleryImages(context: Context, uris: List<Uri>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGalleryLoading = true) }
            val dealerId = sessionManager.getDealerSlug()?.toLongOrNull()
            if (dealerId == null) {
                _uiState.update { it.copy(isGalleryLoading = false, error = "Dealer ID not found.") }
                return@launch
            }

            // This variable will hold the most recent list of image URLs.
            var currentImageUrls = _uiState.value.galleryImages.mapNotNull { it.imageUrl }

            // Upload each selected file one at a time.
            for (uri in uris) {
                val file = uriToFile(context, uri)
                if (file != null) {
                    // Call the repository with the new file AND the current list of URLs.
                    val result = dealerRepository.addGalleryImage(dealerId, file, currentImageUrls)

                    if (result.isFailure) {
                        _uiState.update { it.copy(error = result.exceptionOrNull()?.message, isGalleryLoading = false) }
                        return@launch // Stop the process if any upload fails.
                    }

                    // After a successful upload, we must get the new, updated list from the server
                    // so that the *next* upload includes the image we just added.
                    dealerRepository.getGalleryImages(dealerId)
                        .onSuccess { updatedImages ->
                            currentImageUrls = updatedImages.mapNotNull { it.imageUrl }
                        }
                        .onFailure { error ->
                            _uiState.update { it.copy(error = error.message, isGalleryLoading = false) }
                            return@launch
                        }
                }
            }

            // After all uploads are finished, do a final refresh to ensure UI is consistent.
            fetchGalleryImages()
        }
    }
}