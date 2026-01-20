package com.slt.cardealership.presentation.websitedashboard.menus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.domain.model.DomainMenuItem
import com.slt.cardealership.domain.model.DomainPage
import com.slt.cardealership.domain.repo.DealerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class EditMenuUiState {
    object Loading : EditMenuUiState()
    data class Success(
        val pages: List<DomainPage> = emptyList(),
        val isSaved: Boolean = false
    ) : EditMenuUiState()
    data class Error(val message: String) : EditMenuUiState()
}

@HiltViewModel
class EditMenusViewModel @Inject constructor(
    private val repository: DealerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<EditMenuUiState>(EditMenuUiState.Loading)
    val uiState: StateFlow<EditMenuUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(EditMenuFormState())
    val formState: StateFlow<EditMenuFormState> = _formState.asStateFlow()

    fun fetchData(domainId: Int, menuId: String) {
        viewModelScope.launch {
            _uiState.value = EditMenuUiState.Loading
            try {
                // Fetch Pages
                val pagesResult = repository.getDomainPages(page = 1, itemsPerPage = 100, domainId = domainId)
                val pages = pagesResult.getOrNull()?.list ?: emptyList()

                // Fetch Menu Details
                val menuResult = repository.getDomainMenuDetails(menuId)
                if (menuResult.isSuccess) {
                    val menuDetail = menuResult.getOrNull()
                    menuDetail?.let {
                        _formState.value = EditMenuFormState(
                            menuName = it.menuName,
                            isTopPrimary = it.isTopPrimaryMenu,
                            isFooter = it.isFooterMenu,
                            isFooterBottom = it.isFooterBottomMenu,
                            menuItems = it.menuItems
                        )
                    }
                    _uiState.value = EditMenuUiState.Success(pages = pages)
                } else {
                    _uiState.value = EditMenuUiState.Error("Failed to fetch menu details")
                }

            } catch (e: Exception) {
                _uiState.value = EditMenuUiState.Error(e.message ?: "Unknown error")
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

    fun onFooterBottomChange(isChecked: Boolean) {
        _formState.value = _formState.value.copy(isFooterBottom = isChecked)
    }

    fun initiateAddMenuItem(targetItem: DomainMenuItem?) {
        _formState.value = _formState.value.copy(
            isAddDialogVisible = true,
            targetItemForAdd = targetItem
        )
    }

    fun onDismissAddDialog() {
        _formState.value = _formState.value.copy(isAddDialogVisible = false, selectedPageIds = emptySet(), targetItemForAdd = null)
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

    fun updateMenuItem(index: Int, item: DomainMenuItem) {
        val currentItems = _formState.value.menuItems.toMutableList()
        if (index in currentItems.indices) {
            currentItems[index] = item
            _formState.value = _formState.value.copy(menuItems = currentItems)
        }
    }

    fun removeMenuItem(index: Int) {
        val currentItems = _formState.value.menuItems.toMutableList()
        if (index in currentItems.indices) {
            currentItems.removeAt(index)
            _formState.value = _formState.value.copy(menuItems = currentItems)
        }
    }

    fun onAddPagesToMenu(pages: List<DomainPage>) {
        val selectedPages = pages.filter { _formState.value.selectedPageIds.contains(it.id) }
        val newItems = selectedPages.map { page ->
            DomainMenuItem(
                menuLabel = page.pageName,
                customUrl = null,
                pageSlug = page.pageSlug,
                target = "_self",
                prms = null,
                childItems = emptyList()
            )
        }

        val target = _formState.value.targetItemForAdd
        val currentMenuItems = _formState.value.menuItems

        val updatedMenuItems = if (target == null) {
            currentMenuItems + newItems
        } else {
            addChildrenToTarget(currentMenuItems, target, newItems)
        }

        _formState.value = _formState.value.copy(
            menuItems = updatedMenuItems,
            isAddDialogVisible = false,
            selectedPageIds = emptySet(),
            targetItemForAdd = null
        )
    }

    fun onAddCustomLinkToMenu(label: String, url: String) {
        val newItem = DomainMenuItem(
            menuLabel = label,
            customUrl = url,
            pageSlug = null,
            target = "_self",
            prms = null,
            childItems = emptyList()
        )

        val target = _formState.value.targetItemForAdd
        val currentMenuItems = _formState.value.menuItems

        val updatedMenuItems = if (target == null) {
            currentMenuItems + listOf(newItem)
        } else {
            addChildrenToTarget(currentMenuItems, target, listOf(newItem))
        }

        _formState.value = _formState.value.copy(
            menuItems = updatedMenuItems,
            isAddDialogVisible = false,
            targetItemForAdd = null
        )
    }

    private fun addChildrenToTarget(
        items: List<DomainMenuItem>,
        target: DomainMenuItem,
        newChildren: List<DomainMenuItem>
    ): List<DomainMenuItem> {
        return items.map { item ->
            if (item == target) {
                item.copy(childItems = item.childItems + newChildren)
            } else {
                item.copy(childItems = addChildrenToTarget(item.childItems, target, newChildren))
            }
        }
    }

    fun saveMenu(domainId: Int, menuId: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            _uiState.value = EditMenuUiState.Loading

            val form = _formState.value
            val request = com.slt.cardealership.domain.model.DomainMenuRequest(
                sqlDomainId = domainId,
                menuName = form.menuName,
                isTopPrimaryMenu = form.isTopPrimary,
                isFooterMenu = form.isFooter,
                isFooterBottomMenu = form.isFooterBottom,
                menuItems = form.menuItems
            )

            try {
                val result = repository.updateDomainMenu(menuId, request)

                if (result.isSuccess) {
                    val pages = (currentState as? EditMenuUiState.Success)?.pages ?: emptyList()
                    _uiState.value = EditMenuUiState.Success(pages = pages, isSaved = true)
                } else {
                    _uiState.value = EditMenuUiState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                _uiState.value = EditMenuUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

data class EditMenuFormState(
    val menuName: String = "",
    val isTopPrimary: Boolean = false,
    val isFooter: Boolean = false,
    val isFooterBottom: Boolean = false,
    val menuItems: List<DomainMenuItem> = emptyList(),
    val isAddDialogVisible: Boolean = false,
    val selectedPageIds: Set<String> = emptySet(),
    val targetItemForAdd: DomainMenuItem? = null
)
