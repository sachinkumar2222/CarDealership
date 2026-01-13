package com.slt.cardealership.presentation.websitedashboard.pages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.domain.model.DomainPage
import com.slt.cardealership.domain.model.DomainPageDetails
import com.slt.cardealership.domain.model.DomainPageUpdateRequest
import com.slt.cardealership.domain.model.DomainSlider
import com.slt.cardealership.domain.repo.DealerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class WebsitePagesUiState {
    object Loading : WebsitePagesUiState()
    data class Success(
        val pages: List<DomainPage>,
        val currentPage: Int,
        val totalPages: Int,
        val totalItems: Int,
        val itemsPerPage: Int
    ) : WebsitePagesUiState()
    data class Error(val message: String) : WebsitePagesUiState()
}

@HiltViewModel
class WebsitePagesViewModel @Inject constructor(
    private val dealerRepository: DealerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WebsitePagesUiState>(WebsitePagesUiState.Loading)
    val uiState: StateFlow<WebsitePagesUiState> = _uiState.asStateFlow()

    private val _isAddingPage = MutableStateFlow(false)
    val isAddingPage: StateFlow<Boolean> = _isAddingPage.asStateFlow()

    private var currentDomainId: Int = 0
    private var currentPage: Int = 1
    private val itemsPerPage: Int = 10

    fun fetchPages(domainId: Int, page: Int = 1) {
        currentDomainId = domainId
        currentPage = page
        
        viewModelScope.launch {
            _uiState.value = WebsitePagesUiState.Loading
            val result = dealerRepository.getDomainPages(page, itemsPerPage, domainId)
            result.onSuccess { response ->
                val totalPages = (response.pagination.total + itemsPerPage - 1) / itemsPerPage
                _uiState.value = WebsitePagesUiState.Success(
                    pages = response.list,
                    currentPage = page,
                    totalPages = totalPages,
                    totalItems = response.pagination.total,
                    itemsPerPage = itemsPerPage
                )
            }.onFailure { error ->
                _uiState.value = WebsitePagesUiState.Error(error.message ?: "Failed to load pages")
            }
        }
    }

    fun goToNextPage() {
        val state = _uiState.value
        if (state is WebsitePagesUiState.Success && currentPage < state.totalPages) {
            fetchPages(currentDomainId, currentPage + 1)
        }
    }

    fun goToPreviousPage() {
        if (currentPage > 1) {
            fetchPages(currentDomainId, currentPage - 1)
        }
    }

    fun goToPage(page: Int) {
        val state = _uiState.value
        if (state is WebsitePagesUiState.Success && page in 1..state.totalPages) {
            fetchPages(currentDomainId, page)
        }
    }

    private val _events = kotlinx.coroutines.channels.Channel<WebsitePagesEvent>()
    val events = _events.receiveAsFlow()

    fun deletePage(pageId: String) {
        viewModelScope.launch {
            val result = dealerRepository.deleteDomainPage(pageId)
            result.onSuccess {
                _events.send(WebsitePagesEvent.ShowSuccess("Page deleted successfully"))
                fetchPages(currentDomainId, currentPage) // Refresh list
            }.onFailure { error ->
                _events.send(WebsitePagesEvent.ShowError(error.message ?: "Failed to delete page"))
            }
        }
    }

    fun updatePage(request: com.slt.cardealership.domain.model.DomainPageUpdateRequest) {
        android.util.Log.d("WebsitePagesViewModel", "Updating page: $request")
        viewModelScope.launch {
            val result = dealerRepository.updateDomainPage(request)
            result.onSuccess {
                android.util.Log.d("WebsitePagesViewModel", "Page update successful")
                _events.send(WebsitePagesEvent.ShowSuccess("Page updated successfully"))
                fetchPages(currentDomainId, currentPage) // Refresh list
            }.onFailure { error ->
                android.util.Log.e("WebsitePagesViewModel", "Page update failed", error)
                _events.send(WebsitePagesEvent.ShowError(error.message ?: "Failed to update page"))
            }
        }
    }

    suspend fun getPageDetails(pageId: String): com.slt.cardealership.domain.model.DomainPageDetails? {
        val result = dealerRepository.getDomainPageDetails(pageId)
        return result.getOrNull()
    }

    private val _sliders = kotlinx.coroutines.flow.MutableStateFlow<List<com.slt.cardealership.domain.model.DomainSlider>>(emptyList())
    val sliders = _sliders.asStateFlow()

    fun fetchSliders(domainId: String) {
        viewModelScope.launch {
            val result = dealerRepository.getDomainSliders(
                page = 1,
                itemsPerPage = 100,
                domainId = domainId.toIntOrNull() ?: 0,
                search = ""
            )
            result.onSuccess {
                _sliders.value = it
            }.onFailure {
                android.util.Log.e("WebsitePagesViewModel", "Failed to fetch sliders", it)
            }
        }
    }

    fun addPage(request: com.slt.cardealership.domain.model.DomainPageCreateRequest) {
        android.util.Log.d("WebsitePagesViewModel", "Adding page: $request")
        viewModelScope.launch {
            _isAddingPage.value = true
            val result = dealerRepository.createDomainPage(request)
            result.onSuccess {
                android.util.Log.d("WebsitePagesViewModel", "Page creation successful")
                _events.send(WebsitePagesEvent.ShowSuccess("Page created successfully"))
                fetchPages(currentDomainId, currentPage) // Refresh list
                _isAddingPage.value = false
            }.onFailure { error ->
                android.util.Log.e("WebsitePagesViewModel", "Page creation failed", error)
                _events.send(WebsitePagesEvent.ShowError(error.message ?: "Failed to create page"))
                _isAddingPage.value = false
            }
        }
    }
}

sealed interface WebsitePagesEvent {
    data class ShowSuccess(val message: String) : WebsitePagesEvent
    data class ShowError(val message: String) : WebsitePagesEvent
}
