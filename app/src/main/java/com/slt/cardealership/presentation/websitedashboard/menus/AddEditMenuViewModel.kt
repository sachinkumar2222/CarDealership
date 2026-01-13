package com.slt.cardealership.presentation.websitedashboard.menus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.domain.model.DomainMenuDetail
import com.slt.cardealership.domain.model.DomainPage
import com.slt.cardealership.domain.repo.DealerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AddEditMenuUiState {
    object Loading : AddEditMenuUiState()
    data class Success(
        val menu: DomainMenuDetail? = null,
        val pages: List<DomainPage> = emptyList()
    ) : AddEditMenuUiState()
    data class Error(val message: String) : AddEditMenuUiState()
}

@HiltViewModel
class AddEditMenuViewModel @Inject constructor(
    private val repository: DealerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddEditMenuUiState>(AddEditMenuUiState.Loading)
    val uiState: StateFlow<AddEditMenuUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(MenuFormState())
    val formState: StateFlow<MenuFormState> = _formState.asStateFlow()

    fun fetchData(domainId: Int, menuId: String?) {
        viewModelScope.launch {
            _uiState.value = AddEditMenuUiState.Loading
            try {
                // Fetch Pages
                val pagesResult = repository.getDomainPages(page = 1, itemsPerPage = 100, domainId = domainId)
                val pages = pagesResult.getOrNull()?.list ?: emptyList()

                // Fetch Menu Details if editing
                var menuDetail: DomainMenuDetail? = null
                if (menuId != null) {
                    val menuResult = repository.getDomainMenuDetails(menuId)
                    if (menuResult.isSuccess) {
                        menuDetail = menuResult.getOrNull()
                        menuDetail?.let {
                            _formState.value = MenuFormState(
                                menuName = it.menuName,
                                isTopPrimary = it.isTopPrimaryMenu,
                                isFooter = it.isFooterMenu,
                                isFooterBottom = it.isFooterBottomMenu,
                                menuItems = it.menuItems
                            )
                        }
                    } else {
                        _uiState.value = AddEditMenuUiState.Error("Failed to fetch menu details")
                        return@launch
                    }
                }

                _uiState.value = AddEditMenuUiState.Success(menu = menuDetail, pages = pages)

            } catch (e: Exception) {
                _uiState.value = AddEditMenuUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun onNameChange(newName: String) {
        _formState.value = _formState.value.copy(menuName = newName)
    }

    fun onTopPrimaryChange(isChecked: Boolean) {
        _formState.value = _formState.value.copy(isTopPrimary = isChecked)
    }

    fun onFooterChange(isChecked: Boolean) {
        _formState.value = _formState.value.copy(isFooter = isChecked)
    }

    fun onAddMenuItemClick() {
        _formState.value = _formState.value.copy(isAddDialogVisible = true)
    }

    fun onDismissAddDialog() {
        _formState.value = _formState.value.copy(isAddDialogVisible = false, selectedPageIds = emptySet())
    }

    fun onPageSelectionChange(pageId: String, isSelected: Boolean) {
        val currentSelected = _formState.value.selectedPageIds.toMutableSet()
        if (isSelected) {
            currentSelected.add(pageId)
        } else {
            currentSelected.remove(pageId)
        }
        _formState.value = _formState.value.copy(selectedPageIds = currentSelected)
    }

    fun onAddPagesToMenu(pages: List<DomainPage>) {
        val selectedPages = pages.filter { _formState.value.selectedPageIds.contains(it.id) }
        val newItems = selectedPages.map { page ->
            com.slt.cardealership.domain.model.DomainMenuItem(
                menuLabel = "page.title",
                customUrl = null,
                pageSlug = "page.slug",
                target = "_self",
                prms = null,
                childItems = emptyList()
            )
        }
        val currentItems = _formState.value.menuItems.toMutableList()
        currentItems.addAll(newItems)
        _formState.value = _formState.value.copy(
            menuItems = currentItems,
            isAddDialogVisible = false,
            selectedPageIds = emptySet()
        )
    }

    fun onAddCustomLinkToMenu(label: String, url: String, target: String) {
        val newItem = com.slt.cardealership.domain.model.DomainMenuItem(
            menuLabel = label,
            customUrl = url,
            pageSlug = null,
            target = target,
            prms = null,
            childItems = emptyList()
        )
        val currentItems = _formState.value.menuItems.toMutableList()
        currentItems.add(newItem)
        _formState.value = _formState.value.copy(
            menuItems = currentItems,
            isAddDialogVisible = false
        )
    }
}

data class MenuFormState(
    val menuName: String = "",
    val isTopPrimary: Boolean = false,
    val isFooter: Boolean = false,
    val isFooterBottom: Boolean = false,
    val menuItems: List<com.slt.cardealership.domain.model.DomainMenuItem> = emptyList(),
    val isAddDialogVisible: Boolean = false,
    val selectedPageIds: Set<String> = emptySet()
)
