package com.slt.cardealership.presentation.ManageClassified.articles

import com.slt.cardealership.presentation.navigation.HomeRoutes

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.slt.cardealership.domain.model.Post
import com.slt.cardealership.domain.model.PostCta
import com.slt.cardealership.domain.repo.DealerRepository
import com.slt.cardealership.domain.repo.PostRepository
// Removed wrong import
import com.slt.cardealership.utils.uriToFile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditClassifiedArticleState(
    val post: Post? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isEditMode: Boolean = false,
    val dealerId: Long? = null,
    val availableTags: List<com.slt.cardealership.domain.model.SeoTag> = emptyList(),
    val domainName: String? = null
)

@HiltViewModel
class AddEditClassifiedArticleViewModel @Inject constructor(
    private val dealerRepository: DealerRepository,
    private val postRepository: PostRepository,
    private val sessionManager: com.slt.cardealership.data.local.SessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var state by mutableStateOf(AddEditClassifiedArticleState())
        private set

    private val args: HomeRoutes.AddEditClassifiedArticle = savedStateHandle.toRoute()
    private val siteId = args.siteId
    private val articleId = args.articleId

    private val _eventChannel = Channel<UiEvent>()
    val events = _eventChannel.receiveAsFlow()

    init {
        initializeScreen()
    }


    private fun initializeScreen() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            val domainId = siteId.toIntOrNull()
            if (domainId != null) {
                dealerRepository.getDomainDetails(domainId)
                    .onSuccess { domainItem ->
                        val dealerId = domainItem.dealerId?.toLong() ?: sessionManager.getDealerId()?.toLong()

                        if (dealerId == null) {
                            state = state.copy(isLoading = false, error = "Dealer ID not found for this domain")
                            return@onSuccess
                        }

                        // Fetch SEO Tags
                        val tagsResult = dealerRepository.getSeoTags(dealerId)
                        val availableTags = tagsResult.getOrNull() ?: emptyList()

                        state = state.copy(dealerId = dealerId, availableTags = availableTags, domainName = domainItem.domainName)

                        if (articleId != null && articleId != "null") {
                            state = state.copy(isEditMode = true)
                            loadArticle(dealerId, articleId)
                        } else {
                            // Initialize new post
                            state = state.copy(
                                isLoading = false,
                                isEditMode = false,
                                post = Post(
                                    id = null,
                                    dealerId = dealerId,
                                    name = "",
                                    slug = "",
                                    status = "", // Default to empty for "Select Status"
                                    metaTitle = "",
                                    metaDescription = "",
                                    image = null,
                                    content = "",
                                    createdOn = null,
                                    tags = emptyList(),
                                    ctas = listOf(PostCta("", ""), PostCta("", "")) // Init with 2 empty CTAs
                                )
                            )
                        }
                    }
                    .onFailure {
                        state = state.copy(isLoading = false, error = "Failed to load domain details: ${it.message}")
                    }
            } else {
                state = state.copy(isLoading = false, error = "Invalid Site ID")
            }
        }
    }

    private fun loadArticle(dealerId: Long, id: String) {
        viewModelScope.launch {
            postRepository.getPostById(dealerId, id)
                .onSuccess { post ->
                    // Ensure tags and ctas are not null if backend returns null
                    // Also ensure we have exactly 2 CTAs for editing
                    val currentCtas = post.ctas?.toMutableList() ?: mutableListOf()
                    while (currentCtas.size < 2) {
                        currentCtas.add(PostCta("", ""))
                    }
                    // If more than 2, what to do? Truncate? User said "show two".
                    // Let's keep first 2.
                    val fixedCtas = currentCtas.take(2)

                    val safePost = post.copy(
                        tags = post.tags ?: emptyList(),
                        ctas = fixedCtas
                    )
                    state = state.copy(post = safePost, isLoading = false)
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
    fun onStatusChange(newValue: String) {
        state = state.copy(post = state.post?.copy(status = newValue))
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

    // --- TAGS ---
    fun onTagsChange(newTags: List<com.slt.cardealership.domain.model.SeoTag>) {
        state = state.copy(post = state.post?.copy(tags = newTags))
    }

    // --- CTAs ---
    // Fixed 2 CTAs logic
    fun onCtaLabelChange(index: Int, label: String) {
        val currentCtas = state.post?.ctas?.toMutableList() ?: return
        if (index in currentCtas.indices) {
            val current = currentCtas[index]
            currentCtas[index] = current.copy(label = label)
            state = state.copy(post = state.post?.copy(ctas = currentCtas))
        }
    }

    fun onCtaUrlChange(index: Int, url: String) {
        val currentCtas = state.post?.ctas?.toMutableList() ?: return
        if (index in currentCtas.indices) {
            val current = currentCtas[index]
            currentCtas[index] = current.copy(url = url)
            state = state.copy(post = state.post?.copy(ctas = currentCtas))
        }
    }

    // --- IMAGE ---
    fun onImageRemoved() {
        state = state.copy(post = state.post?.copy(image = null))
    }

    fun onImageSelected(uri: Uri, context: Context) {
        viewModelScope.launch {
            state = state.copy(isSaving = true)
            val dealerId = state.dealerId ?: return@launch
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
                    _eventChannel.send(UiEvent.ShowSnackbar("Image upload failed: ${it.localizedMessage}"))
                }
        }
    }

    // --- SAVE ---
    fun onSave() {
        viewModelScope.launch {
            state = state.copy(isSaving = true)
            val dealerId = state.dealerId ?: return@launch
            val currentPost = state.post ?: return@launch

            if (currentPost.name.isNullOrBlank()) {
                _eventChannel.send(UiEvent.ShowSnackbar("Title cannot be empty."))
                state = state.copy(isSaving = false)
                return@launch
            }

            // Retrieve missing data
            val userEmail = sessionManager.getEmail() ?: "unknown"
            val currentTime = System.currentTimeMillis()

            // Domain Name should be available from initialization, but we didn't save it in state.
            // Let's rely on what we have, or re-fetch/store it.
            // Better to store it in state during init.
            // Assuming I'll add domainName to state in a separate edit or relying on it being there
            // if I added it to state (I didn't yet).
            // Wait, I need to add domainName to state first.
            // I will add domainName to State in the next step.
            // Implementation below assumes state.domainName exists.

            // Filter out empty CTAs
            val validCtas = currentPost.ctas?.filter { it.label.isNotBlank() || it.url.isNotBlank() } ?: emptyList()

            val contentPresent = !currentPost.content.isNullOrBlank()
            val imagePresent = currentPost.image != null
            val linksPresent = validCtas.isNotEmpty()

            val postToSave = currentPost.copy(
                ctas = validCtas,
                // Timestamps & User Info
                createdOn = if (currentPost.id == null) currentTime else currentPost.createdOn,
                updatedOn = currentTime,
                createdBy = if (currentPost.id == null) userEmail else currentPost.createdBy ?: userEmail,
                updatedBy = userEmail,
                domainName = state.domainName, // Need to add this to State

                // Flags
                haveContent = contentPresent,
                haveImage = imagePresent,
                haveLinks = linksPresent
            )

            val result = if (postToSave.id == null) {
                postRepository.addPost(dealerId, postToSave)
            } else {
                postRepository.updatePost(dealerId, postToSave.id, postToSave)
            }

            result.onSuccess {
                _eventChannel.send(UiEvent.NavigateBack)
            }.onFailure {
                _eventChannel.send(UiEvent.ShowSnackbar(it.message ?: "Error saving post."))
            }
            state = state.copy(isSaving = false)
        }
    }
}

sealed class UiEvent {
    object NavigateBack : UiEvent()
    data class ShowSnackbar(val message: String) : UiEvent()
}
