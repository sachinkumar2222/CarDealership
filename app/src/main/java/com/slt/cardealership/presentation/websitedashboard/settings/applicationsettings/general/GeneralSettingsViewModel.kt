package com.slt.cardealership.presentation.websitedashboard.settings.applicationsettings.general

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.domain.model.GeneralSettingsResponse
import com.slt.cardealership.domain.model.UpdateGeneralSettingsRequest
import com.slt.cardealership.domain.repo.DealerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GeneralSettingsUiState(
    val settings: GeneralSettingsResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaving: Boolean = false
)

@HiltViewModel
class GeneralSettingsViewModel @Inject constructor(
    private val repository: DealerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GeneralSettingsUiState())
    val uiState: StateFlow<GeneralSettingsUiState> = _uiState.asStateFlow()

    fun loadSettings(domainId: Int) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = repository.getDomainAppSetting(domainId)
            result.onSuccess { response ->
                _uiState.update { it.copy(isLoading = false, settings = response) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
        }
    }

    fun saveSettings(
        domainId: Int,
        globalStyle: String,
        headerScript: String,
        footerScript: String,
        copyrightContent: String,
        robotsMetaTags: List<String>,
        robotsFileContent: String,
        address: String
    ) {
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val currentSettings = _uiState.value.settings
            val request = UpdateGeneralSettingsRequest(
                domainId = domainId,
                globalStyle = globalStyle,
                headerScript = headerScript,
                footerScript = footerScript,
                copyrightContent = copyrightContent,
                robotsMetaTags = robotsMetaTags,
                robotsFileContent = robotsFileContent,
                address = address,
                advertisementFor = currentSettings?.advertisementFor ?: "none",
                domainUsedForTransaction = currentSettings?.domainUsedForTransaction ?: "",
                sendgridKey = currentSettings?.sendgridKey ?: "",
                createdBy = currentSettings?.createdBy,
                updatedBy = currentSettings?.updatedBy
            )
            val result = repository.updateDomainAppSetting(request)
            result.onSuccess {
                _uiState.update { it.copy(isSaving = false) }
                loadSettings(domainId)
            }.onFailure { error ->
                _uiState.update { it.copy(isSaving = false, error = error.message) }
            }
        }
    }
}
