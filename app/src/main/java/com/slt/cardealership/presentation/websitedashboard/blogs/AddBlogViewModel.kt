package com.slt.cardealership.presentation.websitedashboard.blogs

import com.slt.cardealership.presentation.navigation.HomeRoutes

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.slt.cardealership.domain.model.BlogCta
import com.slt.cardealership.domain.model.BlogResearchCompare
import com.slt.cardealership.domain.model.ResearchMake
import com.slt.cardealership.domain.model.ResearchModel
import com.slt.cardealership.domain.model.ResearchTrim
import com.slt.cardealership.domain.model.ResearchYear
import com.slt.cardealership.domain.model.ManageUsers
import com.slt.cardealership.domain.repo.DealerRepository
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
// Removed wrong import
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import com.slt.cardealership.data.local.SessionManager

data class AddBlogUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    
    // Form Fields
    val title: String = "",
    val slug: String = "",
    val description: String = "",
    val metaTitle: String = "",
    val metaDescription: String = "",
    val shortDescription: String = "",
    val blogType: String = "general", // general, research, compare
    val status: String = "published", // published, draft, revision
    val selectedImageUri: Uri? = null,
    val featuredImageUrl: String? = null,
    
    // CTAs
    val ctas: List<BlogCta> = emptyList(),
    
    // Categories
    val categories: List<com.slt.cardealership.domain.model.BlogCategory> = emptyList(),
    val selectedCategoryIds: Set<String> = emptySet(),
    
    // Authors (Users)
    val authors: List<com.slt.cardealership.domain.model.ManageUsers> = emptyList(),
    val selectedAuthorId: Long? = null,
    
    // Research Data
    val makes: List<ResearchMake> = emptyList(),
    val models: List<ResearchModel> = emptyList(),
    val years: List<ResearchYear> = emptyList(),
    val trims: List<ResearchTrim> = emptyList(),
    
    // Selected Research Items
    val selectedMake: ResearchMake? = null,
    val selectedModel: ResearchModel? = null,
    val selectedYear: ResearchYear? = null,
    val selectedTrim: ResearchTrim? = null,
    
    // For "Compare" type
    val researchComparisons: List<BlogResearchCompare> = emptyList(),
    
    // Dialog State
    val isVehicleDialogOpen: Boolean = false
)

@HiltViewModel
class AddBlogViewModel @Inject constructor(
    private val dealerRepository: DealerRepository,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val domainId: Int = savedStateHandle.toRoute<HomeRoutes.AddBlogScreen>().domainId
    private val blogId: String? = savedStateHandle.toRoute<HomeRoutes.AddBlogScreen>().blogId

    private val _uiState = MutableStateFlow(AddBlogUiState())
    val uiState: StateFlow<AddBlogUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val makesJob = launch { fetchMakes() }
            val categoriesJob = launch { fetchCategories() }
            val authorsJob = launch { fetchAuthors() }
            
            // Wait for dependencies to load before fetching details
            makesJob.join()
            categoriesJob.join()
            authorsJob.join()

            if (blogId != null) {
                fetchBlogDetails(blogId)
            }
        }
    }

    fun onEvent(event: AddBlogEvent) {
        when (event) {
            is AddBlogEvent.TitleChanged -> _uiState.update { it.copy(title = event.title) }
            is AddBlogEvent.SlugChanged -> _uiState.update { it.copy(slug = event.slug) }
            is AddBlogEvent.DescriptionChanged -> _uiState.update { it.copy(description = event.description) }
            is AddBlogEvent.MetaTitleChanged -> _uiState.update { it.copy(metaTitle = event.metaTitle) }
            is AddBlogEvent.MetaDescriptionChanged -> _uiState.update { it.copy(metaDescription = event.metaDescription) }
            is AddBlogEvent.ShortDescriptionChanged -> _uiState.update { it.copy(shortDescription = event.shortDescription) }
            is AddBlogEvent.BlogTypeChanged -> {
                _uiState.update { it.copy(blogType = event.blogType) }
                // Reset selections when type changes? Maybe not strictly necessary but cleaner
            }
            is AddBlogEvent.ImageSelected -> _uiState.update { it.copy(selectedImageUri = event.uri) }
            
            is AddBlogEvent.MakeSelected -> {
                _uiState.update { it.copy(selectedMake = event.make, selectedModel = null, selectedYear = null, selectedTrim = null, models = emptyList(), years = emptyList(), trims = emptyList()) }
                fetchModels(event.make.id)
            }
            is AddBlogEvent.ModelSelected -> {
                _uiState.update { it.copy(selectedModel = event.model, selectedYear = null, selectedTrim = null, years = emptyList(), trims = emptyList()) }
                fetchYears(_uiState.value.selectedMake!!.id, event.model.id)
            }
            is AddBlogEvent.YearSelected -> {
                _uiState.update { it.copy(selectedYear = event.year, selectedTrim = null, trims = emptyList()) }
                if (_uiState.value.blogType == "compare") {
                    fetchTrims(_uiState.value.selectedMake!!.id, _uiState.value.selectedModel!!.id, event.year.year)
                }
            }
            is AddBlogEvent.TrimSelected -> _uiState.update { it.copy(selectedTrim = event.trim) }
            
            is AddBlogEvent.ToggleVehicleDialog -> _uiState.update { it.copy(isVehicleDialogOpen = event.isOpen) }
            is AddBlogEvent.AddResearchItem -> addResearchItem()
            
            // CTA Events
            is AddBlogEvent.AddCta -> _uiState.update { it.copy(ctas = it.ctas + BlogCta("", "")) }
            is AddBlogEvent.UpdateCta -> updateCta(event.index, event.cta)
            is AddBlogEvent.RemoveCta -> removeCta(event.index)
            
            // Category Events
            is AddBlogEvent.ToggleCategory -> toggleCategory(event.categoryId)
            
            // Author Event
            is AddBlogEvent.AuthorSelected -> _uiState.update { it.copy(selectedAuthorId = event.authorId) }
            
            // Status Event
            is AddBlogEvent.StatusChanged -> _uiState.update { it.copy(status = event.status) }
            
            is AddBlogEvent.Submit -> submitBlog(event.file)
            is AddBlogEvent.ErrorDismissed -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun fetchMakes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            dealerRepository.getResearchMakes()
                .onSuccess { makes ->
                    _uiState.update { it.copy(isLoading = false, makes = makes) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    private fun fetchModels(makeId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            dealerRepository.getResearchModels(makeId)
                .onSuccess { models ->
                    _uiState.update { it.copy(isLoading = false, models = models) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    private fun fetchYears(makeId: Int, modelId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            dealerRepository.getResearchModelYears(makeId, modelId)
                .onSuccess { years ->
                    _uiState.update { it.copy(isLoading = false, years = years) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    private fun fetchTrims(makeId: Int, modelId: Int, year: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            dealerRepository.getResearchTrims(makeId, modelId, year)
                .onSuccess { trims ->
                    _uiState.update { it.copy(isLoading = false, trims = trims) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    private fun fetchCategories() {
        viewModelScope.launch {
            // Assuming fetchCategories exists in repo or using getDomainBlogCategories
            dealerRepository.getDomainBlogCategories(domainId)
                .onSuccess { response ->
                    _uiState.update { it.copy(categories = response.list) }
                }
                .onFailure {
                    // Log or handle error silently for now
                }
        }
    }

    private fun fetchAuthors() {
        viewModelScope.launch {
            // First, fetch user profile to get the correct dealer ID from the API
            dealerRepository.getUserAuthorization()
                .onSuccess { userProfile ->
                    val dealerId = userProfile.dealerId
                    android.util.Log.d("AddBlogViewModel", "fetchAuthors: dealerId from API = $dealerId")
                    
                    if (dealerId != null) {
                        android.util.Log.d("AddBlogViewModel", "Calling getAllUsers with dealerId: $dealerId")
                        dealerRepository.getAllUsers(dealerId)
                            .onSuccess { users ->
                                android.util.Log.d("AddBlogViewModel", "getAllUsers SUCCESS: ${users.size} users fetched")
                                
                                if (users.isEmpty()) {
                                    android.util.Log.d("AddBlogViewModel", "No users found for dealer. Using current user as fallback.")
                                    // Create a ManageUsers object from UserProfile
                                    val currentUser = ManageUsers(
                                        id = userProfile.userId,
                                        username = userProfile.username,
                                        first_name = userProfile.firstName,
                                        last_name = userProfile.lastName,
                                        phone = null, // UserProfile doesn't have phone field
                                        is_active = true,
                                        role_type = null,
                                        role_id = 0,
                                        role_name = null,
                                        organization_id = null,
                                        organization_name = null,
                                        designation_id = null,
                                        designation_name = null,
                                        department_id = null,
                                        department_name = null,
                                        group_id = null,
                                        group_name = null,
                                        created_by = null,
                                        updated_by = null,
                                        created_by_email = null,
                                        updated_by_email = null,
                                        created_on = null,
                                        updated_on = null,
                                        last_logged_in = null,
                                        image_url = userProfile.profilePic,
                                        doj = null,
                                        dob = null,
                                        gender = null,
                                        language = null,
                                        dealer_id = userProfile.dealerId,
                                        dealer_name = userProfile.dealerName,
                                        address = null
                                    )
                                    _uiState.update { it.copy(authors = listOf(currentUser), selectedAuthorId = currentUser.id) }
                                } else {
                                    users.forEach { user ->
                                        android.util.Log.d("AddBlogViewModel", "User: id=${user.id}, username=${user.username}, first_name=${user.first_name}, last_name=${user.last_name}")
                                    }
                                    _uiState.update { it.copy(authors = users) }
                                }
                            }
                            .onFailure { error ->
                                android.util.Log.e("AddBlogViewModel", "getAllUsers FAILED: ${error.message}", error)
                                _uiState.update { it.copy(error = "Failed to load authors: ${error.message}") }
                            }
                    } else {
                        android.util.Log.e("AddBlogViewModel", "Dealer ID from API is NULL!")
                        _uiState.update { it.copy(error = "Dealer ID not found in user profile") }
                    }
                }
                .onFailure { error ->
                    android.util.Log.e("AddBlogViewModel", "Failed to fetch user authorization: ${error.message}", error)
                    _uiState.update { it.copy(error = "Failed to load user profile: ${error.message}") }
                }
        }
    }


    private fun updateCta(index: Int, cta: BlogCta) {
        val currentCtas = _uiState.value.ctas.toMutableList()
        if (index in currentCtas.indices) {
            currentCtas[index] = cta
            _uiState.update { it.copy(ctas = currentCtas) }
        }
    }

    private fun removeCta(index: Int) {
        val currentCtas = _uiState.value.ctas.toMutableList()
        if (index in currentCtas.indices) {
            currentCtas.removeAt(index)
            _uiState.update { it.copy(ctas = currentCtas) }
        }
    }

    private fun toggleCategory(categoryId: String) {
        val currentSelected = _uiState.value.selectedCategoryIds.toMutableSet()
        if (currentSelected.contains(categoryId)) {
            currentSelected.remove(categoryId)
        } else {
            currentSelected.add(categoryId)
        }
        _uiState.update { it.copy(selectedCategoryIds = currentSelected) }
    }

    private fun addResearchItem() {
        val state = _uiState.value
        if (state.selectedMake != null && state.selectedModel != null && state.selectedYear != null) {
            val newItem = BlogResearchCompare(
                makeName = state.selectedMake.name,
                makeSlug = state.selectedMake.slug,
                modelName = state.selectedModel.name,
                modelSlug = state.selectedModel.slug,
                trimName = state.selectedTrim?.name ?: "",
                trimSlug = state.selectedTrim?.slug ?: "",
                year = state.selectedYear.year
            )
            _uiState.update { 
                it.copy(
                    researchComparisons = it.researchComparisons + newItem,
                    // Reset selections after adding?
                    selectedMake = null, selectedModel = null, selectedYear = null, selectedTrim = null,
                    isVehicleDialogOpen = false // Close dialog after adding
                ) 
            }
        }
    }

    private fun submitBlog(file: File?) {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update { it.copy(isLoading = true) }

            val gson = Gson()
            val ctaJson = gson.toJson(state.ctas)
            val researchCompareJson = gson.toJson(state.researchComparisons)
            
            // Construct category JSON list of objects {id, name, slug}
            val selectedCategories = state.categories.filter { state.selectedCategoryIds.contains(it.id) }
            val categoryList = selectedCategories.map { 
                mapOf("id" to it.id, "name" to it.name, "slug" to it.slug) 
            }
            val categoryJson = gson.toJson(categoryList) 

            // Validation
            if (state.shortDescription.isBlank()) {
                _uiState.update { it.copy(isLoading = false, error = "Short description is required.") }
                return@launch
            }

            // Determine Author ID
            var authorId = state.selectedAuthorId
            android.util.Log.d("AddBlogViewModel", "submitBlog: selectedAuthorId from UI = $authorId")
            
            if (authorId == null) {
                 _uiState.update { it.copy(isLoading = false, error = "Please select an author.") }
                 return@launch
            }


            android.util.Log.d("AddBlogViewModel", "submitBlog: Final authorId to be used = $authorId")
            if (authorId != null) {
                if (blogId != null) {
                    // Update existing blog
                    dealerRepository.updateDomainBlog(
                        blogId = blogId,
                        title = state.title,
                        slug = state.slug,
                        description = state.description,
                        metaTitle = state.metaTitle,
                        metaDescription = state.metaDescription,
                        blogType = state.blogType,
                        status = state.status,
                        domainId = domainId,
                        cta = ctaJson,
                        researchCompare = researchCompareJson,
                        category = categoryJson,
                        shortDescription = state.shortDescription,
                        createdBy = authorId,
                        updatedBy = authorId,
                        file = file
                    ).onSuccess {
                        _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    }.onFailure { error ->
                        _uiState.update { it.copy(isLoading = false, error = error.message) }
                    }
                } else {
                    // Create new blog
                    dealerRepository.createDomainBlog(
                        title = state.title,
                        slug = state.slug,
                        description = state.description,
                        metaTitle = state.metaTitle,
                        metaDescription = state.metaDescription,
                        blogType = state.blogType,
                        status = state.status,
                        domainId = domainId,
                        cta = ctaJson,
                        researchCompare = researchCompareJson,
                        category = categoryJson,
                        shortDescription = state.shortDescription,
                        createdBy = authorId,
                        updatedBy = authorId,
                        file = file
                    ).onSuccess {
                        _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    }.onFailure { error ->
                        _uiState.update { it.copy(isLoading = false, error = error.message) }
                    }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Failed to identify author. Please select an author.") }
            }
        }
    }
    private fun fetchBlogDetails(blogId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            dealerRepository.getDomainBlogDetails(blogId)
                .onSuccess { details ->
                    android.util.Log.d("AddBlogViewModel", "fetchBlogDetails: details fetched. title=${details.title}")
                    android.util.Log.d("AddBlogViewModel", "fetchBlogDetails: shortDescription=${details.shortDescription}")
                    android.util.Log.d("AddBlogViewModel", "fetchBlogDetails: featuredImage=${details.featuredImage}")
                    android.util.Log.d("AddBlogViewModel", "fetchBlogDetails: categories=${details.categories?.map { it.slug }}")
                    
                    _uiState.update { state ->
                        // Map Author by Email
                        val authorEmail = details.createdBy
                        val authorId = state.authors.find { it.username == authorEmail }?.id
                        android.util.Log.d("AddBlogViewModel", "fetchBlogDetails: mapping author. email=$authorEmail, foundId=$authorId")

                        // Map Categories by Slug
                        val detailCategorySlugs = details.categories?.map { it.slug }?.toSet() ?: emptySet()
                        val selectedCategoryIds = state.categories
                            .filter { detailCategorySlugs.contains(it.slug) }
                            .map { it.id }
                            .toSet()
                        android.util.Log.d("AddBlogViewModel", "fetchBlogDetails: mapping categories. detailSlugs=$detailCategorySlugs, stateCategories=${state.categories.size}, mappedIds=$selectedCategoryIds")

                        state.copy(
                            isLoading = false,
                            title = details.title,
                            slug = details.slug,
                            description = details.description,
                            metaTitle = details.metaTitle ?: "",
                            metaDescription = details.metaDescription ?: "",
                            blogType = details.blogType,
                            status = details.status,
                            ctas = details.blogCta ?: emptyList(),
                            researchComparisons = details.researchCompareData ?: emptyList(),
                            shortDescription = details.shortDescription ?: "",
                            featuredImageUrl = details.featuredImage,
                            selectedCategoryIds = selectedCategoryIds,
                            selectedAuthorId = authorId
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }
}

sealed class AddBlogEvent {
    data class TitleChanged(val title: String) : AddBlogEvent()
    data class SlugChanged(val slug: String) : AddBlogEvent()
    data class DescriptionChanged(val description: String) : AddBlogEvent()
    data class MetaTitleChanged(val metaTitle: String) : AddBlogEvent()
    data class MetaDescriptionChanged(val metaDescription: String) : AddBlogEvent()
    data class ShortDescriptionChanged(val shortDescription: String) : AddBlogEvent()
    data class BlogTypeChanged(val blogType: String) : AddBlogEvent()
    data class ImageSelected(val uri: Uri) : AddBlogEvent()
    
    data class MakeSelected(val make: ResearchMake) : AddBlogEvent()
    data class ModelSelected(val model: ResearchModel) : AddBlogEvent()
    data class YearSelected(val year: ResearchYear) : AddBlogEvent()
    data class TrimSelected(val trim: ResearchTrim) : AddBlogEvent()
    
    data class ToggleVehicleDialog(val isOpen: Boolean) : AddBlogEvent()
    object AddResearchItem : AddBlogEvent()
    data class AddCta(val index: Int? = null) : AddBlogEvent() // index null means add new
    data class UpdateCta(val index: Int, val cta: BlogCta) : AddBlogEvent()
    data class RemoveCta(val index: Int) : AddBlogEvent()
    
    data class ToggleCategory(val categoryId: String) : AddBlogEvent()
    data class AuthorSelected(val authorId: Long) : AddBlogEvent()
    data class StatusChanged(val status: String) : AddBlogEvent()
    
    data class Submit(val file: File?) : AddBlogEvent()
    object ErrorDismissed : AddBlogEvent()
}
