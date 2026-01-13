package com.slt.cardealership.domain.repo

import android.net.Uri
import com.slt.cardealership.domain.model.Advertisement
import com.slt.cardealership.domain.model.AdvertisementGoal
import com.slt.cardealership.domain.model.AdvertisementGoalType
import com.slt.cardealership.domain.model.AdvertisementImage
import com.slt.cardealership.domain.model.Banner
import com.slt.cardealership.domain.model.ChangePasswordRequest
import com.slt.cardealership.domain.model.DealerInfo
import com.slt.cardealership.domain.model.DealerMetasResponse
import com.slt.cardealership.domain.model.DealerService
import com.slt.cardealership.domain.model.DealerServicesRequest
import com.slt.cardealership.domain.model.Department
import com.slt.cardealership.domain.model.Designation
import com.slt.cardealership.domain.model.DetailedUserProfile
import com.slt.cardealership.domain.model.EvoxImageResponse
import com.slt.cardealership.domain.model.FaqDetails
import com.slt.cardealership.domain.model.FaqItem
import com.slt.cardealership.domain.model.FaqRequest
import com.slt.cardealership.domain.model.GalleryImage
import com.slt.cardealership.domain.model.GalleryImageUploadResponse
import com.slt.cardealership.domain.model.InternetLeadsResponse
import com.slt.cardealership.domain.model.ManageUsersResponse
import com.slt.cardealership.domain.model.ModifyDealerRequest
import com.slt.cardealership.domain.model.Post
import com.slt.cardealership.domain.model.ProductType
import com.slt.cardealership.domain.model.SeoCategory
import com.slt.cardealership.domain.model.SeoMenu
import com.slt.cardealership.domain.model.SeoMenuRequest
import com.slt.cardealership.domain.model.SeoTag
import com.slt.cardealership.domain.model.TrimListResponse
import com.slt.cardealership.domain.model.UpdateHoursRequest
import com.slt.cardealership.domain.model.UserProfileUpdateRequest
import com.slt.cardealership.domain.model.Vehicle
import com.slt.cardealership.domain.model.VehicleGalleryResponse
import com.slt.cardealership.domain.model.VehicleModel
import com.slt.cardealership.domain.model.VehicleOptionsResponse
import okhttp3.RequestBody
import java.io.File

interface DealerRepository {


    suspend fun getCombinedDealerInfo(dealerId: Long): Result<DealerInfo>
    suspend fun getPosts(dealerId: Long): Result<List<Post>>
    suspend fun getDealerMetas(dealerId: Long): DealerMetasResponse
    suspend fun getFullUserProfile(): Result<DetailedUserProfile>
    suspend fun updateUserProfile(userId: Long, request: UserProfileUpdateRequest): Result<DetailedUserProfile>
    suspend fun updateUserProfileWithImage(
        userId: Long,
        parts: Map<String, @JvmSuppressWildcards RequestBody>
    ): Result<DetailedUserProfile>
    suspend fun getUsers(
        page: Int,
        itemsPerPage: Int,
        dealerId: Long,
        roleId: Int
    ): Result<ManageUsersResponse>

    suspend fun changeUserPassword(
        userId: Long,
        request: ChangePasswordRequest
    ): Result<Unit>

    suspend fun getUserDetails(userId: Long): Result<DetailedUserProfile>

    suspend fun updateUser(
        userId: Long,
        parts: Map<String, @JvmSuppressWildcards RequestBody>
    ): Result<DetailedUserProfile>

    suspend fun addUser(
        firstName: String,
        lastName: String,
        username: String, // email
        password: String,
        phone: String?,
        imageUri: Uri?, // Pass the Uri of the selected image
        departmentId: Int?,
        designationId: Int
    ): Result<Long>

    suspend fun updateDealerInfoWithImage(
        dealerInfo: DealerInfo,
        newImageUri: Uri? // Pass the new image, or null
    ): Result<DealerInfo>

    suspend fun getDepartments(): Result<List<Department>>

    suspend fun getDesignations(departmentId: Int): Result<List<Designation>>

    // Banner Functions
    suspend fun getBanners(dealerId: Long): Result<List<Banner>>
    suspend fun getBannerDetails(dealerId: Long, bannerId: String): Result<Banner>
    suspend fun addBanner(
        dealerId: Long,
        title: String,
        url: String,
        startDate: String,
        imageFile: File
    ): Result<Unit>

    suspend fun updateBanner(
        dealerId: Long,
        bannerId: String,
        title: String,
        url: String,
        startDate: String,
        imageFile: File?
    ): Result<Unit>

    suspend fun deleteBanner(dealerId: Long, bannerId: String): Result<Unit> // <-- ADD THIS LINE

    // Gallery Functions
    suspend fun getGalleryImages(dealerId: Long): Result<List<GalleryImage>>
    suspend fun addGalleryImage(
        dealerId: Long,
        imageFile: File,
        existingUrls: List<String>
    ): Result<Unit>

    suspend fun deleteGalleryImage(
        dealerId: Long,
        imageId: String
    ): Result<Unit> // <-- ADD THIS LINE

    suspend fun getVehicles(dealerId: Long): Result<List<Vehicle>>
    suspend fun getVehicleDetails(dealerId: Long, vehicleId: String): Result<Vehicle>
    suspend fun addVehicle(dealerId: Long, vehicle: Vehicle): Result<Vehicle> // <-- This is the one for the new API
    suspend fun updateVehicle(dealerId: Long, vehicleId: String, vehicle: Vehicle): Result<Vehicle>
    suspend fun deleteVehicle(dealerId: Long, vehicleId: String): Result<Unit>
    suspend fun decodeVin(vin: String): Result<Vehicle>


    //new inventory
    suspend fun getResearchVehicles(
        dealerId: Int,
        page: Int,
        itemsPerPage: Int
        // Add filters/sorting params if needed
    ): Result<List<Vehicle>>

    suspend fun getResearchVehicleDetails(vehicleId: String): Result<Vehicle>
    suspend fun updateDealerInfo(dealerId: Long, updateMap: Map<String, String>): Result<Unit>
    suspend fun updateDealerMetas(dealerId: Long, updateMap: Map<String, Any>): Result<Unit>

    // Using PUT as the main edit method
    suspend fun editResearchVehicle(vehicleId: String, vehicle: Vehicle): Result<Vehicle>

    suspend fun updateBusinessHours(dealerId: Long, request: UpdateHoursRequest): Result<Unit>

    suspend fun requestDealerUpdate(request: ModifyDealerRequest): Result<Unit>

    suspend fun getVehicleGallery(vehicleId: String): Result<VehicleGalleryResponse>

    suspend fun uploadVehicleGalleryImages(vehicleId: String, imageFiles: List<File>, existingImageUrls: List<String>): Result<Unit>

    suspend fun deleteVehicleGalleryImage(vehicleId: String, imageUrl: String): Result<Unit>

    suspend fun deleteVehicleGalleryImages(vehicleId: String, imageUrls: List<String>): Result<Unit>

    suspend fun uploadVehicleGallerySingleImage(imageFile: File): Result<GalleryImageUploadResponse>

    suspend fun getTrimListPost(vin: String): Result<TrimListResponse>

    suspend fun getTrimListByVin(vin: String): Result<TrimListResponse>

    suspend fun getEvoxImages(vin: String): Result<EvoxImageResponse>

    suspend fun getVehicleOptionsAndPackages(vin: String): Result<VehicleOptionsResponse>

    suspend fun getDecodedDataByTrim(trimId: String): Result<Vehicle>

    suspend fun getModelsForMake(makeId: Int): Result<List<VehicleModel>>


    // --- *** NEW ADVERTISEMENT FUNCTIONS START HERE *** ---
    suspend fun getAdvertisements(dealerId: Long): Result<List<Advertisement>>
    suspend fun getAdvertisementDetails(dealerId: Long, advertisementId: String): Result<Advertisement>
    suspend fun deleteAdvertisement(dealerId: Long, advertisementId: String): Result<Unit>
    suspend fun getAdvertisementGoals(): Result<List<AdvertisementGoal>>
    suspend fun getAdvertisementGoalTypes(goalId: Int): Result<List<AdvertisementGoalType>>
    suspend fun addAdvertisement(dealerId: Long, advertisement: Advertisement): Result<String>
    suspend fun updateAdvertisement(dealerId: Long, advertisementId: String, advertisement: Advertisement): Result<Advertisement>
   // suspend fun getAdvertisementDomains(dealerId: Long, advertisementId: String): Result<List<AdvertisementDomain>>
    suspend fun updateAdvertisementDomains(dealerId: Long, advertisementId: String, domainIds: List<String>): Result<Unit>
    suspend fun getAdvertisementGallery(dealerId: Long, advertisementId: String): Result<List<AdvertisementImage>>
    suspend fun updateAdvertisementGallery(dealerId: Long, advertisementId: String, imageFiles: List<File>): Result<Unit>

    // --- SEO Tags ---
    suspend fun getSeoTags(dealerId: Long): Result<List<SeoTag>> // <-- FIX: Add dealerId
    suspend fun deleteSeoTag(id: String): Result<Unit> // <-- FIX: ID is String
    // FIX: Add dealerId, use tagUrl
    suspend fun addSeoTag(dealerId: Long, tagName: String, tagUrl: String): Result<Unit>
    suspend fun getMappedSeoTags(dealerId: Long, domainId: Int): Result<List<SeoTag>>
    suspend fun mapSeoTagsToDomain(dealerId: Long, domainId: Int, tagIds: List<String>): Result<Unit>

    // --- FAQ ---
    suspend fun getFaqs(dealerId: Long, domainId: Int): Result<List<FaqItem>>
    suspend fun getFaqDetails(faqId: Int): Result<FaqDetails>
    suspend fun addFaq(faqRequest: FaqRequest): Result<Unit>
    suspend fun updateFaq(faqId: Int, faqRequest: FaqRequest): Result<Unit>
    suspend fun deleteFaq(faqId: Int, type: String): Result<Unit>

    //seo menues
    suspend fun getSeoMenus(dealerId: Long): Result<List<SeoMenu>>
    suspend fun getSeoCategories(): Result<List<SeoCategory>>
    suspend fun saveSeoMenus(dealerId: Long, request: SeoMenuRequest): Result<Unit>

    suspend fun getAllProductTypes(): Result<List<ProductType>>
    suspend fun getDealerServices(dealerId: Int): Result<List<DealerService>>

    suspend fun saveDealerServices(dealerId: Int, request: DealerServicesRequest): Result<Unit>

    suspend fun getInternetLeads(
        dealerId: Int,
        leadType: String,
        page: Int,
        itemsPerPage: Int
    ): Result<InternetLeadsResponse>

    // User and Authorization
    suspend fun getUserAuthorization(): Result<com.slt.cardealership.domain.model.UserProfile>
    suspend fun getAllUsers(dealerId: Long): Result<List<com.slt.cardealership.domain.model.ManageUsers>>

    // Domain Management
    suspend fun getDomains(
        page: Int,
        itemsPerPage: Int,
        dealerId: Long
    ): Result<com.slt.cardealership.domain.model.DomainResponse>

    suspend fun getDomainDetails(domainId: Int): Result<com.slt.cardealership.domain.model.DomainItem>

    // Domain Pages
    suspend fun getDomainPages(
        page: Int,
        itemsPerPage: Int,
        domainId: Int
    ): Result<com.slt.cardealership.domain.model.DomainPageResponse>

    suspend fun deleteDomainPage(pageId: String): Result<Unit>

    suspend fun updateDomainPage(request: com.slt.cardealership.domain.model.DomainPageUpdateRequest): Result<Unit>

    suspend fun getDomainPageDetails(pageId: String): Result<com.slt.cardealership.domain.model.DomainPageDetails>

    suspend fun createDomainPage(request: com.slt.cardealership.domain.model.DomainPageCreateRequest): Result<Unit>

    // Domain Blogs
    suspend fun getDomainBlogs(domainId: Int, page: Int, limit: Int): Result<com.slt.cardealership.domain.model.DomainBlogResponse>

    suspend fun getDomainBlogCategories(domainId: Int): Result<com.slt.cardealership.domain.model.BlogCategoryResponse>

    suspend fun createDomainBlog(
        title: String,
        slug: String,
        description: String,
        metaTitle: String,
        metaDescription: String,
        blogType: String,
        status: String,
        domainId: Int,
        cta: String,
        researchCompare: String,
        category: String,
        shortDescription: String,
        createdBy: Long,
        updatedBy: Long,
        file: java.io.File?
    ): Result<Unit>

    suspend fun getDomainBlogDetails(blogId: String): Result<com.slt.cardealership.domain.model.DomainBlogDetails>

    suspend fun updateDomainBlog(
        blogId: String,
        title: String,
        slug: String,
        description: String,
        metaTitle: String,
        metaDescription: String,
        blogType: String,
        status: String,
        domainId: Int,
        cta: String,
        researchCompare: String,
        category: String,
        shortDescription: String,
        createdBy: Long,
        updatedBy: Long,
        file: java.io.File?
    ): Result<Unit>

    // Domain Sliders
    suspend fun getDomainSliders(
        page: Int,
        itemsPerPage: Int,
        domainId: Int,
        search: String
    ): Result<List<com.slt.cardealership.domain.model.DomainSlider>>

    // Research API
    suspend fun getResearchMakes(): Result<List<com.slt.cardealership.domain.model.ResearchMake>>
    suspend fun getResearchModels(makeId: Int): Result<List<com.slt.cardealership.domain.model.ResearchModel>>
    suspend fun getResearchModelYears(makeId: Int, modelId: Int): Result<List<com.slt.cardealership.domain.model.ResearchYear>>
    suspend fun getResearchTrims(makeId: Int, modelId: Int, year: Int): Result<List<com.slt.cardealership.domain.model.ResearchTrim>>

    suspend fun createDomainSlider(name: String, domainId: String): Result<String>

    suspend fun getDomainSliderDetails(sliderId: String): Result<com.slt.cardealership.domain.model.DomainSliderDetails>

    suspend fun addDomainSlideItem(
        sliderId: String,
        domainId: String,
        title: String,
        link: String,
        target: String,
        startDate: Long,
        endDate: Long,
        imageFile: java.io.File
    ): Result<Unit>

    suspend fun deleteDomainSlider(sliderId: String): Result<Unit>

    suspend fun updateDomainSlideItem(
        sliderId: String,
        slideId: String,
        domainId: String,
        title: String,
        link: String,
        target: String,
        startDate: Long,
        endDate: Long,
        imageFile: java.io.File?,
        imageUrl: String?
    ): Result<Unit>

    suspend fun getDomainMenus(
        page: Int,
        itemsPerPage: Int,
        domainId: Int,
        menuName: String
    ): Result<List<com.slt.cardealership.domain.model.DomainMenu>>

    suspend fun getDomainMenuDetails(menuId: String): Result<com.slt.cardealership.domain.model.DomainMenuDetail>

    suspend fun getResearchCompareInterlinks(
        page: Int,
        itemsPerPage: Int,
        domainId: Int
    ): Result<List<com.slt.cardealership.domain.model.ResearchCompareItem>>

    suspend fun getResearchCompareCategories(
        domainId: Int
    ): Result<com.slt.cardealership.domain.model.ResearchCompareCategoryResponse>

    suspend fun createResearchCompare(
        request: com.slt.cardealership.domain.model.CreateResearchCompareRequest
    ): Result<Unit>

    suspend fun deleteResearchCompare(id: String): Result<Unit>

    suspend fun getResearchCompareDetails(id: String): Result<com.slt.cardealership.domain.model.ResearchCompareDetailsResponse>

    suspend fun updateResearchCompare(
        id: String,
        request: com.slt.cardealership.domain.model.CreateResearchCompareRequest
    ): Result<Unit>
}