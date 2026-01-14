package com.slt.cardealership.presentation.websitedashboard.settings.buildandprice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.domain.model.BodyType
import com.slt.cardealership.domain.model.DomainBodyTypeSetting
import com.slt.cardealership.domain.model.DomainMakeSetting
import com.slt.cardealership.domain.model.Make
import com.slt.cardealership.domain.repo.DealerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BuildAndPriceSettingsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val allMakes: List<Make> = emptyList(),
    val allBodyTypes: List<BodyType> = emptyList(),
    val bnpMakes: List<DomainMakeSetting> = emptyList(),
    val bnpBodyTypes: List<DomainBodyTypeSetting> = emptyList()
)

@HiltViewModel
class BuildAndPriceSettingsViewModel @Inject constructor(
    private val repository: DealerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BuildAndPriceSettingsUiState())
    val uiState: StateFlow<BuildAndPriceSettingsUiState> = _uiState.asStateFlow()

    fun loadData(domainId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val makesResult = repository.getAllMakes()
            val bodyTypesResult = repository.getAllBodyTypes()
            val bnpMakesResult = repository.getDomainSettingMakes(domainId, "bnp", null)
            val bnpBodyTypesResult = repository.getDomainSettingBodyTypes(domainId, "bnp")

            val allMakes = makesResult.getOrDefault(emptyList())
            val allBodyTypes = bodyTypesResult.getOrDefault(emptyList())

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
                    allMakes = allMakes,
                    allBodyTypes = allBodyTypes,
                    bnpMakes = enrichMakes(bnpMakesResult.getOrDefault(emptyList())),
                    bnpBodyTypes = enrichBodyTypes(bnpBodyTypesResult.getOrDefault(emptyList()))
                )
            }
        }
    }

    fun addBnpMake(makeId: Int) {
        val make = _uiState.value.allMakes.find { it.id == makeId } ?: return
        if (_uiState.value.bnpMakes.any { it.makeId == makeId }) return

        val newSetting = DomainMakeSetting(
            makeId = make.id,
            vehicleModule = "bnp",
            makeName = make.name,
            condition = "default"
        )
        _uiState.update { it.copy(bnpMakes = it.bnpMakes + newSetting) }
    }

    fun removeBnpMake(makeId: Int) {
        _uiState.update { it.copy(bnpMakes = it.bnpMakes.filter { make -> make.makeId != makeId }) }
    }

    fun addBnpBodyType(bodyTypeId: Int) {
        val bodyType = _uiState.value.allBodyTypes.find { it.id == bodyTypeId } ?: return
        if (_uiState.value.bnpBodyTypes.any { it.bodyTypeId == bodyTypeId }) return

        val newSetting = DomainBodyTypeSetting(
            bodyTypeId = bodyType.id,
            vehicleModule = "bnp",
            bodyTypeName = bodyType.name
        )
        _uiState.update { it.copy(bnpBodyTypes = it.bnpBodyTypes + newSetting) }
    }

    fun removeBnpBodyType(bodyTypeId: Int) {
        _uiState.update { it.copy(bnpBodyTypes = it.bnpBodyTypes.filter { type -> type.bodyTypeId != bodyTypeId }) }
    }

    fun saveSettings(domainId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Save BNP Makes
            val bnpMakeIds = _uiState.value.bnpMakes.map { it.makeId }
            repository.saveDomainSettingMakes(domainId, "bnp", null, bnpMakeIds)

            // Save BNP Body Types
            val bnpBodyTypeIds = _uiState.value.bnpBodyTypes.map { it.bodyTypeId }
            repository.saveDomainSettingBodyTypes(domainId, "bnp", bnpBodyTypeIds)

            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
