package com.slt.cardealership.presentation.websitedashboard.settings.fonts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.domain.model.DomainFont
import com.slt.cardealership.domain.repo.DealerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class ManageFontsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val fonts: List<DomainFont> = emptyList(),
    val totalFonts: Int = 0,
    val isUploading: Boolean = false
)

@HiltViewModel
class ManageFontsViewModel @Inject constructor(
    private val repository: DealerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageFontsUiState())
    val uiState: StateFlow<ManageFontsUiState> = _uiState.asStateFlow()

    fun loadFonts(domainId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.getDomainFonts(domainId)
            result.fold(
                onSuccess = { response ->
                    _uiState.update { it.copy(isLoading = false, fonts = response.list, totalFonts = response.pagination.total) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
            )
        }
    }

    fun addFont(domainId: Int, name: String, file: File) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true) }
            val result = repository.addDomainFont(domainId, name, file)
            result.fold(
                onSuccess = { newFont ->
                    _uiState.update { state ->
                        state.copy(
                            isUploading = false,
                            fonts = state.fonts + newFont,
                            totalFonts = state.totalFonts + 1
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isUploading = false, error = error.message) }
                }
            )
        }
    }

    fun deleteFont(domainId: Int, fontId: Int) {
        viewModelScope.launch {
            // Optimistic update: Could remove immediately, or wait for success.
            // For now, removing the isLoading = true to prevent full screen flash.
            val result = repository.deleteDomainFont(fontId)
            result.fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            fonts = state.fonts.filter { font -> font.id != fontId },
                            totalFonts = maxOf(0, state.totalFonts - 1)
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
            )
        }
    }
}
