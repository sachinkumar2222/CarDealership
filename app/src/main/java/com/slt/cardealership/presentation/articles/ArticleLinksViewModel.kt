package com.slt.cardealership.presentation.articles

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.data.local.SessionManager
import com.slt.cardealership.domain.model.ArticleLink
import com.slt.cardealership.domain.repo.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ArticleLinksUiState {
    object Loading : ArticleLinksUiState()
    data class Success(val links: List<ArticleLink>) : ArticleLinksUiState()
    data class Error(val message: String) : ArticleLinksUiState()
}

@HiltViewModel
class ArticleLinksViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<ArticleLinksUiState>(ArticleLinksUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _eventChannel = Channel<UiEvent>()
    val events = _eventChannel.receiveAsFlow()

    // State for the bottom sheet input
    var newLinkUrl by mutableStateOf("")

    init {
        fetchLinks()
    }

    fun fetchLinks() {
        viewModelScope.launch {
            _uiState.value = ArticleLinksUiState.Loading
            val dealerId = sessionManager.getDealerSlug()?.toLongOrNull()
            if (dealerId == null) {
                _uiState.value = ArticleLinksUiState.Error("Dealer ID not found.")
                return@launch
            }
            postRepository.getArticleLinks(dealerId)
                .onSuccess { links -> _uiState.value = ArticleLinksUiState.Success(links) }
                .onFailure { _uiState.value = ArticleLinksUiState.Error(it.message ?: "Failed to load links.") }
        }
    }

    fun addLink() {
        viewModelScope.launch {
            if (newLinkUrl.isBlank()) {
                _eventChannel.send(UiEvent.ShowSnackbar("Link cannot be empty"))
                return@launch
            }

            val dealerId = sessionManager.getDealerSlug()?.toLongOrNull() ?: return@launch

            // Assume "general" type based on user request "type: general"
            postRepository.addArticleLink(dealerId, newLinkUrl, "general")
                .onSuccess {
                    newLinkUrl = "" // Clear input
                    fetchLinks() // Refresh list
                    _eventChannel.send(UiEvent.LinkAdded)
                }
                .onFailure {
                    _eventChannel.send(UiEvent.ShowSnackbar(it.message ?: "Failed to add link"))
                }
        }
    }

    fun deleteLink(linkId: String) {
        viewModelScope.launch {
            val dealerId = sessionManager.getDealerSlug()?.toLongOrNull() ?: return@launch
            postRepository.deleteArticleLink(dealerId, linkId)
                .onSuccess { fetchLinks() }
                .onFailure { _eventChannel.send(UiEvent.ShowSnackbar("Failed to delete link")) }
        }
    }

    sealed class UiEvent {
        object LinkAdded : UiEvent()
        data class ShowSnackbar(val message: String) : UiEvent()
    }
}
