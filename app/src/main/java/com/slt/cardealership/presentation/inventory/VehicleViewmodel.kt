package com.slt.cardealership.presentation.vehicle

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.data.local.SessionManager
import com.slt.cardealership.domain.model.EvoxImageResponse
import com.slt.cardealership.domain.model.GalleryImageUploadResponse
import com.slt.cardealership.domain.model.TrimListResponse
import com.slt.cardealership.domain.model.Vehicle
import com.slt.cardealership.domain.model.VehicleGalleryResponse
import com.slt.cardealership.domain.model.VehicleModel
import com.slt.cardealership.domain.model.VehicleOptionsResponse
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
import java.time.Instant
import javax.inject.Inject

// --- 1. UI STATE DATA CLASSES ---

data class VehicleListState(
    val isLoading: Boolean = false,
    val vehicles: List<Vehicle> = emptyList(),
    val error: String? = null,
    val currentPage: Int = 1,
    val canLoadMore: Boolean = true
)

data class VehicleModelState(
    val isLoading: Boolean = false,
    val models: List<VehicleModel> = emptyList(),
    val error: String? = null
)

data class VehicleDetailState(
    val isLoading: Boolean = false,
    val vehicle: Vehicle? = null,
    val isSuccess: Boolean = false,
    val error: String? = null
)

data class VinDecodeState(
    val isLoading: Boolean = false,
    val trimList: TrimListResponse? = null,
    val evoxImages: EvoxImageResponse? = null,
    val optionsAndPackages: VehicleOptionsResponse? = null,
    val decodedVehicle: Vehicle? = null,
    val error: String? = null
)

data class VehicleGalleryState(
    val isLoading: Boolean = false,
    val gallery: VehicleGalleryResponse? = null,
    val singleUploadResult: GalleryImageUploadResponse? = null,
    val multiUploadSuccess: Boolean = false,
    val error: String? = null
)

// Form state and events for Add/Edit
data class AddEditVehicleFormState(
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val isSaveSuccess: Boolean = false,
    val formError: String? = null,
    val id: String? = null, // Stores the UUID when editing
    val vin: String = "",
    val make: String = "",
    val model: String = "",
    val year: String = "",
    val trim: String = "",
    val status: String = "Available",
    val condition: String = "New",
    val certified: String = "No",
    val drivetrain: String = "",
    val bodyType: String = "",
    val transmissionType: String = "",
    val fuelType: String = "",
    val interiorColor: String = "",
    val exteriorColor: String = "",
    val stockNumber: String = "",
    val mileage: String = "",
    val dealerPrice: String = "",
    val ourPrice: String = "", // Corresponds to retailPrice in model
    val engineCylinders: String = "",
    val doors: String = "",
    val vdpLink: String = "",
    val features: String = "",
    val comments: String = "",
    val inventoryFlags: Map<String, Boolean> = mapOf(
        "Carfax" to false, "1 Owner" to false, "AutoCheck" to false, "Dealer Certified" to false,
        "Warranty" to false, "Factory Warranty" to false, "Green Vehicle" to false, "Ext Warranty" to false
    )
)

sealed interface InventoryEvent {
    object NavigateBack : InventoryEvent
    data class ShowError(val message: String) : InventoryEvent
}

@HiltViewModel
class VehicleViewModel @Inject constructor(
    private val repository: DealerRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val itemsPerPage = 20

    private val _modelState = MutableStateFlow(VehicleModelState())
    val modelState: StateFlow<VehicleModelState> = _modelState.asStateFlow()

    private val _formState = MutableStateFlow(AddEditVehicleFormState())
    val formState: StateFlow<AddEditVehicleFormState> = _formState.asStateFlow()

    private val _events = Channel<InventoryEvent>()
    val events = _events.receiveAsFlow()

    private val _vehicleListState = MutableStateFlow(VehicleListState())
    val vehicleListState: StateFlow<VehicleListState> = _vehicleListState.asStateFlow()

    private val _vehicleDetailState = MutableStateFlow(VehicleDetailState())
    val vehicleDetailState: StateFlow<VehicleDetailState> = _vehicleDetailState.asStateFlow()

    private val _vinDecodeState = MutableStateFlow(VinDecodeState())
    val vinDecodeState: StateFlow<VinDecodeState> = _vinDecodeState.asStateFlow()

    private val _galleryState = MutableStateFlow(VehicleGalleryState())
    val galleryState: StateFlow<VehicleGalleryState> = _galleryState.asStateFlow()

    // --- Public API for form ---
    fun onFormStateChange(newState: AddEditVehicleFormState) {
        _formState.value = newState
    }

    fun prepareNewVehicleForm() {
        _formState.value = AddEditVehicleFormState()
        _modelState.value = VehicleModelState() // Clear models
    }

    fun onSaveSuccessConsumed() {
        _formState.update { it.copy(isSaveSuccess = false) }
    }

    // --- Sort Options ---
    enum class InventorySortOption(val displayName: String, val orderBy: String?, val order: String?) {
        MAKE_AZ("Make (A-Z)", "brand_name", "asc"),
        MODEL_AZ("Model (A-Z)", "model_name", "asc"),
        YEAR_ASC("Year (ASC)", "year", "asc"),
        YEAR_DESC("Year (DESC)", "year", "desc"),
        PRICE_ASC("Price (ASC)", "price", "asc"),
        PRICE_DESC("Price (DESC)", "price", "desc"),
        CREATED_ASC("Created On (ASC)", "created_on", "asc"),
        CREATED_DESC("Created On (DESC)", "created_on", "desc"),
        UPDATED_ASC("Updated On (ASC)", "updated_on", "asc");
    }

    private val _sortOption = MutableStateFlow(InventorySortOption.CREATED_DESC)
    val sortOption: StateFlow<InventorySortOption> = _sortOption.asStateFlow()

    fun updateSortOption(option: InventorySortOption) {
        _sortOption.value = option
        _vehicleListState.update { it.copy(canLoadMore = true, currentPage = 1, vehicles = emptyList()) }
        getResearchVehicles(loadNextPage = false)
    }

    // --- List functions ---
    fun getResearchVehicles(loadNextPage: Boolean = false) {
        viewModelScope.launch {
            val currentState = _vehicleListState.value
            val currentPage = if (loadNextPage) currentState.currentPage + 1 else 1

            if (currentState.isLoading || (loadNextPage && !currentState.canLoadMore)) {
                return@launch
            }

            val currentDealerId = sessionManager.getDealerId()
            if (currentDealerId == null) {
                _vehicleListState.update { it.copy(isLoading = false, error = "Could not get Dealer ID.") }
                _events.send(InventoryEvent.ShowError("User session error. Could not retrieve Dealer ID."))
                return@launch
            }

            _vehicleListState.update { it.copy(isLoading = true, error = null) }

            val currentSort = _sortOption.value

            repository.getResearchVehicles(
                dealerId = currentDealerId,
                page = currentPage,
                itemsPerPage = itemsPerPage,
                orderBy = currentSort.orderBy,
                order = currentSort.order
            ).onSuccess { newVehicles ->
                _vehicleListState.update { state ->
                    val updatedList = if (currentPage == 1) newVehicles else state.vehicles + newVehicles
                    val canLoadMore = newVehicles.size >= itemsPerPage
                    state.copy(
                        isLoading = false,
                        vehicles = updatedList,
                        currentPage = currentPage,
                        canLoadMore = canLoadMore
                    )
                }
            }.onFailure { error ->
                _vehicleListState.update {
                    it.copy(isLoading = false, error = error.message)
                }
            }
        }
    }

    // --- Detail / Add / Edit (mapped from form) ---
    fun loadVehicleForEdit(vehicleId: String) {
        Log.d("ViewModel", "loadVehicleForEdit called for ID: $vehicleId")
        viewModelScope.launch {
            _formState.update { it.copy(isSaving = true) }
            repository.getResearchVehicleDetails(vehicleId)
                .onSuccess { vehicle ->
                    Log.d("ViewModel", "Successfully loaded vehicle: ${vehicle.vin}")
                    // Resolve Make Name priority: Brand Name -> Make ID -> Brand Slug
                    val resolvedMake = vehicle.brandName
                        ?: getMakeString(vehicle.makeId).takeIf { it.isNotBlank() }
                        ?: vehicle.brandSlug?.replaceFirstChar { it.titlecase() }
                        ?: ""

                    _formState.value = AddEditVehicleFormState(
                        isEditing = true,
                        id = vehicle.id,
                        vin = vehicle.vin,
                        make = resolvedMake,
                        model = vehicle.modelName ?: "",
                        year = vehicle.year.toString(),
                        trim = vehicle.trimName ?: "",
                        status = vehicle.status?.replaceFirstChar { it.titlecase() } ?: "Available",
                        condition = vehicle.condition?.replaceFirstChar { it.titlecase() } ?: "Used",
                        certified = if (vehicle.certified == "true") "Yes" else "No",

                        drivetrain = getDrivetrainString(vehicle.drivetrainId),
                        bodyType = getBodyTypeString(vehicle.bodyTypeId),
                        transmissionType = getTransmissionString(vehicle.transmissionId),
                        fuelType = getFuelTypeString(vehicle.fuelTypeId),

                        interiorColor = vehicle.interiorColorMfr ?: "",
                        exteriorColor = vehicle.exteriorColorMfr ?: "",
                        stockNumber = vehicle.stockNo ?: "",
                        mileage = vehicle.mileage?.toString() ?: "0.0",
                        dealerPrice = vehicle.dealerPrice?.toString() ?: "0.0",
                        ourPrice = vehicle.retailPrice?.toString() ?: "0.0",
                        engineCylinders = vehicle.engineCylinders.toString(),
                        doors = vehicle.doors.toString(),
                        vdpLink = vehicle.vdpLink ?: "",

                        features = vehicle.featureString,
                        comments = vehicle.dealerNotesString,

                        inventoryFlags = mapOf(
                            "Carfax" to vehicle.carfax,
                            "1 Owner" to vehicle.ownerCf,
                            "AutoCheck" to vehicle.autoCheck,
                            "Dealer Certified" to vehicle.dealerCertified,
                            "Warranty" to vehicle.dealerWarranty,
                            "Factory Warranty" to vehicle.factoryWarranty,
                            "Green Vehicle" to vehicle.greenVehicle,
                            "Ext Warranty" to vehicle.extWarranty
                        )
                    )
                    // Fetch models *after* setting state using the resolved make
                    if (resolvedMake.isNotBlank()) {
                        fetchModelsForMake(resolvedMake)
                    }
                    _formState.update { it.copy(isSaving = false) }
                }
                .onFailure { error ->
                    Log.e("ViewModel", "Failed to load vehicle details for ID $vehicleId: ${error.message}", error)
                    _formState.update { it.copy(isSaving = false) }
                    viewModelScope.launch {
                        _events.send(InventoryEvent.ShowError("Failed to load vehicle details: ${error.message}"))
                        Log.d("ViewModel", "Sending NavigateBack event due to load failure.")
                        _events.send(InventoryEvent.NavigateBack)
                    }
                }
        }
    }

    fun saveVehicle() {
        val state = _formState.value
        _formState.update { it.copy(isSaving = true, formError = null) }

        val yearInt = state.year.toIntOrNull()
        val mileageDouble = state.mileage.toDoubleOrNull() ?: 0.0
        val dealerPriceDouble = state.dealerPrice.toDoubleOrNull() ?: 0.0
        val retailPriceDouble = state.ourPrice.toDoubleOrNull() ?: 0.0
        val cylindersInt = state.engineCylinders.toIntOrNull() ?: 0
        val doorsInt = state.doors.toIntOrNull() ?: 0

        if (state.vin.isBlank() || state.make.isBlank() || state.model.isBlank() || yearInt == null) {
            _formState.update { it.copy(isSaving = false, formError = "Please fill in all required fields: VIN, Make, Model, and Year.") }
            return
        }

        val drivetrainId = getDrivetrainId(state.drivetrain)
        val bodyTypeId = getBodyTypeId(state.bodyType)
        val transmissionId = getTransmissionId(state.transmissionType)
        val fuelTypeId = getFuelTypeId(state.fuelType)
        val makeId = getMakeId(state.make)
        val selectedModel = _modelState.value.models.find { normalizeKey(it.name) == normalizeKey(state.model) }
        val modelId = selectedModel?.id ?: 0

        if (modelId == 0 && state.model.isNotBlank()) {
            _formState.update { it.copy(isSaving = false, formError = "Invalid Model selected. Please choose from the list.") }
            return
        }

        val flags = state.inventoryFlags

        viewModelScope.launch {
            val currentDealerId = sessionManager.getDealerId()
            if (currentDealerId == null) {
                _formState.update { it.copy(isSaving = false, formError = "User session error. Cannot determine Dealer ID.") }
                return@launch
            }

            val vehicle = Vehicle(
                id = if (state.isEditing) state.id else null,
                vin = state.vin,
                makeId = makeId,
                dealerId = currentDealerId,
                modelId = modelId,
                year = yearInt,
                bodyTypeId = bodyTypeId,
                fuelTypeId = fuelTypeId,
                transmissionId = transmissionId,
                drivetrainId = drivetrainId,
                certified = (state.certified == "Yes").toString(),
                createdBy = 255,
                updatedBy = 255,
                createdOn = Instant.now().epochSecond,
                updatedOn = Instant.now().epochSecond,
                trimName = state.trim,
                condition = state.condition.lowercase(),
                status = state.status.lowercase().replace("available", "active"),
                exteriorColorMfr = state.exteriorColor,
                interiorColorMfr = state.interiorColor,
                stockNo = state.stockNumber,
                engineCylinders = cylindersInt,
                doors = doorsInt,
                mileage = mileageDouble,
                dealerPrice = dealerPriceDouble,
                retailPrice = retailPriceDouble,
                vdpLink = state.vdpLink,
                thumbnailImage = null, // Send null for nullable String

                // *** UPDATED TO USE RAW FIELDS from Vehicle.kt ***
                dealerNotesRaw = state.comments,
                featureRaw = state.features,

                authorizeDealer = false,
                carfax = flags["Carfax"] ?: false,
                ownerCf = flags["1 Owner"] ?: false,
                autoCheck = flags["AutoCheck"] ?: false,
                dealerCertified = flags["Dealer Certified"] ?: false,
                dealerWarranty = flags["Warranty"] ?: false,
                factoryWarranty = flags["Factory Warranty"] ?: false,
                greenVehicle = flags["Green Vehicle"] ?: false,
                extWarranty = flags["Ext Warranty"] ?: false,
                brandName = state.make,
                brandSlug = state.make.lowercase().replace(" ", "-"),
                modelName = state.model,
                modelSlug = selectedModel?.slug ?: state.model.lowercase().replace(" ", "-"),
                trimSlug = state.trim.lowercase().replace(" ", "-"),
                vehicleOptions = emptyList(),
                vehiclePackages = emptyList()
            )

            Log.d("SaveVehicle", "Attempting to save vehicle: $vehicle")

            // Check if we have a valid return type for addVehicle (Result<String>)
            val result = if (state.isEditing) {
                if (state.id == null) {
                    _formState.update { it.copy(isSaving = false, formError = "Cannot update vehicle: Missing ID.") }
                    _events.send(InventoryEvent.ShowError("Cannot update vehicle: Missing ID."))
                    return@launch
                }
                repository.editResearchVehicle(state.id, vehicle) // editResearchVehicle returns Result<Vehicle>
            } else {
                repository.addVehicle(0L, vehicle) // addVehicle returns Result<String>
            }

            result.onSuccess {
                _formState.update { it.copy(isSaving = false, isSaveSuccess = true) }
                // _events.send(InventoryEvent.NavigateBack) // Handled by state now
                getResearchVehicles()
            }.onFailure { error ->
                val errorMessage = error.message ?: "Unknown error occurred while saving."
                val displayMessage = if (errorMessage.contains("Vehicle already exists")) {
                    "This VIN already exists in the system for another dealer."
                } else {
                    errorMessage
                }
                _formState.update { it.copy(isSaving = false, formError = displayMessage) }
            }
        }
    }

    // --- Function to fetch models ---
    fun fetchModelsForMake(makeName: String) {
        val makeId = getMakeId(makeName)
        if (makeId == 0) {
            _modelState.update { it.copy(isLoading = false, models = emptyList(), error = "Invalid Make selected") }
            return
        }
        viewModelScope.launch {
            _modelState.update { it.copy(isLoading = true, error = null) }
            repository.getModelsForMake(makeId)
                .onSuccess { models ->
                    _modelState.update { it.copy(isLoading = false, models = models) }
                }
                .onFailure { error ->
                    _modelState.update { it.copy(isLoading = false, models = emptyList(), error = error.message) }
                }
        }
    }

    // --- VIN Decoder functions (unchanged) ---
    fun getTrimsByVin(vin: String) {
        viewModelScope.launch {
            _vinDecodeState.update { it.copy(isLoading = true, error = null) }
            repository.getTrimListByVin(vin)
                .onSuccess { trimList -> _vinDecodeState.update { it.copy(isLoading = false, trimList = trimList) } }
                .onFailure { error -> _vinDecodeState.update { it.copy(isLoading = false, error = error.message) } }
        }
    }
    fun getEvoxImages(vin: String) {
        viewModelScope.launch {
            _vinDecodeState.update { it.copy(isLoading = true, error = null) }
            repository.getEvoxImages(vin)
                .onSuccess { images -> _vinDecodeState.update { it.copy(isLoading = false, evoxImages = images) } }
                .onFailure { error -> _vinDecodeState.update { it.copy(isLoading = false, error = error.message) } }
        }
    }
    fun getOptionsAndPackages(vin: String) {
        viewModelScope.launch {
            _vinDecodeState.update { it.copy(isLoading = true, error = null) }
            repository.getVehicleOptionsAndPackages(vin)
                .onSuccess { options -> _vinDecodeState.update { it.copy(isLoading = false, optionsAndPackages = options) } }
                .onFailure { error -> _vinDecodeState.update { it.copy(isLoading = false, error = error.message) } }
        }
    }
    fun getDecodedDataByTrim(trimId: String) {
        viewModelScope.launch {
            _vinDecodeState.update { it.copy(isLoading = true, error = null) }
            repository.getDecodedDataByTrim(trimId)
                .onSuccess { vehicle -> _vinDecodeState.update { it.copy(isLoading = false, decodedVehicle = vehicle) } }
                .onFailure { error -> _vinDecodeState.update { it.copy(isLoading = false, error = error.message) } }
        }
    }
    fun clearVinDecodeState() {
        _vinDecodeState.value = VinDecodeState()
    }

    // --- Vehicle Gallery functions (unchanged) ---
    fun getVehicleGallery(vehicleId: String) {
        viewModelScope.launch {
            // Reset upload success state on refresh to avoid toast repetition
            _galleryState.update { it.copy(isLoading = true, error = null, multiUploadSuccess = false) }
            repository.getVehicleGallery(vehicleId)
                .onSuccess { galleryResponse -> _galleryState.update { it.copy(isLoading = false, gallery = galleryResponse) } }
                .onFailure { error -> _galleryState.update { it.copy(isLoading = false, error = error.message) } }
        }
    }

    fun onGalleryEventConsumed() {
        _galleryState.update { it.copy(multiUploadSuccess = false, error = null) }
    }
    fun uploadVehicleGallerySingleImage(file: File) {
        viewModelScope.launch {
            _galleryState.update { it.copy(isLoading = true, error = null, singleUploadResult = null) }
            repository.uploadVehicleGallerySingleImage(file)
                .onSuccess { uploadResponse -> _galleryState.update { it.copy(isLoading = false, singleUploadResult = uploadResponse) } }
                .onFailure { error -> _galleryState.update { it.copy(isLoading = false, error = error.message) } }
        }
    }

    fun uploadVehicleGalleryImages(vehicleId: String, files: List<File>) {
        viewModelScope.launch {
            val currentImages = _galleryState.value.gallery?.images ?: emptyList()

            _galleryState.update { it.copy(isLoading = true, error = null, multiUploadSuccess = false) }
            repository.uploadVehicleGalleryImages(vehicleId, files, currentImages)
                .onSuccess {
                    _galleryState.update { it.copy(isLoading = false, multiUploadSuccess = true) }
                    getVehicleGallery(vehicleId) // Refresh
                }
                .onFailure { error -> _galleryState.update { it.copy(isLoading = false, error = error.message) } }
        }
    }


    fun deleteGalleryImage(vehicleId: String, imageUrl: String) {
        viewModelScope.launch {
            _galleryState.update { it.copy(isLoading = true, error = null) }
            repository.deleteVehicleGalleryImage(vehicleId, imageUrl)
                .onSuccess {
                    // Refresh gallery after deletion
                    getVehicleGallery(vehicleId)
                }
                .onFailure { error ->
                    _galleryState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    fun deleteGalleryImages(vehicleId: String, images: List<String>) {
        viewModelScope.launch {
            _galleryState.update { it.copy(isLoading = true, error = null) }
            repository.deleteVehicleGalleryImages(vehicleId, images)
                .onSuccess {
                    getVehicleGallery(vehicleId)
                }
                .onFailure { error ->
                    _galleryState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    fun clearVehicleDetailState() {
        _vehicleDetailState.value = VehicleDetailState()
    }

    // ---  MAPPER IMPLEMENTATIONS ---
    private val drivetrainMap = mapOf(
        "4wd" to 10, "4x2" to 14, "4x4" to 13, "all wheel drive" to 8, "awd" to 4,
        "four wheel drive" to 7, "front wheel drive" to 6, "fwd" to 5, "new drain trains" to 11,
        "rear wheel drive" to 9, "unknown" to 2
    )
    private val drivetrainReverse = drivetrainMap.entries.associate { (k, v) -> v to k.capitalize() }

    private val bodyTypeMap = mapOf(
        "abc body type" to 17, "chassis" to 13, "convertible" to 3, "crossover" to 12, "full-size" to 19,
        "hatchback" to 4, "minivan" to 10, "pick up" to 6, "pickup" to 16, "sedan" to 9, "suv" to 8,
        "testhjk" to 15, "truck" to 11, "tzy kjsd kjdd" to 18, "unknown" to 1, "van" to 7, "wagon" to 5
    )
    private val bodyTypeReverse = bodyTypeMap.entries.associate { (k, v) -> v to k.capitalize() }

    private val transmissionMap = mapOf(
        "10-speed automatic w/od" to 33, "10-speed shiftable automatic" to 18, "1-speed direct drive" to 23,
        "2-speed" to 30, "2-speed automated manual" to 37, "2-speed shiftable automatic" to 36,
        "3-speed automatic" to 22, "4-speed automatic" to 4, "4-speed automatic | 5-speed automatic" to 34,
        "4-speed manual" to 26, "4-speed shiftable automatic" to 17, "5-speed automated manual" to 32,
        "5-speed automatic" to 13, "5-speed manual" to 7, "5-speed shiftable automatic" to 1,
        "6-speed automatic" to 19, "6-speed manual" to 9, "6-speed shiftable automatic" to 11,
        "7-speed automated manual" to 16, "7-speed automatic" to 31, "7-speed manual" to 21,
        "7-speed shiftable automatic" to 24, "8-speed automated manual" to 3, "8-speed automatic" to 25,
        "8-speed shiftable automatic" to 20, "9-speed automated manual" to 15, "9-speed automatic" to 28,
        "9-speed shiftable automatic" to 14, "automatic" to 10, "automatic w/manual shift" to 2,
        "continually variable transmission (cvt)" to 5, "continuously variable-speed automatic" to 12,
        "continuously variable-speed shiftable automatic" to 29, "electrically variable-speed automatic" to 27,
        "manual" to 6, "test transmkission" to 40, "unknown" to 39
    )
    private val transmissionReverse = transmissionMap.entries.associate { (k, v) -> v to k.capitalize() }

    private val fuelTypeMap = mapOf(
        "cng" to 3, "diesel" to 8, "diesel hybrid" to 11, "electric" to 4, "ethanol" to 7,
        "flex fuel" to 2, "gas" to 1, "gas/electric hybrid" to 16, "gasoline" to 6,
        "hybrid" to 9, "hydrogen fuel cell" to 12, "petrol" to 10, "plug-in gas/electric hybrid" to 17,
        "propane" to 13, "test fuelsds" to 18, "unknown" to 14
    )
    private val fuelTypeReverse = fuelTypeMap.entries.associate { (k, v) -> v to k.capitalize() }

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

    // --- Mapper helper functions ---
    private fun normalizeKey(s: String?) = s?.trim()?.lowercase() ?: ""
    private fun getDrivetrainId(name: String): Int = drivetrainMap[normalizeKey(name)] ?: 2
    private fun getBodyTypeId(name: String): Int = bodyTypeMap[normalizeKey(name)] ?: 1
    private fun getTransmissionId(name: String): Int = transmissionMap[normalizeKey(name)] ?: 39
    private fun getFuelTypeId(name: String): Int = fuelTypeMap[normalizeKey(name)] ?: 14
    private fun getMakeId(name: String): Int = makeMap[normalizeKey(name)] ?: 0
    private val makeReverse = makeMap.entries.associate { (k, v) -> v to k.replaceFirstChar { it.titlecase() } }

    private fun getDrivetrainString(id: Int): String = drivetrainReverse[id] ?: ""
    private fun getBodyTypeString(id: Int): String = bodyTypeReverse[id] ?: ""
    private fun getTransmissionString(id: Int): String = transmissionReverse[id] ?: ""
    private fun getFuelTypeString(id: Int): String = fuelTypeReverse[id] ?: ""
    private fun getMakeString(id: Int): String = makeReverse[id] ?: ""

    // --- Options for UI dropdowns ---
    val makeOptions: List<String> = makeMap.keys.filter { it.isNotBlank() }.map { it.replaceFirstChar { char -> char.titlecase() } }.sorted()
    val drivetrainOptions: List<String> = drivetrainMap.keys.filter { it.isNotBlank() }.map { it.replaceFirstChar { char -> char.titlecase() } }.sorted()
    val bodyTypeOptions: List<String> = bodyTypeMap.keys.filter { it.isNotBlank() }.map { it.replaceFirstChar { char -> char.titlecase() } }.sorted()
    val transmissionOptions: List<String> = transmissionMap.keys.filter { it.isNotBlank() }.map { it.replaceFirstChar { char -> char.titlecase() } }.sorted()
    val fuelTypeOptions: List<String> = fuelTypeMap.keys.filter { it.isNotBlank() }.map { it.replaceFirstChar { char -> char.titlecase() } }.sorted()
    val colorOptions = listOf("Black", "White", "Silver", "Gray", "Beige", "Red", "Blue") // Example
}

