package com.slt.cardealership.presentation.websitedashboard.sliders

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.domain.model.DomainSlideItem
import com.slt.cardealership.domain.repo.DealerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class AddSliderUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false, // For initial creation or final save
    val sliderName: String = "",
    val sliderId: String? = null, // If null, we are in "Create" mode. If set, "Edit" mode.
    val slides: List<DomainSlideItem> = emptyList(),
    val showAddSlideDialog: Boolean = false,
    val isSlideLoading: Boolean = false, // Loading state for adding a slide
    val slideError: String? = null,
    val editingSlide: DomainSlideItem? = null
)

@HiltViewModel
class AddSliderViewModel @Inject constructor(
    private val dealerRepository: DealerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddSliderUiState())
    val uiState: StateFlow<AddSliderUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        _uiState.update { it.copy(sliderName = name) }
    }

    fun createSlider(domainId: Int) {
        val name = _uiState.value.sliderName
        if (name.isBlank()) {
            _uiState.update { it.copy(error = "Slider name is required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = dealerRepository.createDomainSlider(name, domainId.toString())
            result.onSuccess { id ->
                _uiState.update { it.copy(isLoading = false, isSuccess = true, sliderId = id) }
                // Fetch details to ensure we have the latest state (though it's empty initially)
                fetchSliderDetails(id)
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
        }
    }
    
    // This function is needed if we enter the screen in "Edit" mode
    fun fetchSliderDetails(sliderId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, sliderId = sliderId) }
            val result = dealerRepository.getDomainSliderDetails(sliderId)
            result.onSuccess { details ->
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        sliderName = details.name,
                        slides = details.slides
                    ) 
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
        }
    }

    fun showAddSlideDialog() {
        _uiState.update { it.copy(showAddSlideDialog = true) }
    }

    fun onEditSlide(slide: DomainSlideItem) {
        _uiState.update { it.copy(editingSlide = slide, showAddSlideDialog = true) }
    }

    fun hideAddSlideDialog() {
        _uiState.update { it.copy(showAddSlideDialog = false, editingSlide = null) }
    }

    fun addSlide(
        domainId: Int,
        title: String,
        link: String,
        target: String,
        startDate: Long,
        endDate: Long,
        imageFile: File
    ) {
        val sliderId = _uiState.value.sliderId ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSlideLoading = true, slideError = null) }
            val result = dealerRepository.addDomainSlideItem(
                sliderId = sliderId,
                domainId = domainId.toString(),
                title = title,
                link = link,
                target = target,
                startDate = startDate,
                endDate = endDate,
                imageFile = imageFile
            )
            result.onSuccess {
                _uiState.update { it.copy(isSlideLoading = false, showAddSlideDialog = false) }
                fetchSliderDetails(sliderId) // Refresh list
            }.onFailure { error ->
                _uiState.update { it.copy(isSlideLoading = false, slideError = error.message) }
            }
        }
    }

    fun updateSlide(
        domainId: Int,
        slideId: String,
        title: String,
        link: String,
        target: String,
        startDate: Long,
        endDate: Long,
        imageFile: File?
    ) {
        val sliderId = _uiState.value.sliderId ?: return
        val currentSlide = _uiState.value.editingSlide ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSlideLoading = true, slideError = null) }
            val result = dealerRepository.updateDomainSlideItem(
                sliderId = sliderId,
                slideId = slideId,
                domainId = domainId.toString(),
                title = title,
                link = link,
                target = target,
                startDate = startDate,
                endDate = endDate,
                imageFile = imageFile,
                imageUrl = if (imageFile == null) currentSlide.imageUrl else null
            )
            result.onSuccess {
                _uiState.update { it.copy(isSlideLoading = false, showAddSlideDialog = false, editingSlide = null) }
                fetchSliderDetails(sliderId) // Refresh list
            }.onFailure { error ->
                _uiState.update { it.copy(isSlideLoading = false, slideError = error.message) }
            }
        }
    }
}
