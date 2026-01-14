package com.slt.cardealership.presentation.websitedashboard.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.domain.model.DomainFont
import com.slt.cardealership.domain.model.DomainThemeSetting
import com.slt.cardealership.domain.repo.DealerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ThemeSettingsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val themeSetting: DomainThemeSetting? = null,
    val fonts: List<DomainFont> = emptyList(),
    val isImportSheetVisible: Boolean = false,
    val defaultThemes: List<com.slt.cardealership.domain.model.DomainDefaultTheme> = emptyList()
)

@HiltViewModel
class ThemeSettingsViewModel @Inject constructor(
    private val repository: DealerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ThemeSettingsUiState())
    val uiState: StateFlow<ThemeSettingsUiState> = _uiState.asStateFlow()

    fun init(domainId: Int) {
        fetchThemeSettings(domainId)
        fetchFonts(domainId)
    }

    private fun fetchThemeSettings(domainId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.getDomainThemeSetting(domainId)
            result.fold(
                onSuccess = { settings ->
                    _uiState.update { it.copy(isLoading = false, themeSetting = settings) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
            )
        }
    }

    private fun fetchFonts(domainId: Int) {
        viewModelScope.launch {
            val result = repository.getDomainFonts(domainId)
            result.onSuccess { response ->
                _uiState.update { it.copy(fonts = response.list) }
            }
        }
    }

    fun updateSetting(updatedSettings: DomainThemeSetting) {
        _uiState.update { it.copy(themeSetting = updatedSettings) }
    }

    fun saveSettings() {
        val currentSettings = _uiState.value.themeSetting ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.saveDomainThemeSetting(currentSettings.domainId, currentSettings)
            result.fold(
                onSuccess = { settings ->
                    _uiState.update { it.copy(isLoading = false, themeSetting = settings) }
                    // show success message?
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
            )
        }
    }

    fun uploadImage(file: java.io.File, type: String, domainId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.uploadDomainImage(domainId, file)
            result.fold(
                onSuccess = { responseUrl ->
                    val currentSettings = _uiState.value.themeSetting
                    val updatedSettings = when(type) {
                        "dark_logo" -> currentSettings?.copy(darkLogoUrl = responseUrl)
                        "light_logo" -> currentSettings?.copy(lightLogoUrl = responseUrl)
                        "og_image" -> currentSettings?.copy(ogLogoUrl = responseUrl)
                        "favicon" -> currentSettings?.copy(faviconUrl = responseUrl)
                        else -> currentSettings
                    }
                    if (updatedSettings != null) {
                        _uiState.update { it.copy(isLoading = false, themeSetting = updatedSettings) }
                    } else {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
            )
        }
    }

    fun toggleImportSheet(isVisible: Boolean) {
        _uiState.update { it.copy(isImportSheetVisible = isVisible) }
        if (isVisible && _uiState.value.defaultThemes.isEmpty()) {
            fetchDefaultThemes()
        }
    }

    private fun fetchDefaultThemes() {
        val currentSettings = _uiState.value.themeSetting ?: return
        viewModelScope.launch {
            // Don't show full screen loader for this, maybe just local?
            // keeping it simple for now as per other flows
            val result = repository.getDomainDefaultThemes(currentSettings.domainId)
            result.fold(
                onSuccess = { themes ->
                    _uiState.update { it.copy(defaultThemes = themes) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
            )
        }
    }

    fun removeImage(type: String) {
        val currentSettings = _uiState.value.themeSetting
        val updatedSettings = when(type) {
            "dark_logo" -> currentSettings?.copy(darkLogoUrl = "")
            "light_logo" -> currentSettings?.copy(lightLogoUrl = "")
            "og_image" -> currentSettings?.copy(ogLogoUrl = "")
            "favicon" -> currentSettings?.copy(faviconUrl = "")
            else -> currentSettings
        }
        if (updatedSettings != null) {
            _uiState.update { it.copy(themeSetting = updatedSettings) }
        }
    }
}
