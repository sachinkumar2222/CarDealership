package com.slt.cardealership.presentation.services

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.data.local.SessionManager
import com.slt.cardealership.domain.model.DealerService
import com.slt.cardealership.domain.model.DealerServicePayload
import com.slt.cardealership.domain.model.DealerServicesRequest
import com.slt.cardealership.domain.model.ProductType
import com.slt.cardealership.domain.repo.DealerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ServiceUiItem(
    val id: Int,
    val name: String,
    val canBeEnabled: Boolean,
    val supportsLeadForms: Boolean,
    var isEnabled: Boolean,
    var leadFormEnabled: Boolean,
    var primaryEmail: String,
    var primaryAdfEmail: String,

    // --- NEW FIELDS ---
    var primaryPhone: String,
    var leadEmails: List<String>,
    var adfLeadEmails: List<String>,
    var leadPhones: List<String>
)

// ... (ServicesUiState and ServicesEvent remain the same) ...
data class ServicesUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val serviceItems: List<ServiceUiItem> = emptyList()
)

sealed interface ServicesEvent {
    data class ShowToast(val message: String) : ServicesEvent
    object SaveSuccessAndNavBack : ServicesEvent
}

@HiltViewModel
class ServicesViewModel @Inject constructor(
    private val repository: DealerRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServicesUiState())
    val uiState: StateFlow<ServicesUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<ServicesEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var dealerId: Int? = null

    private val servicesNameWhitelist = setOf(
        "Build And Price", "Sales", "Finance", "Service", "Cash For Cars"
    )

    init {
        viewModelScope.launch {
            dealerId = sessionManager.getDealerId()
            if (dealerId == null) {
                _uiState.value = ServicesUiState(error = "Dealer ID not found.")
                return@launch
            }
            loadServices(dealerId!!)
        }
    }

    fun loadServices(dealerId: Int) {
        _uiState.value = ServicesUiState(isLoading = true)
        viewModelScope.launch {
            try {
                val (allProductsResult, dealerServicesResult) = coroutineScope {
                    val allProducts = async { repository.getAllProductTypes() }
                    val dealerServices = async { repository.getDealerServices(dealerId) }
                    Pair(allProducts.await(), dealerServices.await())
                }

                val allProducts = allProductsResult.getOrThrow()
                val dealerServices = dealerServicesResult.getOrThrow()
                val dealerServiceMap = dealerServices.associateBy { it.productTypeId }

                val filteredProducts = allProducts.filter { it.name in servicesNameWhitelist }

                val serviceUiItems = filteredProducts.map { product ->
                    val dealerSetting = dealerServiceMap[product.id]

                    ServiceUiItem(
                        id = product.id,
                        name = product.name ?: "Unnamed Service",
                        canBeEnabled = product.enableAsService,
                        supportsLeadForms = product.enableLeadForms,
                        isEnabled = dealerSetting != null,
                        leadFormEnabled = dealerSetting?.enableLeadForm ?: false,
                        primaryEmail = dealerSetting?.primaryEmail ?: "",
                        primaryAdfEmail = dealerSetting?.primaryAdfEmail ?: "",

                        // --- POPULATE NEW FIELDS ---
                        primaryPhone = dealerSetting?.primaryPhone ?: "",
                        leadEmails = dealerSetting?.emails ?: emptyList(),
                        adfLeadEmails = dealerSetting?.adfEmails ?: emptyList(),
                        leadPhones = dealerSetting?.phones ?: emptyList()
                    )
                }

                _uiState.value = ServicesUiState(isLoading = false, serviceItems = serviceUiItems)

            } catch (e: Exception) {
                Log.e("ServicesViewModel", "Failed to load services", e)
                _uiState.value = ServicesUiState(error = e.message ?: "Failed to load services")
            }
        }
    }

    fun onServiceToggled(serviceId: Int, isNowEnabled: Boolean) {
        updateItem(serviceId) { it.copy(isEnabled = isNowEnabled) }
    }

    // --- UPDATED: Now accepts primaryPhone ---
    fun onPrimarySettingChanged(serviceId: Int, newEmail: String, newAdfEmail: String, newPhone: String) {
        updateItem(serviceId) {
            it.copy(primaryEmail = newEmail, primaryAdfEmail = newAdfEmail, primaryPhone = newPhone)
        }
    }

    fun onLeadFormToggled(serviceId: Int, isNowEnabled: Boolean) {
        updateItem(serviceId) { it.copy(leadFormEnabled = isNowEnabled) }
    }

    // --- NEW FUNCTIONS FOR LISTS ---

    fun addLeadEmail(serviceId: Int) {
        updateItem(serviceId) { it.copy(leadEmails = it.leadEmails + "") }
    }
    fun updateLeadEmail(serviceId: Int, index: Int, text: String) {
        updateItem(serviceId) {
            val list = it.leadEmails.toMutableList().apply { set(index, text) }
            it.copy(leadEmails = list)
        }
    }
    fun deleteLeadEmail(serviceId: Int, index: Int) {
        updateItem(serviceId) {
            val list = it.leadEmails.toMutableList().apply { removeAt(index) }
            it.copy(leadEmails = list)
        }
    }

    fun addAdfEmail(serviceId: Int) {
        updateItem(serviceId) { it.copy(adfLeadEmails = it.adfLeadEmails + "") }
    }
    fun updateAdfEmail(serviceId: Int, index: Int, text: String) {
        updateItem(serviceId) {
            val list = it.adfLeadEmails.toMutableList().apply { set(index, text) }
            it.copy(adfLeadEmails = list)
        }
    }
    fun deleteAdfEmail(serviceId: Int, index: Int) {
        updateItem(serviceId) {
            val list = it.adfLeadEmails.toMutableList().apply { removeAt(index) }
            it.copy(adfLeadEmails = list)
        }
    }

    fun addLeadPhone(serviceId: Int) {
        updateItem(serviceId) { it.copy(leadPhones = it.leadPhones + "") }
    }
    fun updateLeadPhone(serviceId: Int, index: Int, text: String) {
        updateItem(serviceId) {
            val list = it.leadPhones.toMutableList().apply { set(index, text) }
            it.copy(leadPhones = list)
        }
    }
    fun deleteLeadPhone(serviceId: Int, index: Int) {
        updateItem(serviceId) {
            val list = it.leadPhones.toMutableList().apply { removeAt(index) }
            it.copy(leadPhones = list)
        }
    }

    // Helper to update a single item in the list
    private fun updateItem(serviceId: Int, update: (ServiceUiItem) -> ServiceUiItem) {
        val updatedList = _uiState.value.serviceItems.map {
            if (it.id == serviceId) update(it) else it
        }
        _uiState.value = _uiState.value.copy(serviceItems = updatedList)
    }

    fun saveServices() {
        val currentDealerId = dealerId ?: return
        _uiState.value = _uiState.value.copy(isSaving = true)

        viewModelScope.launch {
            try {
                val enabledServices = _uiState.value.serviceItems.filter { it.isEnabled }

                val payloadList = enabledServices.map { item ->
                    DealerServicePayload(
                        productTypeId = item.id,
                        // Filter out empty strings from lists before saving
                        emails = item.leadEmails.filter { it.isNotBlank() },
                        adfEmails = item.adfLeadEmails.filter { it.isNotBlank() },
                        phones = item.leadPhones.filter { it.isNotBlank() },

                        enableLeadForm = if (item.leadFormEnabled) "Yes" else "No",
                        primaryEmail = item.primaryEmail,
                        primaryAdfEmail = item.primaryAdfEmail,
                        primaryPhone = item.primaryPhone // <-- INCLUDE PRIMARY PHONE
                    )
                }

                val request = DealerServicesRequest(dealerId = currentDealerId, list = payloadList)
                repository.saveDealerServices(currentDealerId, request).getOrThrow()

                _uiState.value = _uiState.value.copy(isSaving = false)
                _eventFlow.emit(ServicesEvent.ShowToast("Services saved successfully!"))
                _eventFlow.emit(ServicesEvent.SaveSuccessAndNavBack)

            } catch (e: Exception) {
                Log.e("ServicesViewModel", "Failed to save services", e)
                _uiState.value = _uiState.value.copy(isSaving = false)
                _eventFlow.emit(ServicesEvent.ShowToast("Error: ${e.message}"))
            }
        }
    }
}