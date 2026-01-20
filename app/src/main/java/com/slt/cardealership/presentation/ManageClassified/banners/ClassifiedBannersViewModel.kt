package com.slt.cardealership.presentation.ManageClassified.banners

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.data.local.SessionManager
import com.slt.cardealership.domain.model.Banner
import com.slt.cardealership.domain.repo.DealerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface BannersUiState {
    data object Loading : BannersUiState
    data class Success(
        val banners: List<Banner>,
        val domainName: String
    ) : BannersUiState
    data class Error(val message: String) : BannersUiState
}

@HiltViewModel
class ClassifiedBannersViewModel @Inject constructor(
    private val repository: DealerRepository,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val siteId: String? = savedStateHandle["siteId"]

    private val _uiState = MutableStateFlow<BannersUiState>(BannersUiState.Loading)
    val uiState: StateFlow<BannersUiState> = _uiState.asStateFlow()

    private var dealerId: Long? = null
    private var domainId: Int? = null
    private var domainName: String? = null

    init {
        fetchData()
    }

    fun fetchData() {
        val id = siteId?.toIntOrNull()
        if (id == null) {
            _uiState.update { BannersUiState.Error("Invalid Site ID") }
            return
        }

        viewModelScope.launch {
            _uiState.update { BannersUiState.Loading }

            val domainResult = repository.getDomainDetails(id)

            domainResult.onSuccess { domainItem ->
                domainName = domainItem.domainName
                domainId = id

                if (domainItem.dealerId != null) {
                    dealerId = domainItem.dealerId.toLong()
                } else {
                    dealerId = sessionManager.getDealerId()?.toLong()
                }

                val currentDealerId = dealerId
                if (currentDealerId != null) {
                    repository.getBanners(
                        dealerId = currentDealerId,
                        page = 1,
                        itemsPerPage = 20,
                        domainId = domainId
                    ).onSuccess { response ->
                        _uiState.update {
                            BannersUiState.Success(
                                banners = response.list,
                                domainName = domainItem.domainName
                            )
                        }
                    }.onFailure { error ->
                        _uiState.update { BannersUiState.Error(error.message ?: "Failed to fetch banners") }
                    }
                } else {
                    _uiState.update { BannersUiState.Error("Dealer ID not found") }
                }
            }.onFailure { exception ->
                _uiState.update { BannersUiState.Error("Failed to fetch domain details: ${exception.localizedMessage ?: "Unknown error"}") }
            }
        }
    }

    fun deleteBanner(bannerId: String) {
        viewModelScope.launch {
            val currentDealerId = dealerId
            if (currentDealerId == null) {
                fetchData()
                return@launch
            }

            // Optimistically remove from UI
            val currentState = _uiState.value
            if (currentState is BannersUiState.Success) {
                _uiState.update {
                    BannersUiState.Success(
                        banners = currentState.banners.filterNot { it.id == bannerId },
                        domainName = currentState.domainName
                    )
                }
            }

            // Call API
            repository.deleteBanner(currentDealerId, bannerId)
                .onSuccess {
                    // Success - item already removed from UI
                }
                .onFailure {
                    // On failure, refresh to restore accurate state
                    fetchData()
                }
        }
    }
}
