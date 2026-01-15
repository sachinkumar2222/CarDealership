package com.slt.cardealership.presentation.websitedashboard.settings.applicationsettings.socialinfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.domain.model.SocialMediaItem
import com.slt.cardealership.domain.repo.DealerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SocialInfoUiState(
    val socialLinks: List<SocialMediaItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddEditSheet: Boolean = false,
    val editingLink: SocialMediaItem? = null,
    val isSaving: Boolean = false
)

@HiltViewModel
class SocialInfoViewModel @Inject constructor(
    private val repository: DealerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SocialInfoUiState())
    val uiState: StateFlow<SocialInfoUiState> = _uiState.asStateFlow()

    fun fetchSocialLinks(domainId: Int) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = repository.getDomainSocialMedia(domainId)
            result.onSuccess { response ->
                _uiState.update { it.copy(isLoading = false, socialLinks = response.list) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
        }
    }

    fun saveSocialLink(domainId: Int, url: String, mediaType: String) {
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val result = repository.saveDomainSocialMedia(domainId, url, mediaType)
            result.onSuccess {
                _uiState.update { it.copy(isSaving = false, showAddEditSheet = false, editingLink = null) }
                fetchSocialLinks(domainId)
            }.onFailure { error ->
                _uiState.update { it.copy(isSaving = false, error = error.message) }
            }
        }
    }

    fun onAddClick() {
        _uiState.update { it.copy(showAddEditSheet = true, editingLink = null) }
    }

    fun onEditClick(link: SocialMediaItem) {
        _uiState.update { it.copy(showAddEditSheet = true, editingLink = link) }
    }

    fun dismissSheet() {
        _uiState.update { it.copy(showAddEditSheet = false, editingLink = null) }
    }
}
