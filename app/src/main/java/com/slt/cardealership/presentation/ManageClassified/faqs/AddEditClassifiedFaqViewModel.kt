package com.slt.cardealership.presentation.ManageClassified.faqs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.data.local.SessionManager
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

data class AddEditClassifiedFaqState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isEditMode: Boolean = false,
    val faqId: Int? = null,
    val question: String = "",
    val answer: String = "",
    val error: String? = null,
    val createdBy: Int? = null,
    val createdOn: Long? = null
)

@HiltViewModel
class AddEditClassifiedFaqViewModel @Inject constructor(
    private val repository: DealerRepository,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val siteId: String? = savedStateHandle["siteId"]
    private val faqIdArg: String? = savedStateHandle["faqId"]

    private val _state = MutableStateFlow(AddEditClassifiedFaqState())
    val state: StateFlow<AddEditClassifiedFaqState> = _state.asStateFlow()

    private val _events = Channel<UiEvent>()
    val events = _events.receiveAsFlow()

    sealed interface UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent
        object NavigateBack : UiEvent
    }

    private var dealerId: Long? = null
    private var domainId: Int? = null

    init {
        initialize()
    }

    private fun initialize() {
        val id = siteId?.toIntOrNull()
        if (id == null) {
            _state.update { it.copy(error = "Invalid Site ID") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val domainResult = repository.getDomainDetails(id)

            domainResult.onSuccess { domainItem ->
                domainId = id
                dealerId = domainItem.dealerId?.toLong() ?: sessionManager.getDealerId()?.toLong()

                if (faqIdArg != null) {
                    loadFaq(faqIdArg.toInt())
                } else {
                    _state.update { it.copy(isLoading = false, isEditMode = false) }
                }
            }.onFailure {
                _state.update { it.copy(isLoading = false, error = "Failed to load domain info") }
            }
        }
    }

    private suspend fun loadFaq(id: Int) {
        repository.getFaqDetails(id)
            .onSuccess { details ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        isEditMode = true,
                        faqId = details.id,
                        question = details.question,
                        answer = details.answer,
                        createdBy = details.created_by,
                        createdOn = details.created_on
                    )
                }
            }
            .onFailure { error ->
                _state.update { it.copy(isLoading = false, error = "Failed to load FAQ: ${error.message}") }
            }
    }

    fun onQuestionChange(newValue: String) {
        _state.update { it.copy(question = newValue) }
    }

    fun onAnswerChange(newValue: String) {
        _state.update { it.copy(answer = newValue) }
    }

    fun saveFaq() {
        if (dealerId == null || domainId == null) {
            _state.update { it.copy(error = "Initialization failed") }
            return
        }
        val currentState = _state.value
        if (currentState.question.isBlank() || currentState.answer.isBlank()) {
            _state.update { it.copy(error = "Question and Answer are required") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val userId = 630 // Hardcoded or fetch from session if available as Int
            val currentTime = System.currentTimeMillis() / 1000L

            val request = FaqRequest(
                dealer_id = dealerId!!.toInt(),
                domain_id = domainId!!,
                question = currentState.question,
                answer = currentState.answer,
                created_by = currentState.createdBy ?: userId,
                updated_by = userId,
                created_on = currentState.createdOn ?: currentTime,
                updated_on = currentTime
            )

            val result = if (currentState.isEditMode) {
                repository.updateFaq(currentState.faqId!!, request)
            } else {
                repository.addFaq(request)
            }

            result.onSuccess {
                _events.send(UiEvent.ShowSnackbar("FAQ Saved Successfully"))
                _events.send(UiEvent.NavigateBack)
            }.onFailure { error ->
                _state.update { it.copy(isSaving = false, error = error.message) }
            }
        }
    }
}
