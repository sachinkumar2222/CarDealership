package com.slt.cardealership.data.remote.network


import com.slt.cardealership.domain.model.AddSeoTagRequest
import com.slt.cardealership.domain.model.Advertisement
import com.slt.cardealership.domain.model.AdvertisementGalleryResponse
import com.slt.cardealership.domain.model.AdvertisementGoal
import com.slt.cardealership.domain.model.AdvertisementGoalType
import com.slt.cardealership.domain.model.AdvertisementListResponse
import com.slt.cardealership.domain.model.Banner
import com.slt.cardealership.domain.model.BannerListResponse
import com.slt.cardealership.domain.model.DealerDetailsResponse
import com.slt.cardealership.domain.model.DealerHours
import com.slt.cardealership.domain.model.DealerMetasResponse
import com.slt.cardealership.domain.model.EvoxImageResponse
import com.slt.cardealership.domain.model.FaqDetails
import com.slt.cardealership.domain.model.FaqListResponse
import com.slt.cardealership.domain.model.FaqRequest
import com.slt.cardealership.domain.model.GalleryImageUploadResponse
import com.slt.cardealership.domain.model.GalleryListResponse
import com.slt.cardealership.domain.model.GalleryResponseObject
import com.slt.cardealership.domain.model.MapSeoTagsRequest
import com.slt.cardealership.domain.model.Post
import com.slt.cardealership.domain.model.PostListResponse
import com.slt.cardealership.domain.model.SeoTag
import com.slt.cardealership.domain.model.SeoTagListResponse
import com.slt.cardealership.domain.model.TrimListResponse
import com.slt.cardealership.domain.model.UpdateDomainsRequest
import com.slt.cardealership.domain.model.Vehicle
import com.slt.cardealership.domain.model.VehicleGalleryResponse
import com.slt.cardealership.domain.model.VehicleListResponse
import com.slt.cardealership.domain.model.VehicleModel
import com.slt.cardealership.domain.model.VehicleOptionsResponse
import com.slt.cardealership.domain.model.VinRequest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @FormUrlEncoded // <-- FIX 1: Change to @Multipart
    @PATCH("dealer-api/Dealers/{dealerId}")
    suspend fun updateDealerInfo(
        @Path("dealerId") dealerId: Long,
        @FieldMap body: Map<String, String> // <-- FIX 2: Use @PartMap and RequestBody
    ): Response<Unit>

    @FormUrlEncoded // <-- FIX 1: Use @Multipart
    @PATCH("dealer-api/dealer-metas/{dealerId}")
    suspend fun updateDealerMetas(
        @Path("dealerId") dealerId: Long,
        @FieldMap body: Map<String, String>
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
        @Part("start_date") startDate: RequestBody,
        @Part image: MultipartBody.Part,
        // The API requires these fields, we can send default/current values
        @Part("created_by") createdBy: RequestBody,
        @Part("updated_by") updatedBy: RequestBody,
        @Part("domain_id") domainId: RequestBody = "".toRequestBody("text/plain".toMediaTypeOrNull())
    ): Response<Unit>


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
    suspend fun getBanners(@Path("dealerId") dealerId: Long): BannerListResponse

    @Multipart
    @PUT("dealer-api/dealers/{dealerId}/banners/{bannerId}")
    suspend fun updateBanner(
        @Path("dealerId") dealerId: Long,
        @Path("bannerId") bannerId: String,
        @Part("title") title: RequestBody,
        @Part("url") url: RequestBody,
        @Part("start_date") startDate: RequestBody,
        @Part image: MultipartBody.Part?, // Image is optional on update
        @Part("updated_by") updatedBy: RequestBody,
        @Part("domain_id") domainId: RequestBody = "".toRequestBody("text/plain".toMediaTypeOrNull())
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
        @Part images: List<MultipartBody.Part>
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


    //advertisement


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

    // --- SEO Tags ---

    /**
     * Get the master list of all available SEO tags
     * GET {base_url}/dealer-api/dealer-seo-tags
     */
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
    ): FaqListResponse // Returns the paginated wrapper

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

}
