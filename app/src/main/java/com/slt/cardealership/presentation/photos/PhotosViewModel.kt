package com.slt.cardealership.presentation.photos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.data.local.SessionManager
import com.slt.cardealership.domain.model.Banner
import com.slt.cardealership.domain.model.GalleryImage
import com.slt.cardealership.domain.repo.DealerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// A simpler, single state class to hold all UI data
data class PhotosUiState(
    val selectedTab: Int = 1, // 0: Google, 1: Banners, 2: Gallery
    val banners: List<Banner> = emptyList(),
    val galleryImages: List<GalleryImage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
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
            _uiState.update { it.copy(isLoading = true) }
            val dealerId = sessionManager.getDealerSlug()?.toLongOrNull()
            if (dealerId == null) {
                _uiState.update { it.copy(isLoading = false, error = "Dealer ID not found.") }
                return@launch
            }

            dealerRepository.getBanners(dealerId)
                .onSuccess { banners ->
                    _uiState.update { it.copy(isLoading = false, banners = banners) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    fun fetchGalleryImages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val dealerId = sessionManager.getDealerSlug()?.toLongOrNull()
            if (dealerId == null) {
                _uiState.update { it.copy(isLoading = false, error = "Dealer ID not found.") }
                return@launch
            }

            dealerRepository.getGalleryImages(dealerId)
                .onSuccess { images ->
                    _uiState.update { it.copy(isLoading = false, galleryImages = images) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }
}