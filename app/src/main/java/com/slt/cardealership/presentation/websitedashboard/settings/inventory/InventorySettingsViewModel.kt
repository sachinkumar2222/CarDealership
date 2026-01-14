package com.slt.cardealership.presentation.websitedashboard.settings.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.domain.model.*
import com.slt.cardealership.domain.repo.DealerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InventorySettingsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val inventorySettings: DomainInventorySetting? = null,
    val allMakes: List<Make> = emptyList(),
    val allBodyTypes: List<BodyType> = emptyList(),
    val defaultMakes: List<DomainMakeSetting> = emptyList(),
    val newMakes: List<DomainMakeSetting> = emptyList(),
    val usedMakes: List<DomainMakeSetting> = emptyList(),
    val newBodyTypes: List<DomainBodyTypeSetting> = emptyList()
)

@HiltViewModel
class InventorySettingsViewModel @Inject constructor(
    private val repository: DealerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventorySettingsUiState())
    val uiState: StateFlow<InventorySettingsUiState> = _uiState.asStateFlow()

    fun loadData(domainId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Fetch all required data
            val settingsResult = repository.getDomainViSetting(domainId)
            val makesResult = repository.getAllMakes()
            val bodyTypesResult = repository.getAllBodyTypes()

            // Fetch saved makes configuration
            val defaultMakesResult = repository.getDomainSettingMakes(domainId, "inventory", "default")
            val newMakesResult = repository.getDomainSettingMakes(domainId, "inventory", "new")
            val usedMakesResult = repository.getDomainSettingMakes(domainId, "inventory", "used")
            val newBodyTypesResult = repository.getDomainSettingBodyTypes(domainId, "inventory")

            val allMakes = makesResult.getOrNull() ?: emptyList()
            val allBodyTypes = bodyTypesResult.getOrNull() ?: emptyList()

            fun enrichMakes(makes: List<DomainMakeSetting>?): List<DomainMakeSetting> {
                return makes?.map { makeSetting ->
                    if (makeSetting.makeName.isNullOrEmpty()) {
                        val matchingMake = allMakes.find { it.id == makeSetting.makeId }
                        makeSetting.copy(makeName = matchingMake?.name ?: "Unknown")
                    } else {
                        makeSetting
                    }
                } ?: emptyList()
            }

            // Helper function to enrich body types with names
            fun enrichBodyTypes(bodyTypes: List<DomainBodyTypeSetting>?): List<DomainBodyTypeSetting> {
                return bodyTypes?.map { bodyTypeSetting ->
                    if (bodyTypeSetting.bodyTypeName.isNullOrEmpty() || bodyTypeSetting.bodyTypeName == "Unknown") {
                        val matchingBodyType = allBodyTypes.find { it.id == bodyTypeSetting.bodyTypeId }
                        bodyTypeSetting.copy(bodyTypeName = matchingBodyType?.name ?: "Unknown")
                    } else {
                        bodyTypeSetting
                    }
                } ?: emptyList()
            }

            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    inventorySettings = settingsResult.getOrNull(),
                    allMakes = allMakes,
                    allBodyTypes = allBodyTypes,
                    defaultMakes = enrichMakes(defaultMakesResult.getOrNull()),
                    newMakes = enrichMakes(newMakesResult.getOrNull()),
                    usedMakes = enrichMakes(usedMakesResult.getOrNull()),
                    newBodyTypes = enrichBodyTypes(newBodyTypesResult.getOrNull())
                )
            }
        }
    }

    fun updateInventoryView(view: String) {
        val currentSettings = _uiState.value.inventorySettings ?: return
        _uiState.update { it.copy(inventorySettings = currentSettings.copy(defaultInventoryView = view)) }
    }

    fun updateDefaultSort(sortLabel: String) {
        val currentSettings = _uiState.value.inventorySettings ?: return
        val sortValue = mapSortLabelToValue(sortLabel)
        _uiState.update { it.copy(inventorySettings = currentSettings.copy(defaultOrder = sortValue)) }
    }

    // Logic for adding/removing Makes to specific lists (Default, New, Used)
    fun addMakeToCondition(make: Make, condition: String) {
        val newEntry = DomainMakeSetting(
            makeId = make.id,
            vehicleModule = "inventory",
            condition = condition,
            makeName = make.name
        )

        _uiState.update { state ->
            when(condition) {
                "default" -> state.copy(defaultMakes = state.defaultMakes + newEntry)
                "new" -> state.copy(newMakes = state.newMakes + newEntry)
                "used" -> state.copy(usedMakes = state.usedMakes + newEntry)
                else -> state
            }
        }
    }

    fun removeMakeFromCondition(request: DomainMakeSetting) {
        _uiState.update { state ->
            when(request.condition) {
                "default" -> state.copy(defaultMakes = state.defaultMakes.filter { it.makeId != request.makeId })
                "new" -> state.copy(newMakes = state.newMakes.filter { it.makeId != request.makeId })
                "used" -> state.copy(usedMakes = state.usedMakes.filter { it.makeId != request.makeId })
                else -> state
            }
        }
    }

    // Logic for adding/removing Body Types
    fun addBodyType(bodyType: BodyType) {
        val newEntry = DomainBodyTypeSetting(
            bodyTypeId = bodyType.id,
            vehicleModule = "inventory",
            bodyTypeName = bodyType.name
        )
        _uiState.update { state ->
            state.copy(newBodyTypes = state.newBodyTypes + newEntry)
        }
    }

    fun removeBodyType(request: DomainBodyTypeSetting) {
        _uiState.update { state ->
            state.copy(newBodyTypes = state.newBodyTypes.filter { it.bodyTypeId != request.bodyTypeId })
        }
    }

    fun saveSettings(domainId: Int) {
        val settings = _uiState.value.inventorySettings ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Map Sort Value
            val sortValue = mapSortLabelToValue(settings.defaultOrder ?: "Price : Low To High")
            repository.saveDomainViSetting(settings.copy(defaultOrder = sortValue))

            // Save Makes - Separate calls per condition
            val defaultMakeIds = _uiState.value.defaultMakes.map { it.makeId }
            repository.saveDomainSettingMakes(domainId, "inventory", "default", defaultMakeIds)

            val newMakeIds = _uiState.value.newMakes.map { it.makeId }
            repository.saveDomainSettingMakes(domainId, "inventory", "new", newMakeIds)

            val usedMakeIds = _uiState.value.usedMakes.map { it.makeId }
            repository.saveDomainSettingMakes(domainId, "inventory", "used", usedMakeIds)

            val bodyTypeIds = _uiState.value.newBodyTypes.map { it.bodyTypeId }
            repository.saveDomainSettingBodyTypes(domainId, "inventory", bodyTypeIds)

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    // Sort Options Mapping
    val sortOptions = listOf(
        "Price : Low To High" to "price_asc",
        "Price : High To Low" to "price_desc",
        "Year : New To Old" to "year_desc",
        "Year : Old To New" to "year_asc",
        "Distance : Nearest To Farthest" to "distance_asc",
        "Listed : Newly Listed To Old Listed" to "listed_new_old",
        "Listed : Old Listed To Newly Listed" to "listed_old_new",
        "Mileage : High To Low" to "mileage_desc",
        "Mileage : Low To High" to "mileage_asc",
        "Make : A To Z" to "make_asc",
        "Make : Z To A" to "make_desc",
        "Model : A To Z" to "model_asc",
        "Model : Z To A" to "model_desc"
    )

    fun getSortLabel(value: String?): String {
        return sortOptions.find { it.second == value }?.first ?: ""
    }

    private fun mapSortLabelToValue(label: String): String {
        return sortOptions.find { it.first == label }?.second ?: label
    }
}
