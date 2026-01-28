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
import com.slt.cardealership.domain.model.DomainItem
import com.slt.cardealership.domain.model.GmbMediaItem

// --- CORRECTED STATE CLASS ---
data class PhotosUiState(
    val selectedTab: Int = 1,
    val banners: List<Banner> = emptyList(),
    val galleryImages: List<GalleryImage> = emptyList(),
    val isBannersLoading: Boolean = false,
    val isGalleryLoading: Boolean = false,
    val error: String? = null,
    // --- Restored Banner Management State ---
    val isManageBannerDialogVisible: Boolean = false,
    val isEditingBanner: Boolean = false,
    val bannerIdToEdit: String? = null,
    val bannerTitle: String = "",
    val bannerUrl: String = "",
    val bannerImageUri: Uri? = null,
    val bannerExistingImageUrl: String? = null,
    val bannerStartDate: Long? = null,
    val bannerEndDate: Long? = null,
    // Note: bannerDomainId removed or ignored for Global Banners

    val showDeleteConfirmDialog: Boolean = false,
    val itemToDeleteId: String? = null,
    val itemToDeleteUrl: String? = null,
    val isDeletingBanner: Boolean = true, // To differentiate which delete dialog is for

    // GMB State
    val isGmbLoading: Boolean = false,
    val gmbImages: List<GmbMediaItem> = emptyList(),
    val gmbError: String? = null
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
        // GMB fetched on tab select? Or init? 
        // Let's lazy load or load on init if we want.
        // For now, load on init to be safe, or just call from UI when tab changes.
    }

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
        if (index == 0 && _uiState.value.gmbImages.isEmpty()) {
            fetchGmbPhotos()
        }
    }

    fun fetchBanners() {
        viewModelScope.launch {
            _uiState.update { it.copy(isBannersLoading = true) }
            val dealerId = sessionManager.getDealerSlug()?.toLongOrNull() ?: return@launch
            dealerRepository.getBanners(dealerId, 1, 10, -1)
                .onSuccess { response ->
                    response.list.forEach {
                        Log.d("BannerDateDebug", "Banner: ${it.title} | createdOn: ${it.createdOn} | startDate: ${it.startDate}")
                    }
                    _uiState.update { it.copy(isBannersLoading = false, banners = response.list) }
                }
                .onFailure { error ->
                    Log.e("BannerDateDebug", "Error fetching banners: ${error.message}")
                    _uiState.update { it.copy(isBannersLoading = false, error = error.message) }
                }
        }
    }

    fun fetchGalleryImages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGalleryLoading = true) }
            val dealerId = sessionManager.getDealerSlug()?.toLongOrNull() ?: return@launch
            dealerRepository.getGalleryImages(dealerId)
                .onSuccess { images ->
                    // Append timestamp to force cache refresh
                    val timestamp = System.currentTimeMillis()
                    val imagesWithCacheBuster = images.map { image ->
                        image.copy(imageUrl = "${image.imageUrl}?t=$timestamp")
                    }
                    _uiState.update { it.copy(isGalleryLoading = false, galleryImages = imagesWithCacheBuster) }
                }
                .onFailure { error -> _uiState.update { it.copy(isGalleryLoading = false, error = error.message) } }
        }
    }

    private fun fetchGmbPhotos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGmbLoading = true, gmbError = null) }
            val dealerId = sessionManager.getDealerSlug()?.toLongOrNull() ?: return@launch
            
            // 1. Get Settings
            dealerRepository.getGmbSettings(dealerId)
                .onSuccess { settings ->
                    val isConnected = settings.tokenResponse?.isConnected == true
                    if (isConnected) {
                        // 2. Get Media if connected
                        dealerRepository.getGmbMedia(dealerId)
                            .onSuccess { mediaResponse ->
                                val allMediaItems = mediaResponse.galleryItems?.flatMap { it.mediaItems ?: emptyList() } ?: emptyList()
                                _uiState.update { it.copy(isGmbLoading = false, gmbImages = allMediaItems) }
                            }
                            .onFailure { error ->
                                _uiState.update { it.copy(isGmbLoading = false, gmbError = "Failed to fetch GMB media: ${error.message}") }
                            }
                    } else {
                        _uiState.update { it.copy(isGmbLoading = false, gmbError = "Google My Business not connected.") }
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isGmbLoading = false, gmbError = "Failed to fetch GMB settings: ${error.message}") }
                }
        }
    }

    fun onAddBannerClicked() {
        _uiState.update {
            it.copy(
                isManageBannerDialogVisible = true,
                isEditingBanner = false,
                bannerIdToEdit = null,
                bannerTitle = "",
                bannerUrl = "",
                bannerImageUri = null,
                bannerExistingImageUrl = null,
                bannerStartDate = System.currentTimeMillis(), // Default to today
                bannerEndDate = null,
                error = null
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
                            isBannersLoading = false,
                            isManageBannerDialogVisible = true,
                            isEditingBanner = true,
                            bannerIdToEdit = banner.id,
                            bannerTitle = banner.title ?: "",
                            bannerUrl = banner.url ?: "",
                            bannerExistingImageUrl = banner.imageUrl,
                            bannerImageUri = null,
                            // Convert Seconds (API) to Millis (UI)
                            bannerStartDate = (banner.startDate ?: 0L) * 1000L,
                            bannerEndDate = banner.endDate?.let { date -> date * 1000L },
                            error = null
                        )
                    }
                }
                .onFailure { error ->
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
    fun onBannerStartDateChanged(date: Long?) { _uiState.update { it.copy(bannerStartDate = date) } }
    fun onBannerEndDateChanged(date: Long?) { _uiState.update { it.copy(bannerEndDate = date) } }

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

            // Convert Millis (UI) to Seconds (API)
            val startDate = (currentState.bannerStartDate ?: System.currentTimeMillis()) / 1000
            val endDate = currentState.bannerEndDate?.div(1000)

            // GLOBAL BANNER: Always Domain ID = null (Repo sends empty string)
            val domainId: Int? = null

            dealerRepository.addBanner(
                dealerId = dealerId,
                domainId = domainId,
                title = currentState.bannerTitle,
                url = currentState.bannerUrl,
                startDate = startDate,
                endDate = endDate,
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
                _uiState.update { it.copy(isBannersLoading = false, error = "Cannot update banner: IDs missing.") }
                return@launch
            }

            if (currentState.bannerTitle.isBlank()) {
                _uiState.update { it.copy(isBannersLoading = false, error = "Title cannot be empty.") }
                return@launch
            }

            val imageFile = currentState.bannerImageUri?.let { uriToFile(context, it) }
            val startDate = (currentState.bannerStartDate ?: System.currentTimeMillis()) / 1000
            val endDate = currentState.bannerEndDate?.div(1000)

            // GLOBAL BANNER: Always Domain ID = null
            val domainIdToSend: Int? = null

            dealerRepository.updateBanner(
                dealerId = dealerId,
                bannerId = bannerId,
                domainId = domainIdToSend,
                title = currentState.bannerTitle,
                url = currentState.bannerUrl,
                startDate = startDate,
                endDate = endDate,
                imageFile = imageFile,
                imageUrl = currentState.bannerExistingImageUrl,
                createdBy = null
            )
                .onSuccess {
                    onDismissManageBannerDialog()
                    fetchBanners()
                }.onFailure { error ->
                    _uiState.update { it.copy(isBannersLoading = false, error = "Update failed: ${error.message}") }
                }
        }
    }

    // --- ADDED: Full Delete Functionality ---
    fun onDeleteBannerClicked(bannerId: String) {
        _uiState.update { it.copy(showDeleteConfirmDialog = true, itemToDeleteId = bannerId, isDeletingBanner = true) }
    }

    fun onDeleteGalleryImageClicked(galleryImage: GalleryImage) {
        Log.d("PhotosViewModel", "onDeleteGalleryImageClicked: ${galleryImage.id} -> ${galleryImage.imageUrl}")
        _uiState.update {
            it.copy(
                showDeleteConfirmDialog = true,
                itemToDeleteId = galleryImage.id,
                itemToDeleteUrl = galleryImage.imageUrl,
                isDeletingBanner = false
            )
        }
    }

    fun onDismissDeleteDialog() {
        _uiState.update { it.copy(showDeleteConfirmDialog = false, itemToDeleteId = null, itemToDeleteUrl = null) }
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

            // Dismiss dialog immediately for better UX
            onDismissDeleteDialog()
            _uiState.update { it.copy(isBannersLoading = true) }

            dealerRepository.deleteBanner(dealerId, bannerId)
                .onSuccess {
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
            val imageUrl = _uiState.value.itemToDeleteUrl // Use URL for deletion

            if (dealerId == null || imageUrl == null) return@launch

            // DEBUG LOGGING
            val currentImages = _uiState.value.galleryImages
            Log.d("PhotosViewModel", "Deleting imageId: $imageId, url: $imageUrl")
            Log.d("PhotosViewModel", "Current images count: ${currentImages.size}")
            // currentImages.forEach { Log.d("PhotosViewModel", "Img: ${it.id} -> ${it.imageUrl}") }

            val newImageList = currentImages.filter {
                // Filter by URL since IDs might be duplicated
                // Compare with cleaned URL just in case, or compare exact if itemToDeleteUrl has timestamp
                // But simpler: just filter out the one we want to delete.
                it.imageUrl != imageUrl
            }
            // CLEAN URLs before sending
            val newImageUrls = newImageList.mapNotNull { it.imageUrl?.substringBefore("?") }

            Log.d("PhotosViewModel", "New image list count: ${newImageList.size}")
            Log.d("PhotosViewModel", "New URLs to send: $newImageUrls")

            // Safety check: Avoid accidental wipe if filtering fails
            if (currentImages.isNotEmpty() && newImageList.isEmpty() && currentImages.size > 1) {
                Log.e("PhotosViewModel", "CRITICAL: Attempted to wipe gallery but only 1 delete requested. Aborting.")
                _uiState.update { it.copy(error = "Error preparing delete list. Please try again.") }
                onDismissDeleteDialog()
                return@launch
            }

            // Dismiss dialog immediately for better UX
            onDismissDeleteDialog()

            _uiState.update { it.copy(isGalleryLoading = true) }

            // Using the new update method that sends only the URLs
            dealerRepository.updateGalleryImages(dealerId, newImageUrls)
                .onSuccess {
                    // Optimistically update UI or fetch? Fetch is safer for consistency.
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
            // IMPORTANT: Strip query params (like timestamp) before sending back to server!
            var currentImageUrls = _uiState.value.galleryImages.mapNotNull {
                it.imageUrl?.substringBefore("?")
            }

            // Upload each selected file one at a time.
            for (uri in uris) {
                val file = uriToFile(context, uri)
                if (file != null) {
                    val result = dealerRepository.addGalleryImage(dealerId, file, currentImageUrls)

                    if (result.isFailure) {
                        _uiState.update { it.copy(error = result.exceptionOrNull()?.message, isGalleryLoading = false) }
                        return@launch // Stop the process if any upload fails.
                    }
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
            fetchGalleryImages()
        }
    }
}