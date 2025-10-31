package com.slt.cardealership.presentation.seo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.data.local.SessionManager
import com.slt.cardealership.domain.model.SeoTag
import com.slt.cardealership.domain.repo.DealerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// --- 1. UI STATE DATA CLASSES ---

data class SeoUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val allTags: List<SeoTag> = emptyList(),
    val selectedTagIds: Set<String> = emptySet(), // FIX: ID is a String
    val error: String? = null
)

data class AddTagState(
    val isSaving: Boolean = false,
    val tagName: String = "",
    val tagUrl: String = "",
    val error: String? = null,
)

// --- 2. UI EVENTS ---

sealed interface SeoEvent {
    data class ShowSuccess(val message: String) : SeoEvent
    data class ShowError(val message: String) : SeoEvent
    object CloseAddTagDialog : SeoEvent
}

// --- 3. SEO VIEWMODEL ---

@HiltViewModel
class SeoViewModel @Inject constructor(
    private val repository: DealerRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val TAG = "SEO_DEBUG"

    private val _uiState = MutableStateFlow(SeoUiState())
    val uiState: StateFlow<SeoUiState> = _uiState.asStateFlow()

    private val _addTagState = MutableStateFlow(AddTagState())
    val addTagState: StateFlow<AddTagState> = _addTagState.asStateFlow()

    private val _events = Channel<SeoEvent>()
    val events = _events.receiveAsFlow()

    // --- MAIN SCREEN LOGIC ---

    fun loadSeoScreenData(domainId: Int) {
        Log.d(TAG, "--- loadSeoScreenData --- domainId: $domainId")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val dealerId = sessionManager.getDealerId()?.toLong()
            if (dealerId == null) {
                Log.d(TAG, "Error: Dealer ID is null.")
                _uiState.update { it.copy(isLoading = false, error = "User session error. Could not get Dealer ID.") }
                return@launch
            }

            Log.d(TAG, "Fetching all tags and mapped tags in parallel for dealerId: $dealerId...")

            // FIX: Pass dealerId to getSeoTags
            val allTagsDeferred = async { repository.getSeoTags(dealerId) }
            val mappedTagsDeferred = async { repository.getMappedSeoTags(dealerId, domainId) }

            val allTagsResult = allTagsDeferred.await()
            val mappedTagsResult = mappedTagsDeferred.await()

            Log.d(TAG, "Fetched allTags: ${allTagsResult.getOrNull()}")
            Log.d(TAG, "Fetched mappedTags: ${mappedTagsResult.getOrNull()}")

            if (allTagsResult.isSuccess && mappedTagsResult.isSuccess) {
                val allTags = allTagsResult.getOrThrow()
                val mappedTags = mappedTagsResult.getOrThrow()

                Log.d(TAG, "Success. All tags count: ${allTags.size}, Mapped tags count: ${mappedTags.size}")
                val selectedIds = mappedTags.map { tag -> tag.id }.toSet() // id is now a String
                Log.d(TAG, "Setting selected IDs: $selectedIds")

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        allTags = allTags,
                        selectedTagIds = selectedIds
                    )
                }
            } else {
                val error = allTagsResult.exceptionOrNull()?.message
                    ?: mappedTagsResult.exceptionOrNull()?.message
                    ?: "Failed to load SEO data."
                Log.d(TAG, "Failure: $error")
                _uiState.update { it.copy(isLoading = false, error = error) }
                _events.send(SeoEvent.ShowError(error))
            }
        }
    }

    fun onTagSelected(tagId: String, isSelected: Boolean) { // FIX: ID is a String
        Log.d(TAG, "onTagSelected: tagId=$tagId, isSelected=$isSelected")
        _uiState.update { currentState ->
            val newSelectedIds = currentState.selectedTagIds.toMutableSet()
            if (isSelected) {
                newSelectedIds.add(tagId)
            } else {
                newSelectedIds.remove(tagId)
            }
            Log.d(TAG, "New selectedTagIds: $newSelectedIds")
            currentState.copy(selectedTagIds = newSelectedIds)
        }
    }

    fun saveMappedTags(domainId: Int) {
        Log.d(TAG, "--- saveMappedTags --- domainId: $domainId")
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            val dealerId = sessionManager.getDealerId()?.toLong()
            if (dealerId == null) {
                Log.d(TAG, "Error: Dealer ID is null.")
                _uiState.update { it.copy(isSaving = false, error = "User session error.") }
                _events.send(SeoEvent.ShowError("User session error."))
                return@launch
            }

            val tagIdsToSave = _uiState.value.selectedTagIds.toList() // This is now List<String>
            Log.d(TAG, "Saving tag IDs: $tagIdsToSave")

            repository.mapSeoTagsToDomain(dealerId, domainId, tagIdsToSave)
                .onSuccess {
                    Log.d(TAG, "Save successful.")
                    _uiState.update { it.copy(isSaving = false) }
                    _events.send(SeoEvent.ShowSuccess("SEO tags updated successfully."))
                }
                .onFailure { error ->
                    Log.d(TAG, "Save failed: ${error.message}")
                    _uiState.update { it.copy(isSaving = false, error = error.message) }
                    _events.send(SeoEvent.ShowError(error.message ?: "Failed to save tags."))
                }
        }
    }

    // --- ADD/DELETE MASTER TAGS LOGIC ---


    fun addNewTag() {
        Log.d(TAG, "--- addNewTag ---")
        viewModelScope.launch {
            val dealerId = sessionManager.getDealerId()?.toLong() // <-- FIX: Get dealerId
            if (dealerId == null) {
                Log.d(TAG, "Error: Dealer ID is null.")
                _addTagState.update { it.copy(error = "User session error.")}
                return@launch
            }

            val state = _addTagState.value
            // FIX: Validate both tagName and tagUrl
            if (state.tagName.isBlank() || state.tagUrl.isBlank()) {
                Log.d(TAG, "Validation failed: Tag name or URL is blank.")
                _addTagState.update { it.copy(error = "Tag name and URL cannot be blank.") }
                return@launch
            }

            _addTagState.update { it.copy(isSaving = true, error = null) }
            Log.d(TAG, "Saving new tag: ${state.tagName}")

            // FIX: Pass dealerId, tagName, and tagUrl
            repository.addSeoTag(dealerId, state.tagName, state.tagUrl)
                .onSuccess {
                    Log.d(TAG, "Successfully added new tag. Refreshing list...")
                    _addTagState.value = AddTagState() // Clear the dialog form
                    _events.send(SeoEvent.ShowSuccess("Tag created successfully."))
                    _events.send(SeoEvent.CloseAddTagDialog) // Tell UI to close dialog
                    refreshAllTagsList() // Refresh the master list on the main screen
                }
                .onFailure { error ->
                    Log.d(TAG, "Failed to add tag: ${error.message}")
                    _addTagState.update { it.copy(isSaving = false, error = error.message) }
                }
        }
    }

    fun deleteTag(tagId: String) { // FIX: ID is a String
        Log.d(TAG, "--- deleteTag --- tagId: $tagId")
        viewModelScope.launch {
            _uiState.update {
                it.copy(allTags = it.allTags.filterNot { tag -> tag.id == tagId })
            }
            Log.d(TAG, "Optimistically removing tag $tagId from UI.")

            repository.deleteSeoTag(tagId)
                .onSuccess {
                    Log.d(TAG, "Delete successful for tag $tagId.")
                    _events.send(SeoEvent.ShowSuccess("Tag deleted successfully."))
                }
                .onFailure { error ->
                    Log.d(TAG, "Delete failed for tag $tagId. Refreshing list. Error: ${error.message}")
                    _events.send(SeoEvent.ShowError(error.message ?: "Failed to delete tag."))
                    refreshAllTagsList()
                }
        }
    }

    private fun refreshAllTagsList() {
        Log.d(TAG, "--- refreshAllTagsList ---")
        viewModelScope.launch {
            val dealerId = sessionManager.getDealerId()?.toLong() // <-- FIX: Need dealerId
            if (dealerId == null) {
                Log.d(TAG, "Refresh failed: Dealer ID is null.")
                _uiState.update { it.copy(isLoading = false, error = "Session error") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }
            repository.getSeoTags(dealerId) // <-- FIX: Pass dealerId
                .onSuccess { allTags ->
                    Log.d(TAG, "Refresh successful. Got ${allTags.size} tags.")
                    _uiState.update { it.copy(isLoading = false, allTags = allTags) }
                }
                .onFailure { error ->
                    Log.d(TAG, "Refresh failed: ${error.message}")
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }








    fun onAddTagStateChange(newState: AddTagState) {
        Log.d(TAG, "onAddTagStateChange: $newState")
        _addTagState.value = newState
    }

    /**
     * Resets the "Add/Edit" state, called when the screen is left.
     */
    fun clearAddTagState() {
        Log.d(TAG, "clearAddTagState")
        _addTagState.value = AddTagState()
    }

    /**
     * NEW FUNCTION: Called from SeoScreen when the edit icon is clicked.
     * This populates the AddTagState before navigating to AddSeoScreen.
     */
    fun loadTagForEdit(tag: SeoTag) {
        Log.d(TAG, "--- loadTagForEdit --- tag: $tag")
        _addTagState.value = AddTagState(
            tagName = tag.tagName,
            tagUrl = tag.tagUrl
        )
    }

    /**
     * RENAMED: Was addNewTag(). Now handles both create and update.
     */
//    fun saveTag() {
//        Log.d(TAG, "--- saveTag ---")
//        viewModelScope.launch {
//            val dealerId = sessionManager.getDealerId()?.toLong()
//            if (dealerId == null) {
//                Log.d(TAG, "Error: Dealer ID is null.")
//                _addTagState.update { it.copy(error = "User session error.")}
//                return@launch
//            }
//
//            val state = _addTagState.value
//            if (state.tagName.isBlank() || state.tagUrl.isBlank()) {
//                Log.d(TAG, "Validation failed: Tag name or URL is blank.")
//                _addTagState.update { it.copy(error = "Tag name and URL cannot be blank.") }
//                return@launch
//            }
//
//            _addTagState.update { it.copy(isSaving = true, error = null) }
//
//            // --- CHOOSE WHICH REPO FUNCTION TO CALL ---
//            val result = if (state.isEditMode) {
//                Log.d(TAG, "Saving (Update) tag: ${state.tagName}")
//                repository.updateSeoTag(
//                    tagId = state.tagIdToEdit!!,
//                    dealerId = dealerId,
//                    tagName = state.tagName,
//                    tagUrl = state.tagUrl
//                )
//            } else {
//                Log.d(TAG, "Saving (Add) tag: ${state.tagName}")
//                repository.addSeoTag(
//                    dealerId = dealerId,
//                    tagName = state.tagName,
//                    tagUrl = state.tagUrl
//                )
//            }
//
//            // --- HANDLE THE RESULT ---
//            result.onSuccess {
//                Log.d(TAG, "Successfully saved tag. Refreshing list...")
//                _addTagState.value = AddTagState() // Clear the form
//                _events.send(SeoEvent.ShowSuccess(if (state.isEditMode) "Tag updated successfully." else "Tag created successfully."))
//                _events.send(SeoEvent.CloseAddTagDialog) // This will trigger navigation back
//                refreshAllTagsList() // Refresh the master list
//            }
//                .onFailure { error ->
//                    Log.d(TAG, "Failed to save tag: ${error.message}")
//                    _addTagState.update { it.copy(isSaving = false, error = error.message) }
//                }
//        }
//    }


}