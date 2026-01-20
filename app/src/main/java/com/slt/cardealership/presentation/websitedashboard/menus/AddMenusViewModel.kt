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

sealed class AddMenuUiState {
    object Loading : AddMenuUiState()
    data class Success(
        val pages: List<DomainPage> = emptyList(),
        val isSaved: Boolean = false
    ) : AddMenuUiState()
    data class Error(val message: String) : AddMenuUiState()
}

@HiltViewModel
class AddMenusViewModel @Inject constructor(
    private val repository: DealerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddMenuUiState>(AddMenuUiState.Loading)
    val uiState: StateFlow<AddMenuUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(AddMenuFormState())
    val formState: StateFlow<AddMenuFormState> = _formState.asStateFlow()

    fun fetchData(domainId: Int) {
        viewModelScope.launch {
            _uiState.value = AddMenuUiState.Loading
            try {
                // Fetch Pages
                val pagesResult = repository.getDomainPages(page = 1, itemsPerPage = 100, domainId = domainId)
                val pages = pagesResult.getOrNull()?.list ?: emptyList()
                _uiState.value = AddMenuUiState.Success(pages = pages)
            } catch (e: Exception) {
                _uiState.value = AddMenuUiState.Error(e.message ?: "Unknown error")
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
                menuLabel = page.pageName,
                customUrl = null,
                pageSlug = page.pageSlug,
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

    fun saveMenu(domainId: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value
            _uiState.value = AddMenuUiState.Loading

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
                val result = repository.createDomainMenu(request)

                if (result.isSuccess) {
                    val pages = (currentState as? AddMenuUiState.Success)?.pages ?: emptyList()
                    _uiState.value = AddMenuUiState.Success(pages = pages, isSaved = true)
                } else {
                    _uiState.value = AddMenuUiState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                _uiState.value = AddMenuUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

data class AddMenuFormState(
    val menuName: String = "",
    val isTopPrimary: Boolean = false,
    val isFooter: Boolean = false,
    val isFooterBottom: Boolean = false,
    val menuItems: List<com.slt.cardealership.domain.model.DomainMenuItem> = emptyList(),
    val isAddDialogVisible: Boolean = false,
    val selectedPageIds: Set<String> = emptySet()
)
