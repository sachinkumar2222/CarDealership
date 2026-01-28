package com.slt.cardealership.data.remote.network

import com.slt.cardealership.domain.model.AddSeoTagRequest
import com.slt.cardealership.domain.model.Advertisement
import com.slt.cardealership.domain.model.AdvertisementGalleryResponse
import com.slt.cardealership.domain.model.AdvertisementGoal
import com.slt.cardealership.domain.model.DetailedUserProfile
import com.slt.cardealership.domain.model.AdvertisementGoalType
import com.slt.cardealership.domain.model.AdvertisementListResponse
import com.slt.cardealership.domain.model.Banner
import com.slt.cardealership.domain.model.BannerListResponse
import com.slt.cardealership.domain.model.ChangePasswordRequest
import com.slt.cardealership.domain.model.DealerDetailsResponse
import com.slt.cardealership.domain.model.DealerHours
import com.slt.cardealership.domain.model.DealerInfo
import com.slt.cardealership.domain.model.DealerMetasResponse
import com.slt.cardealership.domain.model.DealerService
import com.slt.cardealership.domain.model.DealerServicesRequest
import com.slt.cardealership.domain.model.Department
import com.slt.cardealership.domain.model.Designation
import com.slt.cardealership.domain.model.FaqDetails
import com.slt.cardealership.domain.model.FaqListResponse
import com.slt.cardealership.domain.model.FaqRequest
import com.slt.cardealership.domain.model.SocialProfileItem
import com.slt.cardealership.domain.model.SocialProfileRequest
import com.slt.cardealership.domain.model.GalleryResponseObject
import com.slt.cardealership.domain.model.InternetLeadsResponse
import com.slt.cardealership.domain.model.ManageUsersResponse
import com.slt.cardealership.domain.model.MapSeoTagsRequest
import com.slt.cardealership.domain.model.ModifyDealerRequest
import com.slt.cardealership.domain.model.Post
import com.slt.cardealership.domain.model.PostListResponse
import com.slt.cardealership.domain.model.ProductType
import com.slt.cardealership.domain.model.SeoCategory
import com.slt.cardealership.domain.model.SeoMenu
import com.slt.cardealership.domain.model.SeoMenuRequest
import com.slt.cardealership.domain.model.SeoTag
import com.slt.cardealership.domain.model.SeoTagListResponse
import com.slt.cardealership.domain.model.UpdateDomainsRequest
import com.slt.cardealership.domain.model.UpdateHoursRequest
import com.slt.cardealership.domain.model.UserProfile
import com.slt.cardealership.domain.model.UserProfileUpdateRequest
import com.slt.cardealership.domain.model.Vehicle
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.DELETE
import retrofit2.http.Multipart
import retrofit2.http.Body
import okhttp3.MultipartBody
import okhttp3.RequestBody
import com.slt.cardealership.domain.model.VehicleModel
import com.slt.cardealership.domain.model.VinRequest
import com.slt.cardealership.domain.model.VehicleOptionsResponse
import com.slt.cardealership.domain.model.TrimListResponse
import com.slt.cardealership.domain.model.EvoxImageResponse
import com.slt.cardealership.domain.model.VehicleListResponse
import com.slt.cardealership.domain.model.VehicleGalleryResponse
import com.slt.cardealership.domain.model.GalleryImageUploadResponse
import com.slt.cardealership.domain.model.ManageUsers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("organizations-api/businessListingUserAuthorize")
    suspend fun getUserAuthorization(): UserProfile

    @GET("organizations-api/users/{userId}")
    suspend fun getDetailedUserProfile(@Path("userId") userId: Long): DetailedUserProfile

    @Multipart
    @PUT("organizations-api/users/{userId}")
    suspend fun putUserProfile(
        @Path("userId") userId: Long,
        @PartMap parts: Map<String, @JvmSuppressWildcards RequestBody>
    ): DetailedUserProfile

//    @POST("organizations-api/users/changePassword/{id}")
//    suspend fun changeUserPassword(
//        @Path("id") userId: Long,
//        @Body request: ChangePasswordRequest
//    ): Response<Unit>

    @GET("systems-api/departments/getAll")
    suspend fun getDepartments(
        @Query("role_type") roleType: String = "dealer"
    ): List<Department>

    @GET("systems-api/designations/getAll")
    suspend fun getDesignations(
        @Query("department_id") departmentId: Int
    ): List<Designation>

    @Multipart
    @POST("organizations-api/users")
    suspend fun addUser(
        @PartMap parts: Map<String, @JvmSuppressWildcards RequestBody>
    ): Long

    @FormUrlEncoded // <-- FIX 1: Change to @Multipart
    @PATCH("dealer-api/Dealers/{dealerId}")
    suspend fun updateDealerInfo(
        @Path("dealerId") dealerId: Long,
        @FieldMap body: Map<String, String>
    ): Response<Unit>

    @PATCH("dealer-api/dealer-metas/{dealerId}")
    suspend fun updateDealerMetas(
        @Path("dealerId") dealerId: Long,
        @Body body: Map<String,@JvmSuppressWildcards Any>
    ): Response<Unit>

    @POST("dealer-api/Dealers/{dealerId}/Hours")
    suspend fun updateBusinessHours(
        @Path("dealerId") dealerId: Long,
        @Body request: UpdateHoursRequest // <-- Use new request model
    ): Response<Unit>

    @POST("dealer-api/dealer-requests/modify-dealer")
    suspend fun requestDealerUpdate(
        @Body request: ModifyDealerRequest
    ): Response<Unit>


    @GET("dealer-api/Dealers/{dealerId}")
    suspend fun getDealerDetails(@Path("dealerId") dealerId: Long): DealerDetailsResponse


    @GET("dealer-api/dealer-metas/{dealerId}")
    suspend fun getDealerMetas(
        @Path("dealerId") dealerId: Long
    ): DealerMetasResponse

    @GET("dealer-api/Dealers/{dealerId}/Hours")
    suspend fun getDealerHours(@Path("dealerId") dealerId: Long): List<DealerHours>

    @GET("dealer-api/dealers/{dealerId}/posts")
    suspend fun getPosts(
        @Path("dealerId") dealerId: Long
    ): PostListResponse

    @GET("dealer-api/dealers/{dealerId}/posts")
    suspend fun getClassifiedPosts(
        @Path("dealerId") dealerId: Long,
        @Query("page") page: Int,
        @Query("item_per_page") itemsPerPage: Int,
        @Query("order_by") orderBy: String?,
        @Query("order") order: String?,
        @Query("status") status: String?,
        @Query("name") name: String?,
        @Query("have_content") haveContent: String?,
        @Query("have_links") haveLinks: String?,
        @Query("have_image") haveImage: String?,
        @Query("domain_name") domainName: String?
    ): PostListResponse

    @GET("dealer-api/dealers/{dealerId}/ArticleLinks/getAll")
    suspend fun getArticleLinks(
        @Path("dealerId") dealerId: Long
    ): List<com.slt.cardealership.domain.model.ArticleLink>


    @GET("dealer-api/dealers/{dealerId}/posts/{postId}")
    suspend fun getPostById(
        @Path("dealerId") dealerId: Long,
        @Path("postId") postId: String
    ): Post

    @Multipart
    @POST("dealer-api/dealers/{dealerId}/banners")
    suspend fun addBanner(
        @Path("dealerId") dealerId: Long,
        @Part("title") title: RequestBody,
        @Part("url") url: RequestBody,
        @Part("domain_id") domainId: RequestBody,
        @Part("start_date") startDate: RequestBody,
        @Part("end_date") endDate: RequestBody?,
        @Part image: MultipartBody.Part,
        @Part("created_by") createdBy: RequestBody,
        @Part("updated_by") updatedBy: RequestBody,
        @Part("created_on") createdOn: RequestBody,
        @Part("updated_on") updatedOn: RequestBody
    ): Response<Unit>

    @Multipart // <-- 1. Must be Multipart
    @PUT("dealer-api/Dealers/{dealerId}") // <-- 2. Must be PUT
    suspend fun updateDealerInfoMultipart(
        @Path("dealerId") dealerId: Long, // <-- 3. Your dealerId is an Int
        @PartMap parts: Map<String, @JvmSuppressWildcards RequestBody>
    ): DealerInfo

    @POST("dealer-api/dealers/{dealerId}/posts")
    suspend fun addPost(@Path("dealerId") dealerId: Long, @Body post: Post): Response<Unit>

    @PUT("dealer-api/dealers/{dealerId}/posts/{postId}")
    suspend fun updatePost(
        @Path("dealerId") dealerId: Long,
        @Path("postId") postId: String,
        @Body post: Post
    ): Response<Unit>

    @DELETE("dealer-api/dealers/{dealerId}/posts/{postId}")
    suspend fun deletePost(
        @Path("dealerId") dealerId: Long,
        @Path("postId") postId: String
    ): Response<Unit> // Use Response<Unit> for empty responses

    @Multipart
    @POST("dealer-api/dealers/{dealerId}/posts/UploadImage")
    suspend fun uploadPostImage(
        @Path("dealerId") dealerId: Long,
        @Part image: MultipartBody.Part
    ): String

    @GET("dealer-api/dealers/{dealerId}/banners")
    suspend fun getBanners(
        @Path("dealerId") dealerId: Long,
        @Query("page") page: Int,
        @Query("item_per_page") itemsPerPage: Int,
        @Query("domain_id") domainId: Int?
    ): BannerListResponse

    @Multipart
    @PUT("dealer-api/dealers/{dealerId}/banners/{bannerId}")
    suspend fun updateBanner(
        @Path("dealerId") dealerId: Long,
        @Path("bannerId") bannerId: String,
        @Part("id") id: RequestBody,
        @Part("title") title: RequestBody,
        @Part("url") url: RequestBody,
        @Part("domain_id") domainId: RequestBody,
        @Part("start_date") startDate: RequestBody,
        @Part("end_date") endDate: RequestBody?,
        @Part image: MultipartBody.Part?,
        @Part("image_url") imageUrl: RequestBody?,
        @Part("created_by") createdBy: RequestBody,
        @Part("updated_by") updatedBy: RequestBody,
        @Part("updated_on") updatedOn: RequestBody
    ): Response<Unit>


    @GET("dealer-api/dealers/{dealerId}/banners/{bannerId}")
    suspend fun getBannerDetails(
        @Path("dealerId") dealerId: Long,
        @Path("bannerId") bannerId: String
    ): Banner

    @DELETE("dealer-api/dealers/{dealerId}/banners/{bannerId}")
    suspend fun deleteBanner(
        @Path("dealerId") dealerId: Long,
        @Path("bannerId") bannerId: String
    ): Response<Unit>

    @GET("dealer-api/dealers/{dealerId}/Gallery")
    suspend fun getGalleryImages(@Path("dealerId") dealerId: Long): GalleryResponseObject

    @Multipart
    @POST("dealer-api/dealers/{dealerId}/Gallery")
    suspend fun addGalleryImage(
        @Path("dealerId") dealerId: Long,
        @Part("image_urls") imageUrls: RequestBody,
        @Part gallery: MultipartBody.Part
    ): Response<Unit>

    @Multipart
    @POST("dealer-api/dealers/{dealerId}/Gallery")
    suspend fun updateGalleryImages(
        @Path("dealerId") dealerId: Long,
        @Part("image_urls") imageUrls: RequestBody
    ): Response<Unit>

    @DELETE("dealer-api/dealers/{dealerId}/Gallery/{imageId}")
    suspend fun deleteGalleryImage(
        @Path("dealerId") dealerId: Long,
        @Path("imageId") imageId: String
    ): Response<Unit>

    @POST("research-api/vehicleInventory/add")
    suspend fun addVehicle(
        @Body vehicle: Vehicle
    ): Response<Vehicle>


    @GET("dealer-api/dealers/{dealerId}/inventory")
    suspend fun getVehicles(
        @Path("dealerId") dealerId: Long
    ): VehicleListResponse

    @GET("dealer-api/dealers/{dealerId}/inventory/{vehicleId}")
    suspend fun getVehicleDetails(
        @Path("dealerId") dealerId: Long,
        @Path("vehicleId") vehicleId: String
    ): Vehicle

    @PUT("dealer-api/dealers/{dealerId}/inventory/{vehicleId}")
    suspend fun updateVehicle(
        @Path("dealerId") dealerId: Long,
        @Path("vehicleId") vehicleId: String,
        @Body vehicle: Vehicle
    ): Response<Vehicle>

    @DELETE("dealer-api/dealers/{dealerId}/inventory/{vehicleId}")
    suspend fun deleteVehicle(
        @Path("dealerId") dealerId: Long,
        @Path("vehicleId") vehicleId: String
    ): Response<Unit>

    @GET("dealer-api/vin-decoder/{vin}")
    suspend fun decodeVin(@Path("vin") vin: String): Response<Vehicle>

    @GET("research-api/vehicleInventory")
    suspend fun getResearchVehicles(
        // *** ADDED Query Parameters ***
        @Query("dealer_id") dealerId: Int,
        @Query("page") page: Int,
        @Query("item_per_page") itemsPerPage: Int,
        @Query("is_active") isActive: String = "yes", // Example filter, add others if needed
        @Query("is_deleted") isDeleted: String = "no"  // Example filter
        // Add other query params like sort_by, search_term etc. if your API supports them
    ): VehicleListResponse


    @GET("research-api/vehicleInventory/get/{id}")
    suspend fun getResearchVehicleDetails(
        @Path("id") vehicleId: String
    ): Vehicle

    @POST("research-api/vehicleInventory/edit/{id}")
    suspend fun editResearchVehiclePOST(
        @Path("id") vehicleId: String,
        @Body vehicle: Vehicle
    ): Response<Vehicle> // Reusing model

    @PUT("research-api/vehicleInventory/edit/{id}")
    suspend fun editResearchVehiclePUT(
        @Path("id") vehicleId: String,
        @Body vehicle: Vehicle
    ): Response<Vehicle> // Reusing model




    @GET("research-api/vehicleInventory/{id}/gallery")
    suspend fun getVehicleGallery(
        @Path("id") vehicleId: String
    ): VehicleGalleryResponse // Using new model

    // This is "Edit / Post Vehicle Galleries" AND "Upload Multiple Images"
    @Multipart
    @POST("research-api/vehicleInventory/{id}/gallery")
    suspend fun uploadVehicleGalleryImages(
        @Path("id") vehicleId: String,
        @Part images: List<MultipartBody.Part>,
        @Part("image_urls") imageUrls: RequestBody
    ): Response<Unit>

    @POST("research-api/vehicleInventory/{id}/gallery")
    @FormUrlEncoded
    suspend fun updateVehicleGallery(
        @Path("id") vehicleId: String,
        @Field("vehicle_id") formVehicleId: String,
        @Field("image_urls") imageUrls: String
    ): Response<Unit>

    // Upload Single Image
    @Multipart
    @POST("research-api/vehicleInventory/gallery")
    suspend fun uploadVehicleGallerySingleImage(
        @Part image: MultipartBody.Part
    ): Response<GalleryImageUploadResponse> // Using new model



    @POST("research-api/vin-decoder-api/GetMappedDecoderData")
    suspend fun getTrimListPost(
        @Body vinRequest: VinRequest // Using new model
    ): TrimListResponse // Using new model

    @GET("research-api/vin-decoder-api/GetTrimListByVIN")
    suspend fun getTrimListByVin(
        @Query("vin") vin: String
    ): TrimListResponse // Using new model

    @GET("research-api/vin-decoder-api/GetEvoxImagesForAddEdit")
    suspend fun getEvoxImages(
        @Query("vin") vin: String // Assuming VIN is the query param
    ): EvoxImageResponse // Using new model

    @GET("research-api/vin-decoder-api/GetVehicleOptionsPackagesForEdit")
    suspend fun getVehicleOptionsAndPackages(
        @Query("vin") vin: String // Assuming VIN is the query param
    ): VehicleOptionsResponse // Using new model

    @GET("research-api/vin-decoder-api/GetDecodedDataByTrim")
    suspend fun getDecodedDataByTrim(
        @Query("trimId") trimId: String
    ): Vehicle

    @GET("research-api/models/getAll")
    suspend fun getModelsForMake(
        @Query("make_id") makeId: Int,
        @Query("is_active") isActive: String = "yes", // Default query params
        @Query("is_deleted") isDeleted: String = "no"  // Default query params
    ): List<VehicleModel>

    @GET("dealer-api/dealers/{dealer_id}/advertisements")
    suspend fun getAdvertisements(
        @Path("dealer_id") dealerId: Long,
        @Query("page") page: Int = 1,
        @Query("item_per_page") itemsPerPage: Int = 20
    ): AdvertisementListResponse // Correctly returns the wrapper

    @GET("dealer-api/dealers/{dealer_id}/advertisements/{id}")
    suspend fun getAdvertisementDetails(
        @Path("dealer_id") dealerId: Long,
        @Path("id") advertisementId: String
    ): Advertisement // API returns the single ad object directly

    @DELETE("dealer-api/dealers/{dealer_id}/advertisements/{id}")
    suspend fun deleteAdvertisement(
        @Path("dealer_id") dealerId: Long,
        @Path("id") advertisementId: String
    ): Response<Unit>

    @GET("systems-api/dealer-advertisement-goal/getGoalList")
    suspend fun getAdvertisementGoals(): List<AdvertisementGoal> // Correct: returns direct list []

    @GET("systems-api/dealer-advertisement-goal/getGoalTypeList/{id}")
    suspend fun getAdvertisementGoalTypes(
        @Path("id") goalId: Int // Changed to Int to match AdvertisementGoal.id
    ): List<AdvertisementGoalType> // Assuming direct list []

    @POST("dealer-api/dealers/{dealer_id}/advertisements")
    suspend fun addAdvertisement(
        @Path("dealer_id") dealerId: Long,
        @Body advertisement: Advertisement // We will build this object in the ViewModel
    ): Response<String>

    @PUT("dealer-api/dealers/{dealer_id}/advertisements/{adv_id}")
    suspend fun updateAdvertisement(
        @Path("dealer_id") dealerId: Long,
        @Path("adv_id") advertisementId: String,
        @Body advertisement: Advertisement
    ): Response<Advertisement>

    @POST("dealer-api/dealers/{dealer_id}/advertisements/{id}/domains")
    suspend fun updateAdvertisementDomains(
        @Path("dealer_id") dealerId: Long,
        @Path("id") advertisementId: String,
        @Body request: UpdateDomainsRequest
    ): Response<Unit>

    // --- Ad Gallery APIs ---
    @GET("dealer-api/dealers/{dealer_id}/advertisements/{id}/gallery")
    suspend fun getAdvertisementGallery(
        @Path("dealer_id") dealerId: Long,
        @Path("id") advertisementId: String
    ): AdvertisementGalleryResponse

    @Multipart
    @POST("dealer-api/dealers/{dealer_id}/advertisements/{id}/gallery")
    suspend fun updateAdvertisementGallery(
        @Path("dealer_id") dealerId: Long,
        @Path("id") advertisementId: String,
        @Part images: List<MultipartBody.Part>
    ): Response<Unit>

    @GET("dealer-api/dealer-seo-tags")
    suspend fun getSeoTags(
        @Query("dealer_id") dealerId: Long, // <-- FIX: Add parameters
        @Query("page") page: Int = 1,
        @Query("item_per_page") itemsPerPage: Int = 50 // Get up to 50
    ): SeoTagListResponse // <-- FIX: Return the wrapper object

    /**
     * Delete an SEO tag from the master list by its ID
     * DELETE {base_url}/dealer-api/dealer-seo-tags/{id}
     */
    @DELETE("dealer-api/dealer-seo-tags/{id}")
    suspend fun deleteSeoTag(
        @Path("id") tagId: String // <-- FIX: ID is a String
    ): Response<Unit>

    /**
     * Add a new SEO tag to the master list
     * POST {base_url}/dealer-api/dealer-seo-tags
     */
    @POST("dealer-api/dealer-seo-tags")
    suspend fun addSeoTag(
        @Body request: AddSeoTagRequest // <-- FIX: Use new request model
    ): Response<Unit> // Assuming it returns the newly created tag

    /**
     * Get the list of tags already mapped to a specific dealer and domain
     * GET {base_url}/dealer-api/dealer-seo-tags/getfordomain/{dealer_id}/{domain_id}
     */
    @GET("dealer-api/dealer-seo-tags/getfordomain/{dealer_id}/{domain_id}")
    suspend fun getMappedSeoTags(
        @Path("dealer_id") dealerId: Long,
        @Path("domain_id") domainId: Int
    ): List<SeoTag> // This was correct, returns a direct list

    /**
     * Map a list of SEO tag IDs to a dealer/domain
     * POST {base_url}/dealer-api/dealer-seo-tags/addtodomain
     */
    @POST("dealer-api/dealer-seo-tags/addtodomain/{dealer_id}/{domain_id}")
    suspend fun mapSeoTagsToDomain(
        @Path("dealer_id") dealerId: Long,
        @Path("domain_id") domainId: Int,
        @Body request: MapSeoTagsRequest // <-- FIX: Uses List<String>
    ): Response<Unit>

    // --- FAQ ---

    /**
     * Get all FAQs for a dealer
     * GET {base_url}/dealer-api/dealer-faqs
     */
    @GET("dealer-api/dealer-faqs")
    suspend fun getFaqs(
        @Query("dealer_id") dealerId: Long,
        @Query("domain_id") domainId: Int = 0,
        @Query("page") page: Int = 1,
        @Query("item_per_page") itemsPerPage: Int = 20
    ): FaqListResponse

    @GET("dealer-api/dealer-faqs")
    suspend fun getFaqsWithDomain(
        @Query("dealer_id") dealerId: Long,
        @Query("domain_id") domainId: Int = 0,
        @Query("domain_name") domainName: String? = null,
        @Query("page") page: Int = 1,
        @Query("item_per_page") itemsPerPage: Int = 20
    ): FaqListResponse

    /**
     * Get a specific FAQ by ID
     * GET {base_url}/dealer-api/dealer-faqs/{id}
     */
    @GET("dealer-api/dealer-faqs/{id}")
    suspend fun getFaqDetails(
        @Path("id") faqId: Int
    ): FaqDetails // Returns the detailed object

    /**
     * Add a new FAQ
     * POST {base_url}/dealer-api/dealer-faqs
     */
    @POST("dealer-api/dealer-faqs")
    suspend fun addFaq(
        @Body faqRequest: FaqRequest
    ): Response<Unit> // Returns 201 Created with an empty body

    /**
     * Edit an existing FAQ by ID
     * PUT {base_url}/dealer-api/dealer-faqs/{id}
     */
    @PUT("dealer-api/dealer-faqs/{id}")
    suspend fun updateFaq(
        @Path("id") faqId: Int,
        @Body faqRequest: FaqRequest
    ): Response<Unit> // Returns 201 Created with an empty body

    /**
     * Delete an FAQ by ID and type
     * DELETE {base_url}/dealer-api/dealer-faqs/{id}?type={type}
     */
    @DELETE("dealer-api/dealer-faqs/{id}")
    suspend fun deleteFaq(
        @Path("id") faqId: Int,
        @Query("type") type: String // You will need to determine what this 'type' is
    ): Response<Unit>
    @GET("organizations-api/users")
    suspend fun getUsers(
        @Query("page") page: Int,
        @Query("item_per_page") itemsPerPage: Int,
        @Query("dealer_id") dealerId: Long?,
        @Query("role_id") roleId: Int
    ): ManageUsersResponse

    @GET("organizations-api/users/getAll")
    suspend fun getAllUsers(
        @Query("dealer_id") dealerId: Long
    ): List<ManageUsers>



    @PUT("organizations-api/users/{userId}/change-password")
    suspend fun changeUserPassword(
        @Path("userId") userId: Long,
        @Body request: ChangePasswordRequest
    ): Response<Unit>

    /**
     * Get the list of saved SEO Menus for a specific dealer
     */
    @GET("dealer-api/dealers/{dealerId}/Seomenus")
    suspend fun getSeoMenus(
        @Path("dealerId") dealerId: Long
    ): List<SeoMenu> // Returns a direct list

    /**
     * Get the list of all available SEO Categories
     */
    @GET("systems-api/dealer-seo-category/getall")
    suspend fun getSeoCategories(): List<SeoCategory> // Returns a direct list

    /**
     * Save the entire list of SEO Menus for a dealer.
     * This will replace the existing list.
     */
    @POST("dealer-api/dealers/{dealerId}/SeoMenus")
    suspend fun saveSeoMenus(
        @Path("dealerId") dealerId: Long,
        @Body request: SeoMenuRequest
    ): Response<Unit> // Assuming 200 OK with no bod

    @GET("systems-api/product-types/getAll")
    suspend fun getAllProductTypes(): List<ProductType>

    /**
     * Gets the list of services currently enabled for a specific dealer.
     */
    @GET("dealer-api/dealers/{dealerId}/services")
    suspend fun getDealerServices(
        @Path("dealerId") dealerId: Int
    ): List<DealerService>

    @POST("dealer-api/dealers/{dealerId}/services")
    suspend fun saveDealerServices(
        @Path("dealerId") dealerId: Int,
        @Body request: DealerServicesRequest
    ): Response<Unit>

    @GET("application-api/internet-leads")
    suspend fun getInternetLeads(
        @Query("dealer_id") dealerId: Int,
        @Query("lead_type") leadType: String,
        @Query("page") page: Int,
        @Query("item_per_page") itemsPerPage: Int,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("is_read") isRead: Boolean? = null,
        @Query("search") search: String? = null
    ): InternetLeadsResponse

    @GET("application-api/domains")
    suspend fun getDomains(
        @Query("page") page: Int,
        @Query("item_per_page") itemsPerPage: Int,
        @Query("dealer_id") dealerId: Long? = null,
        @Query("product_type_id") productTypeId: Int? = null,
        @Query("domain_name") domainName: String? = null,
        @Query("in_production") inProduction: Boolean? = null,
        @Query("in_business_listing") inBusinessListing: Boolean? = null
    ): com.slt.cardealership.domain.model.DomainResponse

    @GET("application-api/domains/{id}")
    suspend fun getDomainDetails(
        @Path("id") domainId: Int
    ): com.slt.cardealership.domain.model.DomainItem

    @GET("application-api/page-types")
    suspend fun getPageTypes(
        @Query("page_type_slug") slug: String
    ): List<com.slt.cardealership.domain.model.PageType>

    @GET("application-api/domain-pages")
    suspend fun getDomainPages(
        @Query("page") page: Int,
        @Query("item_per_page") itemsPerPage: Int,
        @Query("domain_id") domainId: Int,
        @Query("page_type_id") pageTypeId: String? = null
    ): com.slt.cardealership.domain.model.DomainPageResponse

    @DELETE("application-api/domain-pages/{id}")
    suspend fun deleteDomainPage(
        @Path("id") pageId: String
    ): Response<Unit>

    @Multipart
    @PUT("application-api/domain-pages/{id}")
    suspend fun updateDomainPage(
        @Path("id") pageId: String,
        @PartMap partMap: Map<String, @JvmSuppressWildcards okhttp3.RequestBody>,
        @Part featuredFile: okhttp3.MultipartBody.Part? = null,
        @Part bannerFile: okhttp3.MultipartBody.Part? = null
    ): Response<Unit>
    @GET("application-api/domain-pages/{id}")
    suspend fun getDomainPageDetails(
        @Path("id") pageId: String
    ): com.slt.cardealership.domain.model.DomainPageDetails

    @Multipart
    @POST("application-api/domain-pages")
    suspend fun createDomainPage(
        @PartMap partMap: Map<String, @JvmSuppressWildcards okhttp3.RequestBody>,
        @Part featuredFile: okhttp3.MultipartBody.Part? = null,
        @Part bannerFile: okhttp3.MultipartBody.Part? = null
    ): Response<Unit>

    @GET("application-api/domain-blogs")
    suspend fun getDomainBlogs(
        @Query("domain_id") domainId: Int,
        @Query("page") page: Int,
        @Query("item_per_page") limit: Int
    ): com.slt.cardealership.domain.model.DomainBlogResponse

    @GET("application-api/domain-blog-categories")
    suspend fun getDomainBlogCategories(
        @Query("domain_id") domainId: Int
    ): com.slt.cardealership.domain.model.BlogCategoryResponse

    @GET("application-api/domain-slider")
    suspend fun getDomainSliders(
        @Query("page") page: Int,
        @Query("item_per_page") itemsPerPage: Int,
        @Query("domain_id") domainId: Int,
        @Query("s") search: String
    ): List<com.slt.cardealership.domain.model.DomainSlider>

    @POST("application-api/domain-slider")
    suspend fun createDomainSlider(
        @Body request: com.slt.cardealership.domain.model.DomainSliderCreateRequest
    ): Response<com.slt.cardealership.domain.model.DomainSliderCreateResponse>

    @GET("application-api/domain-slider/{id}")
    suspend fun getDomainSliderDetails(
        @Path("id") sliderId: String
    ): com.slt.cardealership.domain.model.DomainSliderDetails

    @Multipart
    @POST("application-api/domain-slider/{id}/item")
    suspend fun addDomainSlideItem(
        @Path("id") sliderId: String,
        @PartMap partMap: Map<String, @JvmSuppressWildcards okhttp3.RequestBody>,
        @Part file: okhttp3.MultipartBody.Part
    ): Response<Unit>

    @Multipart
    @PUT("application-api/domain-slider/{sliderId}/item/{slideId}")
    suspend fun updateDomainSlideItem(
        @Path("sliderId") sliderId: String,
        @Path("slideId") slideId: String,
        @PartMap partMap: Map<String, @JvmSuppressWildcards okhttp3.RequestBody>,
        @Part file: okhttp3.MultipartBody.Part?
    ): Response<Unit>

    @DELETE("application-api/domain-slider/{id}")
    suspend fun deleteDomainSlider(
        @Path("id") sliderId: String
    ): Response<Unit>

    // --- Research API ---

    @GET("research-api/makes/getAll")
    suspend fun getResearchMakes(): List<com.slt.cardealership.domain.model.ResearchMake>

    @GET("research-api/models/getAll")
    suspend fun getResearchModels(
        @Query("make_id") makeId: Int
    ): List<com.slt.cardealership.domain.model.ResearchModel>

    @GET("research-api/modelyears/getAll")
    suspend fun getResearchModelYears(
        @Query("make_id") makeId: Int,
        @Query("model_id") modelId: Int
    ): List<com.slt.cardealership.domain.model.ResearchYear>

    @GET("research-api/modelyeartrims/getAll")
    suspend fun getResearchTrims(
        @Query("make_id") makeId: Int,
        @Query("model_id") modelId: Int,
        @Query("year") year: Int
    ): List<com.slt.cardealership.domain.model.ResearchTrim>

    // --- Create Blog ---

    @Multipart
    @POST("application-api/domain-blogs")
    suspend fun createDomainBlog(
        @PartMap partMap: Map<String, @JvmSuppressWildcards okhttp3.RequestBody>,
        @Part file: okhttp3.MultipartBody.Part? = null
    ): Response<Unit>

    // --- Get Blog Details ---

    @GET("application-api/domain-blogs/{id}")
    suspend fun getDomainBlogDetails(
        @Path("id") blogId: String
    ): com.slt.cardealership.domain.model.DomainBlogDetails

    @GET("dealer-api/dealer-social-profiles/{dealerId}")
    suspend fun getSocialProfiles(
        @Path("dealerId") dealerId: Long
    ): List<SocialProfileItem>

    @POST("dealer-api/dealer-social-profiles")
    suspend fun saveSocialProfiles(
        @Body request: SocialProfileRequest
    ): Response<Unit>

    // --- Update Blog ---

    @Multipart
    @PUT("application-api/domain-blogs/{id}")
    suspend fun updateDomainBlog(
        @Path("id") blogId: String,
        @PartMap partMap: Map<String, @JvmSuppressWildcards okhttp3.RequestBody>,
        @Part file: okhttp3.MultipartBody.Part? = null
    ): Response<Unit>

    @GET("application-api/domain-menus")
    suspend fun getDomainMenus(
        @Query("page") page: Int,
        @Query("item_per_page") itemsPerPage: Int,
        @Query("domain_id") domainId: Int,
        @Query("menu_name") menuName: String
    ): List<com.slt.cardealership.domain.model.DomainMenu>

    @GET("application-api/domain-menus/{id}")
    suspend fun getDomainMenuDetails(
        @Path("id") menuId: String
    ): com.slt.cardealership.domain.model.DomainMenuDetail

    @GET("application-api/domain-research-compare-interlinks")
    suspend fun getResearchCompareInterlinks(
        @Query("page") page: Int,
        @Query("item_per_page") itemsPerPage: Int,
        @Query("domain_id") domainId: Int
    ): List<com.slt.cardealership.domain.model.ResearchCompareItem>

    @GET("application-api/domain-research-blog-categories")
    suspend fun getResearchCompareCategories(
        @Query("domain_id") domainId: Int
    ): com.slt.cardealership.domain.model.ResearchCompareCategoryResponse

    @POST("application-api/domain-research-compare-interlinks")
    suspend fun createResearchCompare(
        @Body request: com.slt.cardealership.domain.model.CreateResearchCompareRequest
    ): Response<Unit>

    @DELETE("application-api/domain-research-compare-interlinks/{id}")
    suspend fun deleteResearchCompare(
        @Path("id") id: String
    ): Response<Unit>

    @GET("application-api/domain-research-compare-interlinks/{id}")
    suspend fun getResearchCompareDetails(
        @Path("id") id: String
    ): com.slt.cardealership.domain.model.ResearchCompareDetailsResponse

    @GET("dealer-api/dealers/{dealerId}/posts/GetPostCountByDomain")
    suspend fun getPostCountByDomain(
        @Path("dealerId") dealerId: Long
    ): List<com.slt.cardealership.domain.model.PostCountItem>

    @PUT("application-api/domain-research-compare-interlinks/{id}")
    suspend fun updateResearchCompare(
        @Path("id") id: String,
        @Body request: com.slt.cardealership.domain.model.CreateResearchCompareRequest
    ): Response<Unit>

    @GET("application-api/domain-theme-setting/{domainId}")
    suspend fun getDomainThemeSetting(@Path("domainId") domainId: Int): com.slt.cardealership.domain.model.DomainThemeSetting

    @PUT("application-api/domain-theme-setting/{domainId}")
    suspend fun saveDomainThemeSetting(
        @Path("domainId") domainId: Int,
        @Body settings: com.slt.cardealership.domain.model.DomainThemeSetting
    ): com.slt.cardealership.domain.model.DomainThemeSetting

    @Multipart
    @POST("application-api/domain-image-upload")
    suspend fun uploadDomainImage(
        @PartMap parts: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part file: MultipartBody.Part
    ): String

    @GET("application-api/domain-default-themes/getAll")
    suspend fun getDomainDefaultThemes(
        @Query("domain_id") domainId: Int
    ): List<com.slt.cardealership.domain.model.DomainDefaultTheme>

    // Inventory Settings APIs
    @GET("application-api/domain-vi-setting/{domainId}")
    suspend fun getDomainViSetting(
        @Path("domainId") domainId: Int
    ): com.slt.cardealership.domain.model.DomainInventorySetting

    @GET("application-api/domain-research-setting/{domainId}")
    suspend fun getDomainResearchSetting(
        @Path("domainId") domainId: Int
    ): com.slt.cardealership.domain.model.DomainResearchSetting

    @PUT("application-api/domain-research-setting/{domainId}")
    suspend fun saveDomainResearchSetting(
        @Path("domainId") domainId: Int,
        @Body settings: com.slt.cardealership.domain.model.DomainResearchSetting
    ): com.slt.cardealership.domain.model.DomainResearchSetting

    @GET("application-api/domain-fonts")
    suspend fun getDomainFonts(
        @Query("domain_id") domainId: Int
    ): com.slt.cardealership.domain.model.DomainFontsResponse

    @Multipart
    @POST("application-api/domain-fonts")
    suspend fun addDomainFont(
        @Part("domain_id") domainId: RequestBody,
        @Part("name") name: RequestBody,
        @Part file: MultipartBody.Part
    ): com.slt.cardealership.domain.model.DomainFont

    @DELETE("application-api/domain-fonts/{id}")
    suspend fun deleteDomainFont(
        @Path("id") fontId: Int
    ): Response<Unit>

    @GET("research-api/makes/getAll")
    suspend fun getAllMakes(): List<com.slt.cardealership.domain.model.Make>

    @GET("research-api/bodyTypes/getAll")
    suspend fun getAllBodyTypes(): List<com.slt.cardealership.domain.model.BodyType>

    @GET("application-api/domain-setting-makes")
    suspend fun getDomainSettingMakes(
        @Query("domain_id") domainId: Int,
        @Query("vehicle_module") vehicleModule: String,
        @Query("condition") condition: String?
    ): List<com.slt.cardealership.domain.model.DomainMakeSetting>

    @PUT("application-api/domain-vi-setting/{domainId}")
    suspend fun saveDomainViSetting(
        @Path("domainId") domainId: Int,
        @Body settings: com.slt.cardealership.domain.model.DomainInventorySetting
    ): com.slt.cardealership.domain.model.DomainInventorySetting

    @GET("application-api/domain-setting-body-types")
    suspend fun getDomainSettingBodyTypes(
        @Query("domain_id") domainId: Int,
        @Query("vehicle_module") vehicleModule: String
    ): List<com.slt.cardealership.domain.model.DomainBodyTypeSetting>

    @POST("application-api/domain-setting-makes")
    suspend fun saveDomainSettingMakes(
        @Body request: com.slt.cardealership.domain.model.SaveDomainMakesRequest
    ): List<com.slt.cardealership.domain.model.DomainMakeSetting>

    @POST("application-api/domain-setting-body-types")
    suspend fun saveDomainSettingBodyTypes(
        @Body request: com.slt.cardealership.domain.model.SaveDomainBodyTypesRequest
    ): List<com.slt.cardealership.domain.model.DomainBodyTypeSetting>

    // Contact Info APIs
    @GET("application-api/domain-contacts")
    suspend fun getDomainContacts(
        @Query("domain_id") domainId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): com.slt.cardealership.domain.model.ContactInfoListResponse

    @POST("application-api/domain-contacts")
    suspend fun createDomainContact(
        @Body request: com.slt.cardealership.domain.model.DomainContactRequest
    ): Response<Unit>

    @GET("application-api/domain-contacts/{id}")
    suspend fun getDomainContactDetails(
        @Path("id") id: Int
    ): com.slt.cardealership.domain.model.ContactDomainItem

    @PUT("application-api/domain-contacts/{id}")
    suspend fun updateDomainContact(
        @Path("id") id: Int,
        @Body request: com.slt.cardealership.domain.model.DomainContactRequest
    ): Response<Unit>

    @DELETE("application-api/domain-contacts/{id}")
    suspend fun deleteDomainContact(
        @Path("id") id: Int
    ): Response<Unit>

    // Social Info APIs
    @GET("application-api/domain-social-media")
    suspend fun getDomainSocialMedia(
        @Query("domain_id") domainId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): com.slt.cardealership.domain.model.SocialMediaListResponse

    @POST("application-api/domain-social-media")
    suspend fun saveDomainSocialMedia(
        @Body request: com.slt.cardealership.domain.model.SaveSocialMediaRequest
    ): Response<Unit>

    @GET("application-api/domain-social-media/{id}")
    suspend fun getDomainSocialMediaDetails(
        @Path("id") id: Int
    ): com.slt.cardealership.domain.model.SocialMediaItem

    @PUT("application-api/domain-social-media/{id}")
    suspend fun updateDomainSocialMedia(
        @Path("id") id: Int,
        @Body request: com.slt.cardealership.domain.model.SaveSocialMediaRequest
    ): Response<Unit>

    // General Settings API
    @GET("application-api/domain-app-setting/{domainId}")
    suspend fun getDomainAppSetting(
        @Path("domainId") domainId: Int
    ): com.slt.cardealership.domain.model.GeneralSettingsResponse

    @PUT("application-api/domain-app-setting/{domainId}")
    suspend fun updateDomainAppSetting(
        @Path("domainId") domainId: Int,
        @Body request: com.slt.cardealership.domain.model.UpdateGeneralSettingsRequest
    ): Response<Unit>

    // Menus
    @GET("application-api/domain-menus")
    suspend fun getDomainMenus(
        @Query("domain_id") domainId: Int
    ): List<com.slt.cardealership.domain.model.DomainMenu>

    @POST("application-api/domain-menus")
    suspend fun createDomainMenu(
        @Body request: com.slt.cardealership.domain.model.DomainMenuRequest
    ): Response<Unit>

    @PUT("application-api/domain-menus/{id}")
    suspend fun updateDomainMenu(
        @Path("id") id: String,
        @Body request: com.slt.cardealership.domain.model.DomainMenuRequest
    ): Response<Unit>

    @DELETE("application-api/domain-menus/{id}")
    suspend fun deleteDomainMenu(
        @Path("id") id: String
    ): Response<Unit>

    @GET("application-api/domain-pages")
    suspend fun getDomainPages(
        @Query("page") page: Int,
        @Query("items_per_page") itemsPerPage: Int,
        @Query("domain_id") domainId: Int
    ): com.slt.cardealership.domain.model.DomainPagesResponse
    @GET("dealer-api/dealer-gmb-auth/GetGMBSetting/{dealerId}")
    suspend fun getGmbSettings(
        @Path("dealerId") dealerId: Long
    ): com.slt.cardealership.domain.model.GmbSettingsResponse

    @GET("dealer-api/dealer-gmb/{dealerId}/media")
    suspend fun getGmbMedia(
        @Path("dealerId") dealerId: Long
    ): com.slt.cardealership.domain.model.GmbMediaResponse
}
