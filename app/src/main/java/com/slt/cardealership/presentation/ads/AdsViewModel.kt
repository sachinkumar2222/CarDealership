package com.slt.cardealership.presentation.ads

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.data.local.SessionManager
import com.slt.cardealership.domain.model.Advertisement
import com.slt.cardealership.domain.model.AdvertisementGoal
import com.slt.cardealership.domain.model.AdvertisementGoalType
import com.slt.cardealership.domain.model.VehicleModel // Assuming you need this for 'Co-op'
import com.slt.cardealership.domain.repo.DealerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

// --- 1. UI STATE DATA CLASSES ---

data class AdsListState(
    val isLoading: Boolean = false,
    val ads: List<Advertisement> = emptyList(),
    val error: String? = null
)

data class AdFormState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isEditing: Boolean = false,
    val adId: String? = null,

    // Common Fields
    val adName: String = "",
    val adDescription: String = "",
    val selectedGoalId: Int = 0,
    val selectedGoalName: String = "",
    val goalUrl: String = "",
    val adType: String = "General", // Default to 'General'
    val startDate: String = "",
    val noEndDate: Boolean = false,
    val locationRadius: Float = 35f,
    val showLocation: Boolean = true, // Location is shown for General and Co-op

    // *** ADDED: Fields to hold dropdown selection ***
    val selectedGoalTypeId: Int = 0,
    val selectedGoalTypeName: String = "",

    // Conditional Fields
    val condition: String? = null, // For General & Co-op
    val makeId: Int? = null,      // For General & Co-op
    val makeName: String? = null,  // For General & Co-op
    val modelId: Int? = null,     // Only for General
    val modelName: String? = null, // Only for General
    val year: Int? = null,        // Only for General

    val locationType: String = "RADIUS", // Added for RadioButtons

    // Form validation error
    val formError: String? = null,

    val createdBy: Int? = null,
    val createdOn: Long? = null
)

data class AdGoalState(
    val isLoading: Boolean = false,
    val goals: List<AdvertisementGoal> = emptyList(),
    val error: String? = null
)

data class AdGoalTypeState(
    val isLoading: Boolean = false,
    val types: List<AdvertisementGoalType> = emptyList(),
    val error: String? = null
)

data class AdVehicleModelState(
    val isLoading: Boolean = false,
    val models: List<VehicleModel> = emptyList(),
    val error: String? = null
)

sealed interface AdsEvent {
    object NavigateBack : AdsEvent
    data class ShowError(val message: String) : AdsEvent
    data class ShowSuccess(val message: String) : AdsEvent
}


// --- 2. ADS VIEWMODEL ---

@HiltViewModel
class AdsViewModel @Inject constructor(
    private val repository: DealerRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    // --- Mappers (Copied from VehicleViewModel) ---
    private fun normalizeKey(s: String?) = s?.trim()?.lowercase() ?: ""

    private val makeMap = mapOf(
        "acura" to 2, "alfa romeo" to 3, "aston martin" to 5, "audi" to 6, "azbv" to 82,
        "b acura" to 87, "bentley" to 7, "bmw" to 8, "buick" to 10, "cadillac" to 11,
        "chevrolet" to 12, "chrysler" to 13, "dodge" to 16, "ferrari" to 18, "fiat" to 19,
        "ford" to 21, "genesis" to 22, "gmc" to 24, "hindustan" to 83, "honda" to 25,
        "hyundai" to 27, "ineos" to 70, "jaguar" to 30, "jeep" to 31, "lamborghini" to 34,
        "land rover" to 35, "lexus" to 36, "lincoln" to 37, "lotus" to 38, "lucid" to 39,
        "maserati" to 40, "mazda" to 42, "mclaren" to 43, "merc" to 84, "mercedes" to 79,
        "mercedes-benz" to 44, "mini" to 46, "mitsubishi" to 47, "naman" to 81, "nissan" to 48,
        "polestar" to 52, "porsche" to 54, "r acura" to 86, "ram" to 55, "rivian" to 56,
        "rolls-royce" to 57, "sasta" to 80, "simran" to 89, "subaru" to 62, "tesla" to 64,
        "test make" to 85, "toyota" to 33,
        "vinfast" to 66, "volkswagen" to 67, "volvo" to 68
    )

    private val makeReverseMap =
        makeMap.entries.associate { (k, v) -> v to k.replaceFirstChar { it.titlecase() } }

    private val adTypeReverseMap = mapOf(
        "General" to "general",
        "Co-op" to "co_op", // <-- FIX: Use underscore
        "Dealership Ad" to "dealership_advertisement" // <-- FIX: Use underscore
    )

    // Create reverse map for loading (NO CHANGE NEEDED HERE)
    private val adTypeMap = adTypeReverseMap.entries.associate { (k, v) -> v to k }


    // --- StateFlows ---
    private val _adsListState = MutableStateFlow(AdsListState())
    val adsListState: StateFlow<AdsListState> = _adsListState.asStateFlow()

    private val _formState = MutableStateFlow(AdFormState())
    val formState: StateFlow<AdFormState> = _formState.asStateFlow()

    private val _goalState = MutableStateFlow(AdGoalState())
    val goalState: StateFlow<AdGoalState> = _goalState.asStateFlow()

    private val _goalTypeState = MutableStateFlow(AdGoalTypeState())
    val goalTypeState: StateFlow<AdGoalTypeState> = _goalTypeState.asStateFlow()

    private val _modelState = MutableStateFlow(AdVehicleModelState())
    val modelState: StateFlow<AdVehicleModelState> = _modelState.asStateFlow()

    private val _events = Channel<AdsEvent>()
    val events = _events.receiveAsFlow()

    // --- Public UI Dropdown Options ---
    val makeOptions: List<String> = makeMap.keys
        .filter { it.isNotBlank() }
        .map { it.replaceFirstChar { char -> char.titlecase() } }
        .sorted()

    val adTypeOptions = adTypeReverseMap.keys.toList() // "General", "Co-op", "Dealership Ad"
    val conditionOptions = listOf("New", "Used", "CPO")
    private val conditionReverseMap = mapOf(
        "New" to "new",
        "Used" to "used",
        "CPO" to "cpo"
    )


    // --- Ads List Screen Logic ---

    fun getAdvertisements() {
        viewModelScope.launch {
            _adsListState.update { it.copy(isLoading = true, error = null) }
            val dealerId = sessionManager.getDealerId()
            if (dealerId == null) {
                _adsListState.update {
                    it.copy(
                        isLoading = false,
                        error = "User session error. Could not get Dealer ID."
                    )
                }
                return@launch
            }

            repository.getAdvertisements(dealerId.toLong())
                .onSuccess { ads ->
                    _adsListState.update { it.copy(isLoading = false, ads = ads) }
                }
                .onFailure { error ->
                    _adsListState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    fun deleteAdvertisement(advertisementId: String) {
        viewModelScope.launch {
            val dealerId = sessionManager.getDealerId()
            if (dealerId == null) {
                _events.send(AdsEvent.ShowError("User session error. Could not get Dealer ID."))
                return@launch
            }

            repository.deleteAdvertisement(dealerId.toLong(), advertisementId)
                .onSuccess {
                    _events.send(AdsEvent.ShowSuccess("Advertisement deleted successfully."))
                    getAdvertisements() // Refresh the list
                }
                .onFailure { error ->
                    _events.send(
                        AdsEvent.ShowError(
                            error.message ?: "Failed to delete advertisement."
                        )
                    )
                }
        }
    }

    // --- Add/Edit Ad Screen Logic ---

    fun prepareNewAdForm() {
        _formState.value = AdFormState() // Reset form
        _goalState.value = AdGoalState() // Clear goals
        _goalTypeState.value = AdGoalTypeState() // Clear goal types
        _modelState.value = AdVehicleModelState() // Clear models
        getAdvertisementGoals() // Fetch goals for the new form
    }

    fun loadAdForEdit(advertisementId: String) {
        viewModelScope.launch {
            Log.d("EDIT_DEBUG", "--- Starting loadAdForEdit ---")
            _formState.update { it.copy(isLoading = true, formError = null) }

            val dealerId = sessionManager.getDealerId()
            if (dealerId == null) {
                _formState.update { it.copy(isLoading = false, formError = "User session error.") }
                return@launch
            }
            val dealerIdLong = dealerId.toLong()

            // 1. Get Ad Details
            val adResult = repository.getAdvertisementDetails(dealerIdLong, advertisementId)
            if (adResult.isFailure) {
                _formState.update {
                    it.copy(
                        isLoading = false,
                        formError = adResult.exceptionOrNull()?.message
                    )
                }
                return@launch
            }
            val ad = adResult.getOrThrow()
            Log.d(
                "EDIT_DEBUG",
                "Ad loaded. API says goalListId=${ad.advertisementGoalTypeListId}, goalTypeId=${ad.advertisementGoalTypeId}"
            )

            // 2. Get the GLOBAL Goal List [1, 2, 3, 4, 5]
            val goalsResult = repository.getAdvertisementGoals() // Calls getGoalList
            val globalGoals = goalsResult.getOrNull() ?: emptyList()
            Log.d("EDIT_DEBUG", "Got ${globalGoals.size} global goals.")

            // 3. *** FIX FOR SWAPPED GOALS (BUG 1) ***
            // Check if the ad's main goal ID is in the global list
            val mainGoalId = ad.advertisementGoalTypeListId
            val subGoalId = ad.advertisementGoalTypeId

            val isSwapped =
                globalGoals.none { it.id == mainGoalId } && globalGoals.any { it.id == subGoalId }

            val correctMainGoalId = if (isSwapped) subGoalId else mainGoalId
            val correctSubGoalId = if (isSwapped) mainGoalId else subGoalId

            if (isSwapped) {
                Log.d(
                    "EDIT_DEBUG",
                    "Data is SWAPPED. CorrectMainGoal=$correctMainGoalId, CorrectSubGoal=$correctSubGoalId"
                )
            }
            // *** END FIX ***

            _goalState.update { it.copy(isLoading = false, goals = globalGoals) }

            // 4. Get Goal Types (using the CORRECT Main Goal ID)
            val goalTypesResult = repository.getAdvertisementGoalTypes(correctMainGoalId ?: 0)
            val goalTypes = goalTypesResult.getOrNull() ?: emptyList()
            _goalTypeState.update { it.copy(isLoading = false, types = goalTypes) }

            // 5. Get Models
            val makeId = ad.makes?.firstOrNull()
            val models = if (makeId != null && makeId != 0) {
                repository.getModelsForMake(makeId).getOrNull() ?: emptyList()
            } else {
                emptyList()
            }
            _modelState.update { it.copy(isLoading = false, models = models) }

            // 6. Resolve Names
            val makeName = ad.makeNames ?: makeReverseMap[makeId]
            val goalName = globalGoals.find { it.id == correctMainGoalId }?.name ?: "" // <-- Fixed

            // --- FIX FOR MISSING MODEL NAME (BUG 2) ---
            val modelName = models.find { it.id == ad.modelId }?.name ?: ad.modelName // <-- Fixed
            // ----------------------------------------

            val goalTypeName = goalTypes.find { it.id == correctSubGoalId }?.name ?: "" // <-- Fixed

            // --- FIX FOR AD TYPE LOADING (BUG 3) ---
            // Handle the API's inconsistent "type" string ("Co-op" vs "co_op")
            val apiTypeString =
                ad.type?.lowercase()?.replace("-", "_") // "Co-op" -> "co-op" -> "co_op"
            val adTypeUiName = adTypeMap[apiTypeString] ?: "General" // <-- Fixed
            // -------------------------------------

            Log.d("EDIT_DEBUG", "Resolved Goal Name: '$goalName'")
            Log.d("EDIT_DEBUG", "Resolved Goal Type Name: '$goalTypeName'")
            Log.d("EDIT_DEBUG", "Resolved Model Name: '$modelName'")
            Log.d("EDIT_DEBUG", "Resolved Ad Type: '$adTypeUiName' from API value '${ad.type}'")

            // 7. Set Final State
            _formState.value = AdFormState(
                isLoading = false,
                isEditing = true,
                adId = ad.id,
                adName = ad.title,
                adDescription = ad.description ?: "",
                selectedGoalId = correctMainGoalId ?: 0, // <-- Fixed
                selectedGoalName = goalName, // <-- Fixed
                goalUrl = ad.url ?: "",
                adType = adTypeUiName, // <-- Fixed
                startDate = ad.startDate?.toString() ?: "",
                noEndDate = ad.endDate == null,
                showLocation = apiTypeString in listOf("general", "co_op"), // <-- Fixed
                condition = ad.condition,
                makeId = makeId,
                makeName = makeName,
                modelId = ad.modelId,
                modelName = modelName, // <-- Fixed
                year = ad.year,
                selectedGoalTypeId = correctSubGoalId ?: 0, // <-- Fixed
                selectedGoalTypeName = goalTypeName, // <-- Fixed
                locationType = "RADIUS",
                createdBy = (ad.createdBy as? Double)?.toInt(),
                createdOn = ad.createdOn
            )
            Log.d("EDIT_DEBUG", "--- Load finished. ---")
        }
    }

    /** Called when the form state is changed by the UI. */
    fun onFormStateChange(newState: AdFormState) {
        _formState.value = newState
    }

    /** Fetches the list of goals (Sales, Leads, etc.) */
    fun getAdvertisementGoals() {
        viewModelScope.launch {
            _goalState.update { it.copy(isLoading = true, error = null) }
            repository.getAdvertisementGoals()
                .onSuccess { goals ->
                    _goalState.update { it.copy(isLoading = false, goals = goals) }
                }
                .onFailure { error ->
                    _goalState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    /** Fetches the goal types dropdown based on the selected goal. */
    fun fetchGoalTypes(goalId: Int) {
        viewModelScope.launch {
            _goalTypeState.update { it.copy(isLoading = true, error = null) }
            repository.getAdvertisementGoalTypes(goalId)
                .onSuccess { types ->
                    _goalTypeState.update { it.copy(isLoading = false, types = types) }
                }
                .onFailure { error ->
                    _goalTypeState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    /** Fetches the list of models based on the selected make. */
    fun fetchModelsForMake(makeId: Int) {
        if (makeId == 0) {
            _modelState.update { it.copy(models = emptyList()) } // Clear list if make is cleared
            return
        }
        viewModelScope.launch {
            _modelState.update { it.copy(isLoading = true, error = null) }
            repository.getModelsForMake(makeId) // Re-using Vehicle function
                .onSuccess { models ->
                    _modelState.update { it.copy(isLoading = false, models = models) }
                }
                .onFailure { error ->
                    _modelState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    // In AdsViewModel.kt
// In AdsViewModel.kt
    // In AdsViewModel.kt
// REPLACE your saveAdvertisement function with this one

    // In AdsViewModel.kt
// REPLACE your saveAdvertisement function with this one

    fun saveAdvertisement() {
        val state = _formState.value

        // --- Validation ---
        if (state.adName.isBlank() || state.selectedGoalId == 0 || state.goalUrl.isBlank() || state.adType.isBlank()) {
            _formState.update { it.copy(formError = "Please fill in all required fields (*).") }
            return
        }

        if (state.selectedGoalTypeId == 0 && state.adType != "Dealership Ad") {
            _formState.update { it.copy(formError = "Please select a Goal Category Type.") }
            return
        }

        viewModelScope.launch {
            _formState.update { it.copy(isSaving = true, formError = null) }
            val dealerId = sessionManager.getDealerId()
            if (dealerId == null) {
                _formState.update { it.copy(isSaving = false, formError = "User session error.")}
                return@launch
            }

            val apiAdType = adTypeReverseMap[state.adType]
            val apiCondition = state.condition?.let { conditionReverseMap[it] }
            val currentUserId = state.createdBy ?: 630

            val ad = Advertisement(
                id = state.adId,
                title = state.adName,
                type = apiAdType,
                startDate = state.startDate.toLongOrNull(),
                endDate = if (state.noEndDate) null else null,
                condition = apiCondition,
                makeNames = null,
                modelName = if (state.adType == "General") state.modelName else null,
                year = if (state.adType == "General") state.year else null,
                description = state.adDescription,
                advertisementGoalTypeListId = state.selectedGoalId,
                advertisementGoalTypeId = state.selectedGoalTypeId,
                url = state.goalUrl,
                status = "active",
                makes = if (state.adType in listOf("General", "Co-op") && state.makeId != null && state.makeId != 0) listOf(state.makeId) else null,
                modelId = if (state.adType == "General") state.modelId else null,
                isDeleted = null,
                createdBy = if (state.isEditing) state.createdBy else currentUserId,
                updatedBy = currentUserId,
                createdOn = if (state.isEditing) state.createdOn else System.currentTimeMillis() / 1000L,
                updatedOn = System.currentTimeMillis() / 1000L,
                keywords = null,
                haveImage = null,
                haveDomain = null,
                makeId = null
            )

            Log.d("AdsViewModel", "Saving advertisement: $ad")

            // --- THIS IS THE FIX ---
            // We now have different logic for Editing vs. Adding
            if (state.isEditing) {
                // --- UPDATE LOGIC ---
                val result = repository.updateAdvertisement(dealerId.toLong(), state.adId!!, ad)
                result.onSuccess {
                    _formState.update { it.copy(isSaving = false) }
                    _events.send(AdsEvent.NavigateBack)
                    getAdvertisements() // Refresh the list
                }
                    .onFailure { error ->
                        _formState.update { it.copy(isSaving = false, formError = error.message) }
                        _events.send(AdsEvent.ShowError(error.message ?: "Failed to save advertisement."))
                    }
            } else {
                // --- ADD LOGIC ---
                val result = repository.addAdvertisement(dealerId.toLong(), ad)
                result.onSuccess { newAdIdString ->
                    Log.d("AdsViewModel", "Successfully created ad with ID: $newAdIdString")
                    _formState.update { it.copy(isSaving = false) }
                    _events.send(AdsEvent.NavigateBack) // This will now be called
                    getAdvertisements() // Refresh the list
                }
                    .onFailure { error ->
                        _formState.update { it.copy(isSaving = false, formError = error.message) }
                        _events.send(AdsEvent.ShowError(error.message ?: "Failed to save advertisement."))
                    }
            }
            // --- END FIX ---
        }
    }

}

