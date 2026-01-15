package com.slt.cardealership.presentation.websitedashboard.settings.applicationsettings.contactinfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.domain.model.ContactDomainItem
import com.slt.cardealership.domain.repo.DealerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContactInfoUiState(
    val contacts: List<ContactDomainItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddEditSheet: Boolean = false,
    val editingContact: ContactDomainItem? = null,
    val isSaving: Boolean = false
)

@HiltViewModel
class ContactInfoViewModel @Inject constructor(
    private val repository: DealerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactInfoUiState())
    val uiState: StateFlow<ContactInfoUiState> = _uiState.asStateFlow()

    fun fetchContacts(domainId: Int) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = repository.getDomainContacts(domainId)
            result.onSuccess { response ->
                _uiState.update { it.copy(isLoading = false, contacts = response.list) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
        }
    }

    fun deleteContact(contactId: Int, domainId: Int) {
        viewModelScope.launch {
            val result = repository.deleteDomainContact(contactId)
            result.onSuccess {
                fetchContacts(domainId)
            }.onFailure { error ->
                _uiState.update { it.copy(error = error.message) }
            }
        }
    }

    fun saveContact(domainId: Int, label: String, phone: String, email: String) {
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val result = if (_uiState.value.editingContact != null) {
                repository.updateDomainContact(_uiState.value.editingContact!!.id, domainId.toString(), label, phone, email)
            } else {
                repository.createDomainContact(domainId.toString(), label, phone, email)
            }

            result.onSuccess {
                _uiState.update { it.copy(isSaving = false, showAddEditSheet = false, editingContact = null) }
                fetchContacts(domainId)
            }.onFailure { error ->
                _uiState.update { it.copy(isSaving = false, error = error.message) }
            }
        }
    }

    fun onAddClick() {
        _uiState.update { it.copy(showAddEditSheet = true, editingContact = null) }
    }

    fun onEditClick(contact: ContactDomainItem) {
        _uiState.update { it.copy(showAddEditSheet = true, editingContact = contact) }
    }

    fun dismissSheet() {
        _uiState.update { it.copy(showAddEditSheet = false, editingContact = null) }
    }
}
