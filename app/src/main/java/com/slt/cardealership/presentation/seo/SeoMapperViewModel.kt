package com.slt.cardealership.presentation.seo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.domain.model.SeoDomain
import com.slt.cardealership.domain.model.SeoTag
import com.slt.cardealership.domain.repo.DealerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SeoMapperUiState(
    val isLoading: Boolean = false,
    val domains: List<SeoDomain> = emptyList(),
    val filteredDomains: List<SeoDomain> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null,
    
    // Sheet State
    val showManageSheet: Boolean = false,
    val selectedDomain: SeoDomain? = null,
    val isSheetLoading: Boolean = false,
    val allTags: List<com.slt.cardealership.domain.model.SeoTag> = emptyList(),
    val filteredTags: List<com.slt.cardealership.domain.model.SeoTag> = emptyList(),
    val assignedTagIds: Set<String> = emptySet(),
    val isSaving: Boolean = false
)

@HiltViewModel
class SeoMapperViewModel @Inject constructor(
    private val repository: DealerRepository,
    private val sessionManager: com.slt.cardealership.data.local.SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeoMapperUiState())
    val uiState: StateFlow<SeoMapperUiState> = _uiState.asStateFlow()

    private var searchJob: kotlinx.coroutines.Job? = null
    
    // For sheet search
    private var allFetchedTags: List<SeoTag> = emptyList()

    init {
        loadDomains()
    }

    fun loadDomains(query: String? = null) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Fetch both types in parallel
                val type6Deferred = async { repository.getSeoDomains(6, query) }
                val type9Deferred = async { repository.getSeoDomains(9, query) }

                val type6Result = type6Deferred.await()
                val type9Result = type9Deferred.await()

                val allDomains = mutableListOf<SeoDomain>()

                type6Result.onSuccess { allDomains.addAll(it) }
                type9Result.onSuccess { allDomains.addAll(it) }

                if (type6Result.isFailure && type9Result.isFailure) {
                     _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load domains: ${type6Result.exceptionOrNull()?.message}"
                    )
                } else {
                    val distinctDomains = allDomains.distinctBy { it.id }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        domains = distinctDomains,
                        filteredDomains = distinctDomains
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        loadDomains(if (query.isBlank()) null else query)
    }

    // --- Manage Sheet Logic ---

    fun openManageSheet(domain: SeoDomain) {
        viewModelScope.launch {
            val dealerId = sessionManager.getDealerId()?.toLong() ?: 0L 
            // In a real app we might want to handle missing dealerId better
            
            _uiState.value = _uiState.value.copy(
                showManageSheet = true,
                selectedDomain = domain,
                isSheetLoading = true,
                assignedTagIds = emptySet(),
                filteredTags = emptyList()
            )

            try {
                val tagsDeferred = async { repository.getSeoTags(dealerId) }
                val assignedDeferred = async { repository.getDomainAcceptedTags(dealerId, domain.id) }
                
                val tagsResult = tagsDeferred.await()
                val assignedResult = assignedDeferred.await()
                
                if (tagsResult.isSuccess && assignedResult.isSuccess) {
                    allFetchedTags = tagsResult.getOrDefault(emptyList())
                    val assignedIds = assignedResult.getOrDefault(emptyList()).toSet()
                    
                    _uiState.value = _uiState.value.copy(
                        isSheetLoading = false,
                        allTags = allFetchedTags,
                        filteredTags = allFetchedTags, // Initially show all
                        assignedTagIds = assignedIds
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSheetLoading = false,
                        error = "Failed to load tags details"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSheetLoading = false, error = e.message)
            }
        }
    }

    fun closeManageSheet() {
        _uiState.value = _uiState.value.copy(showManageSheet = false, selectedDomain = null)
    }

    fun onTagSearch(query: String) {
        val filtered = if (query.isBlank()) {
           allFetchedTags
        } else {
           allFetchedTags.filter { it.tagName.contains(query, ignoreCase = true) }
        }
        _uiState.value = _uiState.value.copy(filteredTags = filtered)
    }

    fun toggleTagSelection(tagId: String) {
        val currentIds = _uiState.value.assignedTagIds.toMutableSet()
        if (currentIds.contains(tagId)) {
            currentIds.remove(tagId)
        } else {
            currentIds.add(tagId)
        }
        _uiState.value = _uiState.value.copy(assignedTagIds = currentIds)
    }

    fun saveDomainTags() {
        val domain = _uiState.value.selectedDomain ?: return
        val tagIds = _uiState.value.assignedTagIds.toList()
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                val request = com.slt.cardealership.domain.model.SeoDomainMapRequest(
                    domainId = domain.id,
                    tags = tagIds
                )
                val result = repository.saveDomainSeoTags(request)
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false, 
                        showManageSheet = false,
                        selectedDomain = null,
                        // Could show success message via effect
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = result.exceptionOrNull()?.message ?: "Save failed"
                    )
                }
            } catch (e: Exception) {
                 _uiState.value = _uiState.value.copy(isSaving = false, error = e.message)
            }
        }
    }
}
