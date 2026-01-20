package com.slt.cardealership.presentation.ManageClassified

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.data.local.SessionManager
import com.slt.cardealership.domain.repo.DealerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class ClassifiedSite(
    val id: String,
    val name: String,
    val url: String,
    val articlesCount: Int,
    val bannersCount: Int = 0,
    val faqsCount: Int = 0
)

@HiltViewModel
class ManageClassifiedViewModel @Inject constructor(
    private val repository: DealerRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<ManageClassifiedUiState>(ManageClassifiedUiState.Loading)
    val uiState: StateFlow<ManageClassifiedUiState> = _uiState.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    fun onTabSelected(index: Int) {
        _selectedTab.value = index
        fetchData(index)
    }

    fun fetchData(tabIndex: Int = _selectedTab.value) {
        viewModelScope.launch {
            _uiState.value = ManageClassifiedUiState.Loading

            // Get Dealer ID from session
            val dealerId = sessionManager.getDealerId()?.toLong()
            if (dealerId == null) {
                _uiState.value = ManageClassifiedUiState.Error("Dealer ID not found")
                return@launch
            }

            // Determine Product Type ID based on Tab
            val productTypeId = when (tabIndex) {
                0 -> 6 // Classifieds
                1 -> 9 // Directory
                2 -> 15 // Service
                else -> null // Search Engines?
            }

            if (productTypeId == null) {
                // For now, return empty or mock for Search Engines
                _uiState.value = ManageClassifiedUiState.Success(emptyList())
                return@launch
            }

            // Fetch Sites
            val sitesResult = repository.getClassifiedSites(
                page = 1,
                itemsPerPage = 1000,
                productTypeId = productTypeId
            )

            // Fetch Counts
            val countsResult = repository.getPostCounts(dealerId)

            if (sitesResult.isSuccess) {
                val sites = sitesResult.getOrNull()?.list ?: emptyList()
                val counts = countsResult.getOrNull() ?: emptyList()

                // Map counts to a map for O(1) lookup
                val countsMap = counts.associateBy { it.domainName }

                // Map to UI Model
                val uiSites = sites.map { domain ->
                    // Find matching count
                    val postCount = countsMap[domain.domainName]?.count ?: 0

                    ClassifiedSite(
                        id = domain.id.toString(),
                        name = domain.applicationName, // Use applicationName for display name
                        url = domain.domainName, // Use domainName for URL display
                        articlesCount = postCount,
                        bannersCount = 0,
                        faqsCount = 0
                    )
                }
                _uiState.value = ManageClassifiedUiState.Success(uiSites)

            } else {
                _uiState.value = ManageClassifiedUiState.Error("Failed to fetch sites")
            }
        }
    }
    private val _openUrlEvent = kotlinx.coroutines.flow.MutableSharedFlow<String>()
    val openUrlEvent: kotlinx.coroutines.flow.SharedFlow<String> = _openUrlEvent.asSharedFlow()

    fun onVisitClick(site: ClassifiedSite) {
        viewModelScope.launch {
            val dealerId = sessionManager.getDealerId()?.toLong()
            if (dealerId == null) {
                var url = site.url
                if (!url.startsWith("http")) {
                    url = "https://$url"
                }
                _openUrlEvent.emit(url)
                return@launch
            }

            // Fetch accurate Dealer Slug from API
            var dealerSlug = sessionManager.getDealerSlug() // Default to session
            try {
                // Use getUserAuthorization as per user request to get the authoritative slug
                val userProfileResult = repository.getUserAuthorization()
                val userProfile = userProfileResult.getOrNull()

                if (!userProfile?.dealerSlug.isNullOrEmpty()) {
                    dealerSlug = userProfile?.dealerSlug
                } else {
                    // Fallback to getCombinedDealerInfo if getUserAuthorization doesn't have it (unlikely given user report)
                    val dealerInfoResult = repository.getCombinedDealerInfo(dealerId)
                    val dealerInfo = dealerInfoResult.getOrNull()
                    if (!dealerInfo?.slug.isNullOrEmpty()) {
                        dealerSlug = dealerInfo?.slug
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (dealerSlug.isNullOrEmpty()) {
                // Fallback to simple domain visit if no dealer slug found
                var url = site.url
                if (!url.startsWith("http")) {
                    url = "https://$url"
                }
                _openUrlEvent.emit(url)
                return@launch
            }

            // Try to get dynamic page slug
            val pageSlugResult = repository.getDealerDetailPageSlug(site.id.toIntOrNull() ?: 0)
            val pageSlug = pageSlugResult.getOrNull() ?: "dealers-profile" // fallback

            // Ensure protocol
            var domain = site.url
            if (!domain.startsWith("http")) {
                domain = "https://$domain"
            }
            // Remove trailing slash if any
            if (domain.endsWith("/")) {
                domain = domain.dropLast(1)
            }

            val fullUrl = "$domain/$pageSlug/$dealerSlug"
            _openUrlEvent.emit(fullUrl)
        }
    }
}

sealed class ManageClassifiedUiState {
    object Loading : ManageClassifiedUiState()
    data class Success(val sites: List<ClassifiedSite>) : ManageClassifiedUiState()
    data class Error(val message: String) : ManageClassifiedUiState()
}
