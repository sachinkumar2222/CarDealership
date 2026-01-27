package com.slt.cardealership.presentation.ManageClassified.faqs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.data.local.SessionManager
import com.slt.cardealership.domain.model.FaqItem
import com.slt.cardealership.domain.repo.DealerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ClassifiedFaqsUiState {
    data object Loading : ClassifiedFaqsUiState
    data class Content(val faqs: List<FaqItem>) : ClassifiedFaqsUiState
    data class Error(val message: String) : ClassifiedFaqsUiState
}

@HiltViewModel
class ClassifiedFaqsViewModel @Inject constructor(
    private val repository: DealerRepository,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val siteId: String? = savedStateHandle["siteId"]

    private val _uiState = MutableStateFlow<ClassifiedFaqsUiState>(ClassifiedFaqsUiState.Loading)
    val uiState: StateFlow<ClassifiedFaqsUiState> = _uiState.asStateFlow()

    private var dealerId: Long? = null
    private var domainId: Int? = null
    private var domainName: String? = null

    init {
        fetchData()
    }

    fun fetchData() {
        val id = siteId?.toIntOrNull()
        if (id == null) {
            _uiState.update { ClassifiedFaqsUiState.Error("Invalid Site ID") }
            return
        }

        viewModelScope.launch {
            _uiState.update { ClassifiedFaqsUiState.Loading }
            val domainResult = repository.getDomainDetails(id)

            domainResult.onSuccess { domainItem ->
                domainId = id
                // Use dealerId from domain or fallback to session
                dealerId = domainItem.dealerId?.toLong() ?: sessionManager.getDealerId()?.toLong()
                domainName = domainItem.domainName // Using domainItem.domainName if available, assuming it exists

                if (dealerId != null) {
                    fetchFaqs(dealerId!!, id, domainItem.domainName)
                } else {
                    _uiState.update { ClassifiedFaqsUiState.Error("Dealer ID not found") }
                }
            }.onFailure { exception ->
                _uiState.update { ClassifiedFaqsUiState.Error("Failed to fetch domain details: ${exception.message}") }
            }
        }
    }

    private suspend fun fetchFaqs(dealerId: Long, domainId: Int, domainName: String?) {
        repository.getFaqs(dealerId, domainId, domainName)
            .onSuccess { faqs ->
                _uiState.update { ClassifiedFaqsUiState.Content(faqs) }
            }
            .onFailure { exception ->
                _uiState.update { ClassifiedFaqsUiState.Error("Failed to fetch FAQs: ${exception.message}") }
            }
    }

}
