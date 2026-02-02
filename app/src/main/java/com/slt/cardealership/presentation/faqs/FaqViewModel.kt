package com.slt.cardealership.presentation.faq // Or your correct presentation package

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.data.local.SessionManager
import com.slt.cardealership.domain.model.FaqItem
import com.slt.cardealership.domain.model.FaqRequest
import com.slt.cardealership.domain.repo.DealerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// --- 1. UI STATE DATA CLASSES ---

data class FaqListState(
    val isLoading: Boolean = false,
    val faqs: List<FaqItem> = emptyList(),
    val error: String? = null
)

data class FaqFormState(
    val isLoading: Boolean = false, // For loading details
    val isSaving: Boolean = false,
    val isEditMode: Boolean = false,
    val faqId: Int? = null,
    val question: String = "",
    val answer: String = "",
    val formError: String? = null,

    // We need to store these when editing
    val createdBy: Int? = null,
    val createdOn: Long? = null
)

// --- 2. UI EVENTS ---

sealed interface FaqEvent {
    data class ShowSuccess(val message: String) : FaqEvent
    data class ShowError(val message: String) : FaqEvent
    object NavigateBack : FaqEvent // To go back after saving
}


// --- 3. FAQ VIEWMODEL ---

@HiltViewModel
class FaqViewModel @Inject constructor(
    private val repository: DealerRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val TAG = "FAQ_DEBUG"

    // State for the list screen
    private val _listState = MutableStateFlow(FaqListState())
    val listState: StateFlow<FaqListState> = _listState.asStateFlow()

    // State for the add/edit screen
    private val _formState = MutableStateFlow(FaqFormState())
    val formState: StateFlow<FaqFormState> = _formState.asStateFlow()

    // Channel for one-time events (toasts, navigation)
    private val _events = Channel<FaqEvent>()
    val events = _events.receiveAsFlow()

    // --- LIST SCREEN LOGIC ---

    /**
     * Fetches the list of FAQs for the current dealer.
     * Uses domain_id = 0 based on your API log.
     */
    fun getFaqs() {
        Log.d(TAG, "--- getFaqs ---")
        viewModelScope.launch {
            _listState.update { it.copy(isLoading = true, error = null) }

            val dealerId = sessionManager.getDealerId()?.toLong()
            if (dealerId == null) {
                Log.d(TAG, "Error: Dealer ID is null.")
                _listState.update { it.copy(isLoading = false, error = "User session error.") }
                return@launch
            }

            // Using domainId = 0 as seen in your GET request
            repository.getFaqs(dealerId, domainId = 0)
                .onSuccess { faqs ->
                    Log.d(TAG, "Success: Fetched ${faqs.size} FAQs.")
                    _listState.update { it.copy(isLoading = false, faqs = faqs) }
                }
                .onFailure { error ->
                    Log.d(TAG, "Failure: ${error.message}")
                    _listState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    /**
     * Deletes an FAQ.
     * @param type - Your API requires a 'type' query param. You need to find out what this is.
     * I will hardcode "default" for now.
     */
    fun deleteFaq(faqId: Int, type: String = "default") {
        Log.d(TAG, "--- deleteFaq --- faqId: $faqId, type: $type")
        viewModelScope.launch {
            repository.deleteFaq(faqId, type)
                .onSuccess {
                    Log.d(TAG, "Delete successful. Refreshing list.")
                    _events.send(FaqEvent.ShowSuccess("FAQ deleted successfully."))
                    getFaqs() // Refresh the list
                }
                .onFailure { error ->
                    Log.d(TAG, "Delete failed: ${error.message}")
                    _events.send(FaqEvent.ShowError(error.message ?: "Failed to delete FAQ."))
                }
        }
    }

    // --- ADD/EDIT SCREEN LOGIC ---

    /**
     * Loads a single FAQ's details into the form state for editing.
     */
    fun loadFaqForEdit(faqId: Int) {
        Log.d(TAG, "--- loadFaqForEdit --- faqId: $faqId")
        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true, formError = null) }
            repository.getFaqDetails(faqId)
                .onSuccess { details ->
                    Log.d(TAG, "Success: Loaded FAQ details.")
                    _formState.value = FaqFormState(
                        isLoading = false,
                        isEditMode = true,
                        faqId = details.id,
                        question = details.question,
                        answer = details.answer,
                        createdBy = details.created_by,
                        createdOn = details.created_on
                    )
                }
                .onFailure { error ->
                    Log.d(TAG, "Failure: ${error.message}")
                    _formState.update { it.copy(isLoading = false, formError = error.message) }
                }
        }
    }

    /**
     * Prepares the form state for creating a new FAQ.
     */
    fun prepareNewFaqForm() {
        Log.d(TAG, "--- prepareNewFaqForm ---")
        _formState.value = FaqFormState()
    }

    /**
     * Called by the UI to update the form state (e.g., on text change).
     */
    fun onFormStateChange(newState: FaqFormState) {
        _formState.value = newState
    }

    /**
     * Saves the FAQ (handles both creating a new one and updating an existing one).
     */
    fun saveFaq() {
        Log.d(TAG, "--- saveFaq ---")
        viewModelScope.launch {
            val state = _formState.value
            if (state.question.isBlank() || state.answer.isBlank()) {
                _formState.update { it.copy(formError = "Question and Answer cannot be blank.") }
                return@launch
            }

            _formState.update { it.copy(isSaving = true, formError = null) }

            val dealerId = sessionManager.getDealerId()?.toInt()
            // Your POST log shows created_by as 630 (Int)
            val userId = 630 // Hardcoded based on your API logs

            if (dealerId == null) {
                _formState.update { it.copy(isSaving = false, formError = "User session error.") }
                return@launch
            }

            val currentTime = System.currentTimeMillis() / 1000L

            // Build the request object based on your POST/PUT logs
            val faqRequest = FaqRequest(
                dealer_id = dealerId,
                domain_id = -1, // Hardcoded based on your POST log
                question = state.question,
                answer = state.answer,
                // Use existing values if editing, new values if creating
                created_by = state.createdBy ?: userId,
                updated_by = userId,
                created_on = state.createdOn ?: currentTime,
                updated_on = currentTime
            )

            Log.d(TAG, "Saving FAQ: $faqRequest")

            val result = if (state.isEditMode) {
                Log.d(TAG, "Calling updateFaq for ID: ${state.faqId!!}")
                repository.updateFaq(state.faqId!!, faqRequest)
            } else {
                Log.d(TAG, "Calling addFaq")
                repository.addFaq(faqRequest)
            }

            result.onSuccess {
                Log.d(TAG, "Save successful.")
                _formState.update { it.copy(isSaving = false) }
                getFaqs() // Refresh list immediately
                _events.send(FaqEvent.ShowSuccess(if (state.isEditMode) "FAQ updated" else "FAQ added"))
                _events.send(FaqEvent.NavigateBack)
            }
                .onFailure { error ->
                    Log.d(TAG, "Save failed: ${error.message}")
                    _formState.update { it.copy(isSaving = false, formError = error.message) }
                    _events.send(FaqEvent.ShowError(error.message ?: "Failed to save FAQ."))
                }
        }
    }
}