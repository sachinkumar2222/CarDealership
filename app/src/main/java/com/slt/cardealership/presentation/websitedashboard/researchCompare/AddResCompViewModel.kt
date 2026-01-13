package com.slt.cardealership.presentation.websitedashboard.researchCompare

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

data class AddResCompUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val categories: List<ResearchCompareCategory> = emptyList(),
    val selectedCategory: ResearchCompareCategory? = null,
    val vehicle1: ResearchCompareTrim? = null,
    val vehicle2: ResearchCompareTrim? = null,
    val isVehicle1Added: Boolean = false,
    val isVehicle2Added: Boolean = false,
    val isSaveSuccess: Boolean = false
)

data class VehicleSelectionState(
    val makes: List<ResearchMake> = emptyList(),
    val models: List<ResearchModel> = emptyList(),
    val years: List<ResearchYear> = emptyList(),
    val trims: List<ResearchTrim> = emptyList(),
    val selectedMake: ResearchMake? = null,
    val selectedModel: ResearchModel? = null,
    val selectedYear: ResearchYear? = null,
    val selectedTrim: ResearchTrim? = null,
    val isLoadingDropdowns: Boolean = false
)

@HiltViewModel
class AddResCompViewModel @Inject constructor(
    private val repository: DealerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddResCompUiState())
    val uiState: StateFlow<AddResCompUiState> = _uiState.asStateFlow()

    private val _vehicleSelectionState = MutableStateFlow(VehicleSelectionState())
    val vehicleSelectionState: StateFlow<VehicleSelectionState> = _vehicleSelectionState.asStateFlow()

    // Temp state to know which slot we are filling (1 or 2)
    private var currentSlot: Int = 0
    private var domainName: String = ""
    private var currentResearchCompareId: String? = null // To track if we are in Edit mode

    fun init(domainId: Int) {
        fetchCategories(domainId)
        fetchMakes()
        fetchDomainDetails(domainId)
        currentResearchCompareId = null // Reset on init
    }

    private fun fetchDomainDetails(domainId: Int) {
        viewModelScope.launch {
            val result = repository.getDomainDetails(domainId)
            result.onSuccess {
                domainName = it.domainName
            }
        }
    }

    fun loadResearchCompare(id: String) {
        currentResearchCompareId = id // Set ID for edit mode
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.getResearchCompareDetails(id)
            result.fold(
                onSuccess = { details ->
                    // Transform details to UI state
                    val category = details.category?.let {
                        ResearchCompareCategory(
                            id = it.id ?: "",
                            name = it.categoryName ?: "",
                            slug = it.categorySlug ?: ""
                        )
                    }

                    val v1 = details.items?.getOrNull(0)
                    val v2 = details.items?.getOrNull(1)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            selectedCategory = category,
                            vehicle1 = v1,
                            vehicle2 = v2,
                            isVehicle1Added = v1 != null,
                            isVehicle2Added = v2 != null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
            )
        }
    }

    private fun fetchCategories(domainId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.getResearchCompareCategories(domainId)
            result.fold(
                onSuccess = { response ->
                    _uiState.update { it.copy(isLoading = false, categories = response.list) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
            )
        }
    }

    private fun fetchMakes() {
        viewModelScope.launch {
            _vehicleSelectionState.update { it.copy(isLoadingDropdowns = true) }
            val result = repository.getResearchMakes()
            result.fold(
                onSuccess = { list ->
                    _vehicleSelectionState.update { it.copy(isLoadingDropdowns = false, makes = list) }
                },
                onFailure = {
                    _vehicleSelectionState.update { it.copy(isLoadingDropdowns = false) }
                }
            )
        }
    }

    fun onMakeSelected(make: ResearchMake) {
        _vehicleSelectionState.update {
            it.copy(
                selectedMake = make,
                selectedModel = null,
                selectedYear = null,
                selectedTrim = null,
                models = emptyList(),
                years = emptyList(),
                trims = emptyList()
            )
        }
        fetchModels(make.id)
    }

    private fun fetchModels(makeId: Int) {
        viewModelScope.launch {
            _vehicleSelectionState.update { it.copy(isLoadingDropdowns = true) }
            val result = repository.getResearchModels(makeId)
            result.fold(
                onSuccess = { list ->
                    _vehicleSelectionState.update { it.copy(isLoadingDropdowns = false, models = list) }
                },
                onFailure = {
                    _vehicleSelectionState.update { it.copy(isLoadingDropdowns = false) }
                }
            )
        }
    }

    fun onModelSelected(model: ResearchModel) {
        val make = _vehicleSelectionState.value.selectedMake ?: return
        _vehicleSelectionState.update {
            it.copy(
                selectedModel = model,
                selectedYear = null,
                selectedTrim = null,
                years = emptyList(),
                trims = emptyList()
            )
        }
        fetchYears(make.id, model.id)
    }

    private fun fetchYears(makeId: Int, modelId: Int) {
        viewModelScope.launch {
            _vehicleSelectionState.update { it.copy(isLoadingDropdowns = true) }
            val result = repository.getResearchModelYears(makeId, modelId)
            result.fold(
                onSuccess = { list ->
                    _vehicleSelectionState.update { it.copy(isLoadingDropdowns = false, years = list) }
                },
                onFailure = {
                    _vehicleSelectionState.update { it.copy(isLoadingDropdowns = false) }
                }
            )
        }
    }

    fun onYearSelected(year: ResearchYear) {
        val make = _vehicleSelectionState.value.selectedMake ?: return
        val model = _vehicleSelectionState.value.selectedModel ?: return
        _vehicleSelectionState.update {
            it.copy(
                selectedYear = year,
                selectedTrim = null,
                trims = emptyList()
            )
        }
        fetchTrims(make.id, model.id, year.year)
    }

    private fun fetchTrims(makeId: Int, modelId: Int, year: Int) {
        viewModelScope.launch {
            _vehicleSelectionState.update { it.copy(isLoadingDropdowns = true) }
            val result = repository.getResearchTrims(makeId, modelId, year)
            result.fold(
                onSuccess = { list ->
                    _vehicleSelectionState.update { it.copy(isLoadingDropdowns = false, trims = list) }
                },
                onFailure = {
                    _vehicleSelectionState.update { it.copy(isLoadingDropdowns = false) }
                }
            )
        }
    }

    fun onTrimSelected(trim: ResearchTrim) {
        _vehicleSelectionState.update { it.copy(selectedTrim = trim) }
    }

    fun startSelectingVehicle(slot: Int) {
        currentSlot = slot
        // Reset selection state for new selection if desired, or keep last?
        // Usually reset is better so user starts fresh
        _vehicleSelectionState.update {
            it.copy(
                selectedMake = null,
                selectedModel = null,
                selectedYear = null,
                selectedTrim = null,
                models = emptyList(),
                years = emptyList(),
                trims = emptyList()
            )
        }
    }

    fun saveVehicleSelection() {
        val state = _vehicleSelectionState.value
        val make = state.selectedMake ?: return
        val model = state.selectedModel ?: return
        val year = state.selectedYear ?: return
        val trim = state.selectedTrim ?: return

        val researchTrim = ResearchCompareTrim(
            makeName = make.name,
            makeSlug = make.slug,
            modelName = model.name,
            modelSlug = model.slug,
            year = year.year,
            trimName = trim.name,
            trimSlug = trim.slug
        )

        _uiState.update {
            if (currentSlot == 1) {
                it.copy(vehicle1 = researchTrim, isVehicle1Added = true)
            } else {
                it.copy(vehicle2 = researchTrim, isVehicle2Added = true)
            }
        }
    }

    fun onCategorySelected(category: ResearchCompareCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun saveComparison(domainId: Int) {
        val state = _uiState.value
        val category = state.selectedCategory ?: return
        val v1 = state.vehicle1 ?: return
        val v2 = state.vehicle2 ?: return

        val items = listOf(v1, v2)
        val categoryRequest = ResearchCompareCategoryRequest(
            id = category.id,
            categoryName = category.name ?: "",
            categorySlug = category.slug ?: ""
        )

        val currentTime = System.currentTimeMillis() / 1000 // Seconds

        val request = CreateResearchCompareRequest(
            category = categoryRequest,
            items = items,
            createdBy = "sachinsingh@slt.work", // TODO: Get from session
            createdOn = currentTime,
            domainName = this.domainName, // Use member variable
            sqlDomainId = domainId,
            updatedBy = "sachinsingh@slt.work",
            updatedOn = currentTime
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = if (currentResearchCompareId != null) {
                // UPDATE
                repository.updateResearchCompare(currentResearchCompareId!!, request)
            } else {
                // CREATE
                repository.createResearchCompare(request)
            }

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, isSaveSuccess = true) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
