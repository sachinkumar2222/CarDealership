package com.slt.cardealership.data.repo

import android.util.Log
import com.slt.cardealership.data.remote.network.ApiService
import com.slt.cardealership.domain.model.AddSeoTagRequest
import com.slt.cardealership.domain.model.Advertisement
import com.slt.cardealership.domain.model.Amenities
import com.slt.cardealership.domain.model.SeoTag
import com.slt.cardealership.domain.model.Banner
import com.slt.cardealership.domain.model.VehicleGalleryResponse
import com.slt.cardealership.domain.model.VehicleOptionsResponse
import com.slt.cardealership.domain.model.AdvertisementGoalType
import com.slt.cardealership.domain.model.AdvertisementDomain
import com.slt.cardealership.domain.model.AdvertisementGoal
import com.slt.cardealership.domain.model.AdvertisementImage
import com.slt.cardealership.domain.model.EvoxImageResponse
import com.slt.cardealership.domain.model.DealerCategory
import com.slt.cardealership.domain.model.FaqItem
import com.slt.cardealership.domain.model.FaqDetails
import com.slt.cardealership.domain.model.VehicleModel
import com.slt.cardealership.domain.model.DealerInfo
import com.slt.cardealership.domain.model.DealerMetasResponse
import com.slt.cardealership.domain.model.FaqRequest
import com.slt.cardealership.domain.model.GalleryImage
import com.slt.cardealership.domain.model.HomeDelivery
import com.slt.cardealership.domain.model.HomeTestDrive
import com.slt.cardealership.domain.repo.DealerRepository
import com.slt.cardealership.domain.model.Vehicle
import com.slt.cardealership.domain.model.Post
import com.slt.cardealership.domain.model.VinRequest
import com.slt.cardealership.domain.model.TrimListResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import com.slt.cardealership.domain.model.GalleryImageUploadResponse
import com.slt.cardealership.domain.model.MapSeoTagsRequest
import com.slt.cardealership.domain.model.UpdateDomainsRequest
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject


class DealerRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
) : DealerRepository {


    override suspend fun getPosts(dealerId: Long): Result<List<Post>> {
        return try {
            val response = apiService.getPosts(dealerId)
            Result.success(response.list ?: emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDealerMetas(dealerId: Long): DealerMetasResponse {
        return apiService.getDealerMetas(dealerId)
    }

    override suspend fun updateDealerInfo(dealerId: Long, updateMap: Map<String, String>): Result<Unit> {
        return try {
            // This now correctly calls the @FormUrlEncoded function
            val response = apiService.updateDealerInfo(dealerId, updateMap)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(getErrorMessageFromResponse(response, "Failed to update info")))
            }
        } catch (e: Exception) {
            Result.failure(Exception(getErrorMessage(e)))
        }
    }

    override suspend fun updateDealerMetas(dealerId: Long, updateMap: Map<String, String>): Result<Unit> {
        return try {
            val response = apiService.updateDealerMetas(dealerId, updateMap)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                // --- LOGGING ADDED ---
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Log.e(
                    "DealerRepository",
                    "updateDealerMetas FAILED. Code: ${response.code()}, Error: $errorBody"
                )
                // ---------------------
                Result.failure(Exception(getErrorMessageFromResponse(response, "Failed to update metadata")))
            }
        } catch (e: Exception) {
            // --- LOGGING ADDED ---
            Log.e("DealerRepository", "updateDealerMetas CRASHED", e)
            // ---------------------
            Result.failure(Exception(getErrorMessage(e)))
        }
    }

    override suspend fun getCombinedDealerInfo(dealerId: Long): Result<DealerInfo> {
        return try {
            coroutineScope {
                // Launch API calls in parallel
                val detailsDeferred = async { apiService.getDealerDetails(dealerId) }
                val metasDeferred = async { apiService.getDealerMetas(dealerId) }
                val hoursDeferred = async { apiService.getDealerHours(dealerId) }

                // Wait for all calls to complete
                val details = detailsDeferred.await()
                val metas = metasDeferred.await()
                val hours = hoursDeferred.await()

                // --- THIS IS THE COMPLETE MAPPING LOGIC ---
                // It provides a value for every parameter in the DealerInfo data class
                val combinedInfo = DealerInfo(
                    id = details.id,
                    name = details.name,
                    phone = details.phone,
                    address = details.address,
                    city = details.cityName,
                    state = details.stateName,
                    zipCode = details.zipcodeName,
                    websiteUrl = details.websiteUrl,
                    aboutText = details.description,
                    isVirtual = details.isVirtual,
                    isClaimed = details.isClaimed,
                    headerImageUrl = details.imageUrl,
                    dealerType = when (details.dealerTypeId) {
                        1 -> "Franchise"
                        2 -> "Independent"
                        else -> "N/A"
                    },
                    dealerCategory = DealerCategory(
                        name = metas.category,
                        businessSegment = metas.businessSegment
                    ),
                    amenities = Amenities(
                        isEntrance = metas.isEntrance,
                        isRestroom = metas.isRestroom,
                        isSeating = metas.isSeating,
                        isParking = metas.parking,
                        isKidsPlayArea = metas.kidsPlayArea,
                        isWifi = metas.wifi
                    ),
                    dealerHours = hours,
                    homeDelivery = HomeDelivery(
                        isAvailable = metas.homeDelivery == "yes",
                        isNationWide = false,
                        radius = metas.homeDeliveryRadius ?: 0
                    ),
                    homeTestDrive = HomeTestDrive(
                        isAvailable = metas.homeTestDrive ?: false,
                        radius = metas.homeTestDriveRadius ?: 0
                    ),
                     // This comes from a separate /Gallery endpoint
                )
                Result.success(combinedInfo)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addBanner(
        dealerId: Long,
        title: String,
        url: String,
        startDate: String,
        imageFile: File
    ): Result<Unit> {
        return try {
            // Convert strings to RequestBody
            val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val urlPart = url.toRequestBody("text/plain".toMediaTypeOrNull())
            val startDatePart = startDate.toRequestBody("text/plain".toMediaTypeOrNull())

            // TODO: Replace "630" with the actual logged-in user ID
            val userPart = "630".toRequestBody("text/plain".toMediaTypeOrNull())

            // Convert file to RequestBody
            val imageReqBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("file", imageFile.name, imageReqBody)

            apiService.addBanner(
                dealerId = dealerId,
                title = titlePart,
                url = urlPart,
                startDate = startDatePart,
                image = imagePart,
                createdBy = userPart,
                updatedBy = userPart
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getBanners(dealerId: Long): Result<List<Banner>> {
        return try {
            val response = apiService.getBanners(dealerId)
            Result.success(response.list) // <-- FIX: Extract the list from the response object
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun getBannerDetails(dealerId: Long, bannerId: String): Result<Banner> {
        return try {
            Result.success(apiService.getBannerDetails(dealerId, bannerId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateBanner(
        dealerId: Long,
        bannerId: String,
        title: String,
        url: String,
        startDate: String,
        imageFile: File?
    ): Result<Unit> {
        return try {
            val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val urlPart = url.toRequestBody("text/plain".toMediaTypeOrNull())
            val startDatePart = startDate.toRequestBody("text/plain".toMediaTypeOrNull())
            val userPart = "630".toRequestBody("text/plain".toMediaTypeOrNull()) // TODO: Use real user ID

            val imagePart = imageFile?.let {
                val requestBody = it.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("file", it.name, requestBody)
            }

            apiService.updateBanner(
                dealerId = dealerId,
                bannerId = bannerId,
                title = titlePart,
                url = urlPart,
                startDate = startDatePart,
                image = imagePart,
                updatedBy = userPart
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getGalleryImages(dealerId: Long): Result<List<GalleryImage>> {
        return try {
            // 1. Get the single response object from the API
            val responseObject = apiService.getGalleryImages(dealerId)

            // 2. Transform the 'images' list from that object into the list the UI needs
            val imageList = responseObject.images?.map { imageUrl ->
                GalleryImage(id = responseObject.id, imageUrl = imageUrl)
            } ?: emptyList() // If 'images' is null, return an empty list

            Result.success(imageList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addGalleryImage(dealerId: Long, imageFile: File, existingUrls: List<String>): Result<Unit> {
        return try {
            val requestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("file", imageFile.name, requestBody)

            val urlsRequestBody = existingUrls.joinToString(",").toRequestBody("text/plain".toMediaTypeOrNull())

            val response = apiService.addGalleryImage(dealerId, urlsRequestBody, imagePart)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to add gallery image. Code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteBanner(dealerId: Long, bannerId: String): Result<Unit> {
        return try {
            val response = apiService.deleteBanner(dealerId, bannerId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete banner. Code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteGalleryImage(dealerId: Long, imageId: String): Result<Unit> {
        return try {
            val response = apiService.deleteGalleryImage(dealerId, imageId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete gallery image. Code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getVehicles(dealerId: Long): Result<List<Vehicle>> {
        return try {
            val response = apiService.getVehicles(dealerId)
            Result.success(response.list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getVehicleDetails(dealerId: Long, vehicleId: String): Result<Vehicle> {
        return try {
            val vehicle = apiService.getVehicleDetails(dealerId, vehicleId)
            Result.success(vehicle)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addVehicle(dealerId: Long, vehicle: Vehicle): Result<Vehicle> {
        return try {
            // This calls the NEW research-api endpoint
            val response = apiService.addVehicle(vehicle)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMessage = response.errorBody()?.string()?.let {
                    try {
                        val json = JSONObject(it)
                        json.optString("message", json.optString("error", "Failed to add vehicle"))
                    } catch (e: Exception) {
                        "Failed to add vehicle. Code: ${response.code()}"
                    }
                } ?: "Failed to add vehicle. Code: ${response.code()}"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateVehicle(dealerId: Long, vehicleId: String, vehicle: Vehicle): Result<Vehicle> {
        return try {
            val response = apiService.updateVehicle(dealerId, vehicleId, vehicle)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update vehicle. Code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteVehicle(dealerId: Long, vehicleId: String): Result<Unit> {
        return try {
            val response = apiService.deleteVehicle(dealerId, vehicleId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete vehicle. Code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun decodeVin(vin: String): Result<Vehicle> {
        return try {
            val response = apiService.decodeVin(vin)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to decode VIN. Code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getResearchVehicles(
        dealerId: Int,
        page: Int,
        itemsPerPage: Int
    ): Result<List<Vehicle>> {
        return try {
            val response = apiService.getResearchVehicles(
                dealerId = dealerId,
                page = page,
                itemsPerPage = itemsPerPage
                // Pass default or specified filters here too if needed
                // isActive = "yes",
                // isDeleted = "no"
            )
            // Assuming VehicleListResponse has 'list' and potentially 'pagination' info
            // You might want to return the whole VehicleListResponse instead of just List<Vehicle>
            // if you need pagination details in the ViewModel.
            Result.success(response.list ?: emptyList())
        } catch (e: Exception) {
            Result.failure(Exception(getErrorMessage(e))) // Use error message helper
        }
    }

    override suspend fun getResearchVehicleDetails(vehicleId: String): Result<Vehicle> {
        return try {
            val vehicle = apiService.getResearchVehicleDetails(vehicleId)
            Result.success(vehicle)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // We implement the PUT version. You can add the POST version if needed.
    override suspend fun editResearchVehicle(vehicleId: String, vehicle: Vehicle): Result<Vehicle> {
        return try {
            val response = apiService.editResearchVehiclePUT(vehicleId, vehicle)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update vehicle. Code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getVehicleGallery(vehicleId: String): Result<VehicleGalleryResponse> {
        return try {
            val response = apiService.getVehicleGallery(vehicleId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadVehicleGalleryImages(vehicleId: String, imageFiles: List<File>): Result<Unit> {
        return try {
            val imageParts = imageFiles.map { file ->
                val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("images", file.name, requestBody) // "images" is a guess
            }
            val response = apiService.uploadVehicleGalleryImages(vehicleId, imageParts)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to upload images. Code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadVehicleGallerySingleImage(imageFile: File): Result<GalleryImageUploadResponse> {
        return try {
            val requestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestBody) // "image" is a guess
            val response = apiService.uploadVehicleGallerySingleImage(imagePart)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to upload image. Code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTrimListPost(vin: String): Result<TrimListResponse> {
        return try {
            val request = VinRequest(vin = vin)
            val response = apiService.getTrimListPost(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTrimListByVin(vin: String): Result<TrimListResponse> {
        return try {
            val response = apiService.getTrimListByVin(vin)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getEvoxImages(vin: String): Result<EvoxImageResponse> {
        return try {
            val response = apiService.getEvoxImages(vin)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getVehicleOptionsAndPackages(vin: String): Result<VehicleOptionsResponse> {
        return try {
            val response = apiService.getVehicleOptionsAndPackages(vin)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDecodedDataByTrim(trimId: String): Result<Vehicle> {
        return try {
            val vehicle = apiService.getDecodedDataByTrim(trimId)
            Result.success(vehicle)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getErrorMessage(e: Exception): String {
        if (e is HttpException) {
            return try {
                // Try to parse the error body
                val errorBody = e.response()?.errorBody()?.string()
                if (errorBody.isNullOrEmpty()) {
                    e.message()
                } else {
                    // Try to get a 'message' or 'error' key from the JSON
                    val json = JSONObject(errorBody)
                    json.optString("message", json.optString("error", e.message()))
                }
            } catch (jsonError: Exception) {
                e.message() // Fallback to the standard HTTP message
            }
        }
        return e.message ?: "An unknown error occurred"
    }

    override suspend fun getModelsForMake(makeId: Int): Result<List<VehicleModel>> {
        return try {
            // Assumes ApiService returns List<VehicleModel> directly
            val models = apiService.getModelsForMake(makeId = makeId)
            Result.success(models)
            // If ApiService returns VehicleModelListResponse, use:
            // val response = apiService.getModelsForMake(makeId = makeId)
            // Result.success(response.list ?: emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun getAdvertisements(dealerId: Long): Result<List<Advertisement>> {
        return try {
            val response = apiService.getAdvertisements(dealerId)
            Result.success(response.list ?: emptyList()) // Handle null list
        } catch (e: Exception) {
            Result.failure(Exception(getErrorMessage(e)))
        }
    }

    override suspend fun getAdvertisementDetails(dealerId: Long, advertisementId: String): Result<Advertisement> {
        return try {
            val response = apiService.getAdvertisementDetails(dealerId, advertisementId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception(getErrorMessage(e)))
        }
    }

    override suspend fun deleteAdvertisement(dealerId: Long, advertisementId: String): Result<Unit> {
        return try {
            val response = apiService.deleteAdvertisement(dealerId, advertisementId)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception(getErrorMessageFromResponse(response, "Failed to delete advertisement")))
        } catch (e: Exception) {
            Result.failure(Exception(getErrorMessage(e)))
        }
    }

    override suspend fun getAdvertisementGoals(): Result<List<AdvertisementGoal>> {
        return try {
            val response = apiService.getAdvertisementGoals()
            Result.success(response ?: emptyList()) // Handle direct list response
        } catch (e: Exception) {
            Result.failure(Exception(getErrorMessage(e)))
        }
    }

    override suspend fun getAdvertisementGoalTypes(goalId: Int): Result<List<AdvertisementGoalType>> {
        return try {
            val response = apiService.getAdvertisementGoalTypes(goalId)
            Result.success(response ?: emptyList()) // Handle direct list response
        } catch (e: Exception) {
            Result.failure(Exception(getErrorMessage(e)))
        }
    }

    override suspend fun addAdvertisement(dealerId: Long, advertisement: Advertisement): Result<String> { // <-- FIX
        return try {
            val response = apiService.addAdvertisement(dealerId, advertisement)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!) // <-- FIX: Return the String
            } else {
                Result.failure(Exception(getErrorMessageFromResponse(response, "Failed to add advertisement")))
            }
        } catch (e: Exception) {
            Result.failure(Exception(getErrorMessage(e)))
        }
    }

    override suspend fun updateAdvertisement(dealerId: Long, advertisementId: String, advertisement: Advertisement): Result<Advertisement> {
        return try {
            val response = apiService.updateAdvertisement(dealerId, advertisementId, advertisement)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(getErrorMessageFromResponse(response, "Failed to update advertisement")))
            }
        } catch (e: Exception) {
            Result.failure(Exception(getErrorMessage(e)))
        }
    }

//    override suspend fun getAdvertisementDomains(dealerId: Long, advertisementId: String): Result<List<AdvertisementDomain>> {
////        return try {
////            val response = apiService.
////            //getAdvertisementDomains(dealerId, advertisementId)
////            Result.success(response.list ?: emptyList())
////        } catch (e: Exception) {
////            Result.failure(Exception(getErrorMessage(e)))
////        }
//        return null
//    }

    override suspend fun updateAdvertisementDomains(dealerId: Long, advertisementId: String, domainIds: List<String>): Result<Unit> {
        return try {
            val request = UpdateDomainsRequest(domainIds = domainIds)
            val response = apiService.updateAdvertisementDomains(dealerId, advertisementId, request)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception(getErrorMessageFromResponse(response, "Failed to update domains")))
        } catch (e: Exception) {
            Result.failure(Exception(getErrorMessage(e)))
        }
    }

    override suspend fun getAdvertisementGallery(dealerId: Long, advertisementId: String): Result<List<AdvertisementImage>> {
        return try {
            val response = apiService.getAdvertisementGallery(dealerId, advertisementId)
            Result.success(response.list ?: emptyList())
        } catch (e: Exception) {
            Result.failure(Exception(getErrorMessage(e)))
        }
    }

    override suspend fun updateAdvertisementGallery(dealerId: Long, advertisementId: String, imageFiles: List<File>): Result<Unit> {
        return try {
            val imageParts = imageFiles.map { file ->
                val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("images[]", file.name, requestBody) // Use images[] for array
            }
            val response = apiService.updateAdvertisementGallery(dealerId, advertisementId, imageParts)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception(getErrorMessageFromResponse(response, "Failed to update gallery")))
        } catch (e: Exception) {
            Result.failure(Exception(getErrorMessage(e)))
        }
    }

    // In DealerRepositoryImpl.kt

    // In DealerRepositoryImpl.kt

    // --- SEO Tags ---

    override suspend fun getSeoTags(dealerId: Long): Result<List<SeoTag>> { // <-- FIX: Added dealerId
        return try {
            // FIX: Pass dealerId and get the 'list' from the response object
            val response = apiService.getSeoTags(dealerId = dealerId)
            Result.success(response.list)
        } catch (e: Exception) {
            Result.failure(Exception(getErrorMessage(e)))
        }
    }

    override suspend fun deleteSeoTag(id: String): Result<Unit> { // <-- FIX: ID is a String
        return try {
            val response = apiService.deleteSeoTag(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(getErrorMessageFromResponse(response, "Failed to delete tag")))
            }
        } catch (e: Exception) {
            Result.failure(Exception(getErrorMessage(e)))
        }
    }

    override suspend fun addSeoTag(dealerId: Long, tagName: String, tagUrl: String): Result<Unit> { // <-- FIX: Added dealerId
        return try {
            // FIX: Pass all required fields
            val request = AddSeoTagRequest(tagName = tagName, tagUrl = tagUrl, dealerId = dealerId)
            val response = apiService.addSeoTag(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(getErrorMessageFromResponse(response, "Failed to add tag")))
            }
        } catch (e: Exception) {
            Result.failure(Exception(getErrorMessage(e)))
        }
    }

    override suspend fun getMappedSeoTags(dealerId: Long, domainId: Int): Result<List<SeoTag>> {
        return try {
            val response = apiService.getMappedSeoTags(dealerId, domainId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception(getErrorMessage(e)))
        }
    }

    override suspend fun mapSeoTagsToDomain(dealerId: Long, domainId: Int, tagIds: List<String>): Result<Unit> { // <-- FIX: ID is a String
        return try {
            val request = MapSeoTagsRequest(tagIds = tagIds)
            val response = apiService.mapSeoTagsToDomain(dealerId, domainId, request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(getErrorMessageFromResponse(response, "Failed to map tags")))
            }
        } catch (e: Exception) {
            Result.failure(Exception(getErrorMessage(e)))
        }
    }

    // In DealerRepositoryImpl.kt

    // ... (after updateAdvertisementGallery)

    // --- FAQ ---

    override suspend fun getFaqs(dealerId: Long, domainId: Int): Result<List<FaqItem>> {
        return try {
            // We pass the domainId and dealerId as required by the API
            val response = apiService.getFaqs(dealerId = dealerId, domainId = domainId)
            Result.success(response.list) // Extract the list from the wrapper
        } catch (e: Exception) {
            Result.failure(Exception(getErrorMessage(e)))
        }
    }

    override suspend fun getFaqDetails(faqId: Int): Result<FaqDetails> {
        return try {
            val response = apiService.getFaqDetails(faqId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception(getErrorMessage(e)))
        }
    }

    override suspend fun addFaq(faqRequest: FaqRequest): Result<Unit> {
        return try {
            val response = apiService.addFaq(faqRequest)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(getErrorMessageFromResponse(response, "Failed to add FAQ")))
            }
        } catch (e: Exception) {
            Result.failure(Exception(getErrorMessage(e)))
        }
    }

    override suspend fun updateFaq(faqId: Int, faqRequest: FaqRequest): Result<Unit> {
        return try {
            val response = apiService.updateFaq(faqId, faqRequest)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(getErrorMessageFromResponse(response, "Failed to update FAQ")))
            }
        } catch (e: Exception) {
            Result.failure(Exception(getErrorMessage(e)))
        }
    }

    override suspend fun deleteFaq(faqId: Int, type: String): Result<Unit> {
        return try {
            val response = apiService.deleteFaq(faqId, type)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(getErrorMessageFromResponse(response, "Failed to delete FAQ")))
            }
        } catch (e: Exception) {
            Result.failure(Exception(getErrorMessage(e)))
        }
    }

    // ... (before getErrorMessageFromResponse)

    private fun <T> getErrorMessageFromResponse(response: Response<T>, defaultMessage: String): String {
        return response.errorBody()?.string()?.let {
            try {
                // Try to parse the error as a JSON object
                val json = JSONObject(it)
                json.optString("message", json.optString("error", defaultMessage))
            } catch (e: Exception) {
                // If it's not a JSON object, it might be a plain string response
                it.ifBlank { "$defaultMessage. Code: ${response.code()}" }
            }
        } ?: "$defaultMessage. Code: ${response.code()}"
    }



}
