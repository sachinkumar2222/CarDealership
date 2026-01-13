package com.slt.cardealership.presentation.websitedashboard.sliders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.domain.model.DomainSlider
import com.slt.cardealership.domain.repo.DealerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface WebsiteSlidersUiState {
    data object Loading : WebsiteSlidersUiState
    data class Success(val sliders: List<DomainSlider>) : WebsiteSlidersUiState
    data class Error(val message: String) : WebsiteSlidersUiState
}

@HiltViewModel
class WebsiteSlidersViewModel @Inject constructor(
    private val dealerRepository: DealerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WebsiteSlidersUiState>(WebsiteSlidersUiState.Loading)
    val uiState: StateFlow<WebsiteSlidersUiState> = _uiState.asStateFlow()

    fun fetchSliders(domainId: Int) {
        viewModelScope.launch {
            _uiState.value = WebsiteSlidersUiState.Loading
            dealerRepository.getDomainSliders(
                page = 1,
                itemsPerPage = 100, // Fetch all for now or implement pagination later
                domainId = domainId,
                search = ""
            ).onSuccess { sliders ->
                _uiState.value = WebsiteSlidersUiState.Success(sliders)
            }.onFailure { error ->
                _uiState.value = WebsiteSlidersUiState.Error(error.message ?: "Failed to fetch sliders")
            }
        }
    }
    fun deleteSlider(domainId: Int, sliderId: String) {
        viewModelScope.launch {
            dealerRepository.deleteDomainSlider(sliderId)
                .onSuccess {
                    fetchSliders(domainId)
                }
                .onFailure { error ->
                    _uiState.value = WebsiteSlidersUiState.Error(error.message ?: "Failed to delete slider")
                }
        }
    }
}
