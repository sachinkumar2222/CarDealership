package com.slt.cardealership.presentation.websitedashboard.settings.research

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.domain.model.BodyType
import com.slt.cardealership.domain.model.DomainBodyTypeSetting
import com.slt.cardealership.domain.model.DomainMakeSetting
import com.slt.cardealership.domain.model.DomainResearchSetting
import com.slt.cardealership.domain.model.Make
import com.slt.cardealership.domain.repo.DealerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResearchSettingsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val researchSettings: DomainResearchSetting? = null,
    val allMakes: List<Make> = emptyList(),
    val allBodyTypes: List<BodyType> = emptyList(),
    val researchMakes: List<DomainMakeSetting> = emptyList(),
    val researchBodyTypes: List<DomainBodyTypeSetting> = emptyList()
)

@HiltViewModel
class ResearchSettingsViewModel @Inject constructor(
    private val repository: DealerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResearchSettingsUiState())
    val uiState: StateFlow<ResearchSettingsUiState> = _uiState.asStateFlow()

    fun loadData(domainId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val settingsResult = repository.getDomainResearchSetting(domainId)
            val makesResult = repository.getAllMakes()
            val bodyTypesResult = repository.getAllBodyTypes()
            val researchMakesResult = repository.getDomainSettingMakes(domainId, "research", null)
            val researchBodyTypesResult = repository.getDomainSettingBodyTypes(domainId, "research")

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
                    researchSettings = settingsResult.getOrNull(),
                    allMakes = allMakes,
                    allBodyTypes = allBodyTypes,
                    researchMakes = enrichMakes(researchMakesResult.getOrDefault(emptyList())),
                    researchBodyTypes = enrichBodyTypes(researchBodyTypesResult.getOrDefault(emptyList()))
                )
            }
        }
    }

    fun updateMinYear(year: String) {
        val currentSettings = _uiState.value.researchSettings ?: return
        val minYear = year.toIntOrNull()
        _uiState.update { it.copy(researchSettings = currentSettings.copy(minYear = minYear)) }
    }

    fun updateShowDiscontinued(show: Boolean) {
        val currentSettings = _uiState.value.researchSettings ?: return
        _uiState.update { it.copy(researchSettings = currentSettings.copy(showDiscontinuedMakes = show)) }
    }

    fun addResearchMake(makeId: Int) {
        val make = _uiState.value.allMakes.find { it.id == makeId } ?: return
        if (_uiState.value.researchMakes.any { it.makeId == makeId }) return

        val newSetting = DomainMakeSetting(
            makeId = make.id,
            vehicleModule = "research",
            makeName = make.name,
            condition = "default"
        )
        _uiState.update { it.copy(researchMakes = it.researchMakes + newSetting) }
    }

    fun removeResearchMake(makeId: Int) {
        _uiState.update { it.copy(researchMakes = it.researchMakes.filter { make -> make.makeId != makeId }) }
    }

    fun addResearchBodyType(bodyTypeId: Int) {
        val bodyType = _uiState.value.allBodyTypes.find { it.id == bodyTypeId } ?: return
        if (_uiState.value.researchBodyTypes.any { it.bodyTypeId == bodyTypeId }) return

        val newSetting = DomainBodyTypeSetting(
            bodyTypeId = bodyType.id,
            vehicleModule = "research",
            bodyTypeName = bodyType.name
        )
        _uiState.update { it.copy(researchBodyTypes = it.researchBodyTypes + newSetting) }
    }

    fun removeResearchBodyType(bodyTypeId: Int) {
        _uiState.update { it.copy(researchBodyTypes = it.researchBodyTypes.filter { type -> type.bodyTypeId != bodyTypeId }) }
    }

    fun saveSettings(domainId: Int) {
        val settings = _uiState.value.researchSettings ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Save Research Settings
            repository.saveDomainResearchSetting(settings)

            // Save Research Makes
            val researchMakeIds = _uiState.value.researchMakes.map { it.makeId }
            repository.saveDomainSettingMakes(domainId, "research", null, researchMakeIds)

            // Save Research Body Types
            val researchBodyTypeIds = _uiState.value.researchBodyTypes.map { it.bodyTypeId }
            repository.saveDomainSettingBodyTypes(domainId, "research", researchBodyTypeIds)

            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
