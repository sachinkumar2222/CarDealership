package com.slt.cardealership.presentation.articles

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.*
import androidx.navigation.toRoute
import com.slt.cardealership.data.local.SessionManager
import com.slt.cardealership.domain.model.Post
import com.slt.cardealership.domain.model.PostCta
import com.slt.cardealership.domain.model.SeoTag
import com.slt.cardealership.domain.repo.PostRepository
import com.slt.cardealership.presentation.home.HomeRoutes
import com.slt.cardealership.utils.uriToFile // Make sure you have this utility function
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditArticleState(
    val post: Post? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isEditMode: Boolean = false,
    val availableTags: List<SeoTag> = emptyList() // Added availableTags
)

@HiltViewModel
class AddEditArticleViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var state by mutableStateOf(AddEditArticleState())
        private set

    private val articleArgs: HomeRoutes.AddEditArticle = savedStateHandle.toRoute()
    private val articleId = articleArgs.articleId

    private val _eventChannel = Channel<UiEvent>()
    val events = _eventChannel.receiveAsFlow()

    init {
        loadSeoTags() // Fetch tags on init

        if (articleId != null && articleId != "null") {
            state = state.copy(isEditMode = true)
            loadArticle(articleId)
        } else {
            state = state.copy(
                isLoading = false,
                isEditMode = false,
                post = Post(
                    id = null,
                    dealerId = null,
                    name = "",
                    slug = "",
                    status = "published",
                    metaTitle = "",
                    metaDescription = "",
                    image = null,
                    content = "",
                    createdOn = null,
                    tags = emptyList(),
                    ctas = listOf(PostCta("", ""), PostCta("", "")) // Default 2 empty CTAs
                )
            )
        }
    }

    private fun loadSeoTags() {
        viewModelScope.launch {
            val dealerId = sessionManager.getDealerSlug()?.toLongOrNull()
            if (dealerId != null) {
                postRepository.getDealerSeoTags(dealerId)
                    .onSuccess { tags ->
                        state = state.copy(availableTags = tags)
                    }
                    .onFailure {
                        // Log or handle error, but don't block the screen
                        Log.e("AddEditArticleViewModel", "Failed to load SEO tags", it)
                    }
            }
        }
    }

    private fun loadArticle(id: String) {
        viewModelScope.launch {
            val dealerId = sessionManager.getDealerSlug()?.toLongOrNull() ?: return@launch
            postRepository.getPostById(dealerId, id)
                .onSuccess { post ->
                    // Ensure CTAs list is mutable or at least present
                    // Ensure CTAs list is mutable or at least present, and has 2 items
                    val currentCtas = post.ctas?.toMutableList() ?: mutableListOf()
                    while (currentCtas.size < 2) {
                        currentCtas.add(PostCta("", ""))
                    }
                    state = state.copy(post = post.copy(ctas = currentCtas), isLoading = false)
                }
                .onFailure { state = state.copy(error = it.message, isLoading = false) }
        }
    }

    // --- FIELD UPDATES ---
    fun onTitleChange(newValue: String) {
        state = state.copy(post = state.post?.copy(name = newValue))
    }
    fun onSlugChange(newValue: String) {
        state = state.copy(post = state.post?.copy(slug = newValue))
    }
    fun onMetaTitleChange(newValue: String) {
        state = state.copy(post = state.post?.copy(metaTitle = newValue))
    }
    fun onMetaDescriptionChange(newValue: String) {
        state = state.copy(post = state.post?.copy(metaDescription = newValue))
    }
    fun onContentChange(newValue: String) {
        state = state.copy(post = state.post?.copy(content = newValue))
    }
    fun onStatusChange(newValue: String) {
        state = state.copy(post = state.post?.copy(status = newValue))
    }

    // --- TAGS ---
    fun onTagsChange(newTags: List<SeoTag>) {
        state = state.copy(post = state.post?.copy(tags = newTags))
    }

    // --- CTAs ---
    fun onCtaLabelChange(index: Int, newLabel: String) {
        val currentCtas = state.post?.ctas?.toMutableList() ?: return
        if (index in currentCtas.indices) {
            currentCtas[index] = currentCtas[index].copy(label = newLabel)
            state = state.copy(post = state.post?.copy(ctas = currentCtas))
        }
    }

    fun onCtaUrlChange(index: Int, newUrl: String) {
        val currentCtas = state.post?.ctas?.toMutableList() ?: return
        if (index in currentCtas.indices) {
            currentCtas[index] = currentCtas[index].copy(url = newUrl)
            state = state.copy(post = state.post?.copy(ctas = currentCtas))
        }
    }

    // --- IMAGE HANDLING ---
    fun onImageRemoved() {
        state = state.copy(post = state.post?.copy(image = null))
    }

    fun onImageSelected(uri: Uri, context: Context) {
        viewModelScope.launch {
            state = state.copy(isSaving = true)
            val dealerId = sessionManager.getDealerSlug()?.toLongOrNull() ?: return@launch
            val imageFile = uriToFile(context, uri) ?: return@launch

            postRepository.uploadPostImage(dealerId, imageFile)
                .onSuccess { imageUrl ->
                    state = state.copy(
                        isSaving = false,
                        post = state.post?.copy(image = imageUrl)
                    )
                }
                .onFailure {
                    state = state.copy(isSaving = false)
                    _eventChannel.send(UiEvent.ShowSnackbar("Image upload failed."))
                }
        }
    }

    fun onSave() {
        viewModelScope.launch {
            state = state.copy(isSaving = true)
            val dealerId = sessionManager.getDealerSlug()?.toLongOrNull() ?: return@launch
            val currentPost = state.post ?: return@launch

            if (currentPost.name.isNullOrBlank()) {
                _eventChannel.send(UiEvent.ShowSnackbar("Title cannot be empty."))
                state = state.copy(isSaving = false)
                return@launch
            }

            val result = if (articleId == null || articleId == "null") {
                postRepository.addPost(dealerId, currentPost)
            } else {
                postRepository.updatePost(dealerId, articleId, currentPost)
            }

            result.onSuccess {
                _eventChannel.send(UiEvent.NavigateBack)
            }.onFailure {
                _eventChannel.send(UiEvent.ShowSnackbar(it.message ?: "Error saving post."))
            }
            state = state.copy(isSaving = false)
        }
    }

    sealed class UiEvent {
        object NavigateBack : UiEvent()
        data class ShowSnackbar(val message: String) : UiEvent()
    }
}