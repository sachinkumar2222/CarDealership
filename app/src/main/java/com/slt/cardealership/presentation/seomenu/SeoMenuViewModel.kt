package com.slt.cardealership.presentation.seomenu

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.data.local.SessionManager
import com.slt.cardealership.domain.model.SeoCategory
import com.slt.cardealership.domain.model.SeoMenu
import com.slt.cardealership.domain.model.SeoMenuPayload
import com.slt.cardealership.domain.model.SeoMenuRequest
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
import java.util.UUID
import javax.inject.Inject

/**
 * A UI-friendly data class that combines the SeoMenu data
 * with the human-readable category name.
 */
data class SeoMenuUiItem(
    // We use a local ID (UUID) to track items in the UI,
    // especially new ones that don't have a server ID yet.
    val localId: String = UUID.randomUUID().toString(),
    val categoryId: Int,
    val categoryName: String,
    val label: String,
    val url: String,
    val target: String
)

data class AddSeoMenuUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val allCategories: List<SeoCategory> = emptyList(),
    // Form Fields
    val menuLabel: String = "",
    val menuUrl: String = "",
    val selectedTarget: String = "_self",
    val selectedCategory: SeoCategory? = null
)

/**
 * Represents the state of the SeoMenuScreen.
 */
data class SeoMenuUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    // This list holds all available categories for the dropdowns
    val allCategories: List<SeoCategory> = emptyList(),
    // This is the list of menus the user is actively editing
    val menuItems: List<SeoMenuUiItem> = emptyList()
)

/**
 * One-time events for the UI to consume (e.g., showing Toasts).
 * (This re-uses the same pattern from your other ViewModels)
 */
sealed interface SeoMenuEvent {
    data class ShowToast(val message: String) : SeoMenuEvent
    object SaveAddSuccessAndNavBack : SeoMenuEvent// For the add screen
}

@HiltViewModel
class SeoMenuViewModel @Inject constructor(
    private val repository: DealerRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeoMenuUiState())
    val uiState: StateFlow<SeoMenuUiState> = _uiState.asStateFlow()

    private val _addMenuUiState = MutableStateFlow(AddSeoMenuUiState())
    val addMenuUiState: StateFlow<AddSeoMenuUiState> = _addMenuUiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<SeoMenuEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    // A list of valid "target" values for the dropdown
    val menuTargets = listOf("_self", "_blank", "_parent", "_top")

    private var dealerId: Int? = null

    init {
        viewModelScope.launch {
            // Get the dealerId once from the session
            dealerId = sessionManager.getDealerId()
            if (dealerId == null) {
                Log.e("SeoMenuViewModel", "Dealer ID is null, cannot proceed.")
                _uiState.value = SeoMenuUiState(error = "Dealer ID not found.")
                return@launch
            }
            loadSeoMenuData(dealerId!!.toLong())
        }
    }

    // ===================================================
    // --- NEW FUNCTIONS FOR THE "ADD" SCREEN ---
    // ===================================================

    /**
     * Call this from AddSeoMenuScreen's init (LaunchedEffect)
     * to populate the category dropdown.
     */
    fun loadAddScreenData() {
        // Only load if categories aren't already available from the list screen
        if (_uiState.value.allCategories.isNotEmpty()) {
            _addMenuUiState.value = AddSeoMenuUiState(
                allCategories = _uiState.value.allCategories,
                selectedCategory = _uiState.value.allCategories.firstOrNull(),
                selectedTarget = menuTargets.first()
            )
            return
        }

        // If categories are empty, fetch them
        _addMenuUiState.value = AddSeoMenuUiState(isLoading = true)
        viewModelScope.launch {
            repository.getSeoCategories()
                .onSuccess { categories ->
                    _addMenuUiState.value = AddSeoMenuUiState(
                        isLoading = false,
                        allCategories = categories,
                        selectedCategory = categories.firstOrNull(),
                        selectedTarget = menuTargets.first()
                    )
                }
                .onFailure {
                    _addMenuUiState.value = AddSeoMenuUiState(
                        isLoading = false,
                        error = it.message ?: "Failed to load categories"
                    )
                }
        }
    }

    // --- Functions to update state from the "Add" screen UI ---
    fun updateAddFormLabel(label: String) {
        _addMenuUiState.value = _addMenuUiState.value.copy(menuLabel = label)
    }

    fun updateAddFormUrl(url: String) {
        _addMenuUiState.value = _addMenuUiState.value.copy(menuUrl = url)
    }

    fun updateAddFormCategory(category: SeoCategory) {
        _addMenuUiState.value = _addMenuUiState.value.copy(selectedCategory = category)
    }

    fun updateAddFormTarget(target: String) {
        _addMenuUiState.value = _addMenuUiState.value.copy(selectedTarget = target)
    }

    /**
     * Fetches both the categories and the current menus in parallel.
     */
    fun loadSeoMenuData(dealerId: Long) {
        _uiState.value = SeoMenuUiState(isLoading = true)
        viewModelScope.launch {
            try {
                // Fetch categories and menus in parallel for efficiency
                val (categoriesResult, menusResult) = coroutineScope {
                    val categories = async { repository.getSeoCategories() }
                    val menus = async { repository.getSeoMenus(dealerId) }
                    Pair(categories.await(), menus.await())
                }

                // Handle categories result
                val allCategories = categoriesResult.getOrThrow()

                // Handle menus result
                val currentMenus = menusResult.getOrThrow()

                // Create a map for quick category name lookup
                val categoryMap = allCategories.associateBy { it.id }

                // Convert the server's SeoMenu list into our UI-friendly list
                val uiItems = currentMenus.map { menu ->
                    SeoMenuUiItem(
                        localId = menu.id, // Use the server's UUID as the local ID
                        categoryId = menu.seoCategoryId,
                        categoryName = categoryMap[menu.seoCategoryId]?.name ?: "Unknown",
                        label = menu.menuLabel,
                        url = menu.menuUrl,
                        target = menu.menuTarget
                    )
                }

                _uiState.value = SeoMenuUiState(
                    isLoading = false,
                    allCategories = allCategories,
                    menuItems = uiItems
                )

            } catch (e: Exception) {
                Log.e("SeoMenuViewModel", "Error loading data", e)
                _uiState.value = SeoMenuUiState(error = e.message ?: "An unknown error occurred")
            }
        }
    }

    fun refreshSeoMenus() {
        dealerId?.let { id ->
            loadSeoMenuData(id.toLong())
        }
    }

    /**
     * Adds a new, blank menu item to the end of the list.
     */
    fun addNewMenuItem() {
        val currentState = _uiState.value
        // Use the first category as a default
        val defaultCategory = currentState.allCategories.firstOrNull()

        val newItem = SeoMenuUiItem(
            categoryId = defaultCategory?.id ?: -1, // Use -1 or a placeholder if no categories
            categoryName = defaultCategory?.name ?: "Select Category",
            label = "",
            url = "",
            target = menuTargets.first() // Default to "_self"
        )

        _uiState.value = currentState.copy(
            menuItems = currentState.menuItems + newItem
        )
    }

    /**
     * Removes a menu item from the list based on its unique localId.
     */
    fun removeMenuItem(localId: String) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            menuItems = currentState.menuItems.filterNot { it.localId == localId }
        )
    }

    /**
     * Updates a specific field of a menu item at a given index.
     * This is a generic way to handle updates from any TextField or Dropdown.
     */
    fun updateMenuItem(index: Int, update: (SeoMenuUiItem) -> SeoMenuUiItem) {
        val currentState = _uiState.value
        if (index < 0 || index >= currentState.menuItems.size) {
            Log.w("SeoMenuViewModel", "Invalid index $index for updateMenuItem")
            return
        }

        val updatedList = currentState.menuItems.toMutableList()
        val oldItem = updatedList[index]
        updatedList[index] = update(oldItem)

        _uiState.value = currentState.copy(menuItems = updatedList)
    }

    /**
     * Saves the entire list of menus to the server.
     */
    fun saveSeoMenus() {
        val currentDealerId = dealerId
        if (currentDealerId == null) {
            viewModelScope.launch { _eventFlow.emit(SeoMenuEvent.ShowToast("Error: Dealer ID not found")) }
            return
        }

        val currentState = _uiState.value
        _uiState.value = currentState.copy(isSaving = true)

        viewModelScope.launch {
            try {
                // Convert our UI list back into the POST request payload
                val payloadList = currentState.menuItems.map { uiItem ->
                    SeoMenuPayload(
                        seo_category_id = uiItem.categoryId,
                        menu_label = uiItem.label,
                        menu_url = uiItem.url,
                        menu_target = uiItem.target
                    )
                }
                val request = SeoMenuRequest(list = payloadList)

                // Make the API call
                repository.saveSeoMenus(currentDealerId.toLong(), request).getOrThrow()

                // On success, show toast and refresh
                _uiState.value = _uiState.value.copy(isSaving = false)
                _eventFlow.emit(SeoMenuEvent.ShowToast("SEO Menus saved successfully!"))
                loadSeoMenuData(currentDealerId.toLong())

            } catch (e: Exception) {
                Log.e("SeoMenuViewModel", "Failed to save SEO menus", e)
                _uiState.value = _uiState.value.copy(isSaving = false)
                _eventFlow.emit(SeoMenuEvent.ShowToast("Error: ${e.message}"))
            }
        }
    }

    fun saveNewMenuItem() {
        val currentDealerId = dealerId
        if (currentDealerId == null) {
            viewModelScope.launch { _eventFlow.emit(SeoMenuEvent.ShowToast("Error: Dealer ID not found")) }
            return
        }

        val currentState = _addMenuUiState.value

        // --- Validation ---
        if (currentState.menuLabel.isBlank()) {
            viewModelScope.launch { _eventFlow.emit(SeoMenuEvent.ShowToast("Menu Label cannot be empty.")) }
            return
        }
        if (currentState.menuUrl.isBlank()) {
            viewModelScope.launch { _eventFlow.emit(SeoMenuEvent.ShowToast("Menu URL cannot be empty.")) }
            return
        }
        if (currentState.selectedCategory == null) {
            viewModelScope.launch { _eventFlow.emit(SeoMenuEvent.ShowToast("Please select a category.")) }
            return
        }

        _addMenuUiState.value = currentState.copy(isSaving = true)
        viewModelScope.launch {
            try {
                // 1. GET the current list of menus
                val oldMenuList = repository.getSeoMenus(currentDealerId.toLong()).getOrThrow()

                // 2. Convert old list to payload
                val oldPayload = oldMenuList.map {
                    SeoMenuPayload(
                        seo_category_id = it.seoCategoryId,
                        menu_label = it.menuLabel,
                        menu_target = it.menuTarget,
                        menu_url = it.menuUrl
                    )
                }

                // 3. Create the new item from our UI state
                val newPayloadItem = SeoMenuPayload(
                    seo_category_id = currentState.selectedCategory.id,
                    menu_label = currentState.menuLabel,
                    menu_url = currentState.menuUrl,
                    menu_target = currentState.selectedTarget
                )

                // 4. Combine them into a new list
                val newCompleteList = oldPayload + newPayloadItem
                val request = SeoMenuRequest(list = newCompleteList)

                // 5. POST the new complete list
                repository.saveSeoMenus(currentDealerId.toLong(), request).getOrThrow()

                // 6. Success
                _addMenuUiState.value = _addMenuUiState.value.copy(isSaving = false)
                _eventFlow.emit(SeoMenuEvent.ShowToast("Menu item added successfully!"))

                // 7. Refresh the main list
                loadSeoMenuData(currentDealerId.toLong())

                // 8. Tell the "Add" screen to navigate back
                _eventFlow.emit(SeoMenuEvent.SaveAddSuccessAndNavBack)

            } catch (e: Exception) {
                Log.e("SeoMenuViewModel", "Failed to save new menu item", e)
                _addMenuUiState.value = _addMenuUiState.value.copy(isSaving = false)
                _eventFlow.emit(SeoMenuEvent.ShowToast("Error: ${e.message}"))
            }
        }
    }

}