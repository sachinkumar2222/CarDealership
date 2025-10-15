package com.slt.cardealership.presentation.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.domain.model.Vehicle
import com.slt.cardealership.domain.repo.DealerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InventoryUiState(
    val vehicles: List<Vehicle> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val dealerRepository: DealerRepository
    // Add SessionManager if you need the dealerId
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Mock data for UI development. Replace with actual API call.
        loadMockVehicles()
    }

    private fun fetchVehicles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // val dealerId = sessionManager.getDealerSlug()?.toLongOrNull() ?: return@launch
            // For now, let's assume a hardcoded dealer ID for the API call
            val dealerId = 31181L

            dealerRepository.getVehicles(dealerId)
                .onSuccess { vehicles ->
                    _uiState.update { it.copy(isLoading = false, vehicles = vehicles) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    private fun loadMockVehicles() {
        _uiState.update { it.copy(isLoading = true) }
        val mockVehicles = listOf(
            Vehicle(id = "1", vin = "1FTDE1CF5GKA12345", year = 2023, make = "Ford", model = "F-150", trim = "Lariat", mileage = 15000, price = 55000.00, stockNumber = "P1234", previewImageUrl = "https://placehold.co/400x300/CCCCCC/31343C?text=Ford+F-150"),
            Vehicle(id = "2", vin = "JN1AZ0BV3LM123456", year = 2022, make = "Nissan", model = "Altima", trim = "SV", mileage = 32000, price = 28000.00, stockNumber = "N5678", previewImageUrl = "https://placehold.co/400x300/BDBDBD/31343C?text=Nissan+Altima"),
            Vehicle(id = "3", vin = "5YJSA1E2XLF123456", year = 2021, make = "Tesla", model = "Model 3", trim = "Long Range", mileage = 45000, price = 42000.00, stockNumber = "T9101", previewImageUrl = "https://placehold.co/400x300/E0E0E0/31343C?text=Tesla+Model+3")
        )
        _uiState.update { it.copy(isLoading = false, vehicles = mockVehicles) }
    }
}
