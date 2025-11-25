package com.slt.cardealership.data.repo

import android.net.Uri
import android.util.Log
import com.slt.cardealership.data.local.SessionManager
import com.slt.cardealership.data.remote.network.ApiService
import com.slt.cardealership.domain.model.AddSeoTagRequest
import com.slt.cardealership.domain.model.Designation
import com.slt.cardealership.domain.model.Advertisement
import com.slt.cardealership.domain.model.Amenities
import com.slt.cardealership.domain.model.SeoTag
import com.slt.cardealership.domain.model.SeoMenu
import com.slt.cardealership.domain.model.SeoCategory
import com.slt.cardealership.domain.model.Department
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.OpenableColumns
import com.slt.cardealership.domain.model.Banner
import com.slt.cardealership.domain.model.DetailedUserProfile
import com.slt.cardealership.domain.model.VehicleGalleryResponse
import com.slt.cardealership.domain.model.VehicleOptionsResponse
import com.slt.cardealership.domain.model.AdvertisementGoalType
import com.slt.cardealership.domain.model.AdvertisementGoal
import com.slt.cardealership.domain.model.AdvertisementImage
import com.slt.cardealership.domain.model.ChangePasswordRequest
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
import com.slt.cardealership.domain.model.InternetLeadsResponse
import com.slt.cardealership.domain.model.Post
import com.slt.cardealership.domain.model.VinRequest
import com.slt.cardealership.domain.model.TrimListResponse
import com.slt.cardealership.domain.model.ProductType
import com.slt.cardealership.domain.model.DealerService
import com.slt.cardealership.domain.model.DealerServicesRequest
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import com.slt.cardealership.domain.model.GalleryImageUploadResponse
import com.slt.cardealership.domain.model.ManageUsersResponse
import com.slt.cardealership.domain.model.MapSeoTagsRequest
import com.slt.cardealership.domain.model.ModifyDealerRequest
import com.slt.cardealership.domain.model.SeoMenuRequest
import com.slt.cardealership.domain.model.UpdateDomainsRequest
import com.slt.cardealership.domain.model.UpdateHoursRequest
import com.slt.cardealership.domain.model.UserProfileUpdateRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.inject.Inject


class DealerRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    val sessionManager : SessionManager,
    @ApplicationContext private val context: Context
) : DealerRepository {

    override suspend fun getDepartments(): Result<List<Department>> {
        return try {
            val response = apiService.getDepartments(roleType = "dealer")
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPosts(dealerId: Long): Result<List<Post>> {
        return try {
            val response = apiService.getPosts(dealerId)
            Result.success(response.list ?: emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun changeUserPassword(
        userId: Long,
        request: ChangePasswordRequest
    ): Result<Unit> {
        return try {
            val response = apiService.changeUserPassword(userId, request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to change password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUser(
        userId: Long,
        parts: Map<String, @JvmSuppressWildcards RequestBody>
    ): Result<DetailedUserProfile> {
        return try {
            // Call the ApiService function
            val response = apiService.putUserProfile(userId, parts)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSeoMenus(dealerId: Long): Result<List<SeoMenu>> {
        return try {
            val response = apiService.getSeoMenus(dealerId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSeoCategories(): Result<List<SeoCategory>> {
        return try {
            val response = apiService.getSeoCategories()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveSeoMenus(dealerId: Long, request: SeoMenuRequest): Result<Unit> {
        return try {
            val response = apiService.saveSeoMenus(dealerId, request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDealerMetas(dealerId: Long): DealerMetasResponse {
        return apiService.getDealerMetas(dealerId)
    }

    override suspend fun getFullUserProfile(): Result<DetailedUserProfile> {
        return try {
            // Step 1: Get the user ID from the authorization endpoint
            val authorizationResponse = apiService.getUserAuthorization()
            val userId = authorizationResponse.userId

            // Step 2: Use the userId to get the detailed profile
            val detailedProfileResponse = apiService.getDetailedUserProfile(userId)

            Result.success(detailedProfileResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // Helper function to convert any value to a 'text/plain' RequestBody
    private fun Any?.toTextRequestBody(): RequestBody {
        return (this?.toString() ?: "").toRequestBody("text/plain".toMediaType())
    }

    override suspend fun updateDealerInfoWithImage(
        dealerInfo: DealerInfo,
        newImageUri: Uri? // Image is optional
    ): Result<DealerInfo> {
        return try {
            // 1. "we fill other data from get"
            // We build the full map of text parts from the dealerInfo object.
            val parts = buildDealerPartsMap(dealerInfo)

            // 2. "use upload image as file in binary"
            // If a new image is provided, we create and add it to the map.
            if (newImageUri != null) {
                // Your API log shows the key is "file"
                val imagePart = createMultipartBodyPart(newImageUri, "file")
                parts[imagePart.first] = imagePart.second
            }

            // 3. Make the API call
            // We need a new ApiService function for this. Let's call it updateDealerInfoMultipart
            val response = apiService.updateDealerInfoMultipart(dealerInfo.id, parts)
            Result.success(response)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @Throws(IOException::class)
    private fun createMultipartBodyPart(uri: Uri, partName: String): Pair<String, RequestBody> {
        val contentResolver = context.contentResolver

        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
        } else {
            @Suppress("DEPRECATION")
            android.provider.MediaStore.Images.Media.getBitmap(contentResolver, uri)
        }

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val compressedFileBytes = outputStream.toByteArray()

        val filename = contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        } ?: "image.jpg"

        val requestFile = compressedFileBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())

        val formDataName = "$partName\"; filename=\"$filename"
        return Pair(formDataName, requestFile)
    }

    /**
     * A private helper to build the complete map of text fields
     * for the dealer update API, based on your payload log.
     */
    private fun buildDealerPartsMap(dealerInfo: DealerInfo): MutableMap<String, RequestBody> {
        val map = mutableMapOf<String, RequestBody>()

        // Add all fields from your payload log
        map["name"] = (dealerInfo.name ?: "").toTextRequestBody()
        map["slug"] = (dealerInfo.slug ?: "").toTextRequestBody()
        map["is_virtual"] = (if (dealerInfo.isVirtual == true) "1" else "0").toTextRequestBody()
        map["phone"] = (dealerInfo.phone ?: "").toTextRequestBody()
        map["website_url"] = (dealerInfo.websiteUrl ?: "").toTextRequestBody()
        map["address"] = (dealerInfo.address ?: "").toTextRequestBody()
        map["image_url"] = (dealerInfo.headerImageUrl ?: "").toTextRequestBody()
        map["description"] = (dealerInfo.description ?: "").toTextRequestBody()
        map["id"] = dealerInfo.id.toString().toTextRequestBody()
        map["is_claimed"] = (if (dealerInfo.isClaimed == true) "1" else "0").toTextRequestBody()
        map["updated_on"] = (System.currentTimeMillis() / 1000).toString().toTextRequestBody()
        map["zipcode_id"] = (dealerInfo.zipcodeId ?: 0).toString().toTextRequestBody()
        map["makes"] = "".toTextRequestBody()

        return map
    }


    override suspend fun addUser(
        firstName: String,
        lastName: String,
        username: String,
        password: String,
        phone: String?,
        imageUri: Uri?,
        departmentId: Int?,
        designationId: Int// <-- 1. ADD THIS PARAMETER
    ): Result<Long> {
        return try {
            // 2. Get data from SessionManager and Profile
            val dealerId = sessionManager.getDealerId() ?: throw Exception("Session expired: Dealer ID missing")
            val creatorProfile = getFullUserProfile().getOrThrow()

            val createdById = creatorProfile.id
            val organizationId = creatorProfile.organizationId

            // 3. Use hard-coded IDs from payload log (as you requested)
            val roleId = 9
            val designationId = 25

            val parts = mutableMapOf<String, @JvmSuppressWildcards RequestBody>()
            val timestamp = (System.currentTimeMillis() / 1000).toString()

            parts["first_name"] = firstName.toTextRequestBody()
            parts["last_name"] = lastName.toTextRequestBody()
            parts["username"] = username.toTextRequestBody()
            parts["password"] = password.toTextRequestBody()
            parts["phone"] = phone.toTextRequestBody()
            parts["dealer_id"] = dealerId.toString().toTextRequestBody()
            parts["group_id"] = "null".toTextRequestBody()
            parts["role_id"] = roleId.toString().toTextRequestBody()
            parts["created_by"] = createdById.toString().toTextRequestBody()
            parts["updated_by"] = createdById.toString().toTextRequestBody()
            parts["created_on"] = timestamp.toTextRequestBody()
            parts["updated_on"] = timestamp.toTextRequestBody()
            parts["organization_id"] = organizationId.toString().toTextRequestBody()

            // --- 4. THIS IS THE FIX ---
            // Use the parameter instead of a hard-coded value
            parts["department_id"] = departmentId.toString().toTextRequestBody()
            parts["designation_id"] = designationId.toString().toTextRequestBody()

            parts["is_active"] = "active".toTextRequestBody()

            // ... (imageUri handling is unchanged) ...
            if (imageUri != null) {
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val fileBytes = inputStream?.readBytes()
                inputStream?.close()
                if (fileBytes == null) throw Exception("Could not read image file")

                val mimeType = context.contentResolver.getType(imageUri)
                val requestFile = fileBytes.toRequestBody(mimeType?.toMediaTypeOrNull())
                parts["image_url"] = requestFile
            } else {
                parts["image_url"] = "".toTextRequestBody()
            }

            val response = apiService.addUser(parts)
            Result.success(response)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDesignations(departmentId: Int): Result<List<Designation>> {
        return try {
            val response = apiService.getDesignations(departmentId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(userId: Long, request: UserProfileUpdateRequest): Result<DetailedUserProfile> {

        // 1. Create the map that @PartMap expects
        val partMap = mutableMapOf<String, RequestBody>()

        partMap["first_name"] = request.firstName.toTextRequestBody()
        partMap["last_name"] = request.lastName.toTextRequestBody()
        partMap["username"] = request.username.toTextRequestBody()
        partMap["role_id"] = request.roleId.toTextRequestBody()
        partMap["created_by"] = request.createdBy.toTextRequestBody()
        partMap["created_on"] = request.createdOn.toTextRequestBody()
        partMap["updated_by"] = request.updatedBy.toTextRequestBody()
        partMap["updated_on"] = request.updatedOn.toTextRequestBody()
        partMap["organization_id"] = request.organizationId.toTextRequestBody()
        partMap["department_id"] = request.departmentId.toTextRequestBody()
        partMap["designation_id"] = request.designationId.toTextRequestBody()
        partMap["image_url"] = request.imageUrl.toTextRequestBody()
        partMap["dealer_id"] = request.dealerId.toTextRequestBody()
        partMap["dealername"] = request.dealerName.toTextRequestBody()
        partMap["gender"] = request.gender.toTextRequestBody()
        partMap["language"] = request.language.toTextRequestBody()
        partMap["phone"] = request.phone.toTextRequestBody()
        partMap["address"] = request.address.toTextRequestBody()
         partMap["dob"] = request.dob.toTextRequestBody()
         partMap["doj"] = request.doj.toTextRequestBody()

        // 3. Make the API call with the new 'partMap'
        return try {
            val response = apiService.putUserProfile(userId, partMap)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfileWithImage(
        userId: Long,
        parts: Map<String, @JvmSuppressWildcards RequestBody>
    ): Result<DetailedUserProfile> {
        return try {
            // This calls the SAME ApiService 'putUserProfile'
            // but passes the map instead of the data class.
            val response = apiService.putUserProfile(userId, parts)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUsers(
        page: Int,
        itemsPerPage: Int,
        roleId: Int
    ): Result<ManageUsersResponse> { // <-- This is kotlin.Result
        return try {
            // Get the dealer_id from your session
            val dealerId = sessionManager.getDealerId()
                ?: return Result.failure(Exception("User session not found")) // <-- FIX 1

            val response = apiService.getUsers(
                page = page,
                itemsPerPage = itemsPerPage,
                dealerId = dealerId.toLong(),
                roleId = roleId
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserDetails(userId: Long): Result<DetailedUserProfile> {
        return try {
            val response = apiService.getDetailedUserProfile(userId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
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

    // Change the implementation to convert the map to a list of parts
    override suspend fun updateDealerMetas(dealerId: Long, updateMap: Map<String, Any>): Result<Unit> {
        return try {
            val response = apiService.updateDealerMetas(dealerId, updateMap)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Failed to update metadata. Code: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(Exception(e.message))
        }
    }

    override suspend fun updateBusinessHours(dealerId: Long, request: UpdateHoursRequest): Result<Unit> {
        return try {
            val response = apiService.updateBusinessHours(dealerId, request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Log.e(
                    "DealerRepository",
                    "updateBusinessHours FAILED for ${request.hour_type}. Code: ${response.code()}, Error: $errorBody"
                )
                Result.failure(Exception(getErrorMessageFromResponse(response, "Failed to update hours")))
            }
        } catch (e: Exception) {
            Log.e("DealerRepository", "updateBusinessHours CRASHED for ${request.hour_type}", e)
            Result.failure(Exception(getErrorMessage(e)))
        }
    }

    override suspend fun requestDealerUpdate(request: ModifyDealerRequest): Result<Unit> {
        return try {
            val response = apiService.requestDealerUpdate(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Log.e(
                    "DealerRepository",
                    "requestDealerUpdate FAILED. Code: ${response.code()}, Error: $errorBody"
                )
                Result.failure(Exception(getErrorMessageFromResponse(response, "Failed to update info")))
            }
        } catch (e: Exception) {
            Log.e("DealerRepository", "requestDealerUpdate CRASHED", e)
            Result.failure(Exception(getErrorMessage(e)))
        }
    }

    private fun mapHomeDelivery(
        apiHomeDeliveryStatus: String?, // "no", "nation_wide", "radius"
        apiHomeDeliveryRadius: Int? // 0, 74, null
    ): HomeDelivery {
        val isAvailable = apiHomeDeliveryStatus != "no" && apiHomeDeliveryStatus != null
        val isNationWide = apiHomeDeliveryStatus == "nation_wide"
        val radius = if (apiHomeDeliveryStatus == "radius") {
            apiHomeDeliveryRadius ?: 0 // Default to 0 if API gives null unexpectedly for "radius"
        } else {
            0 // Radius is 0 for "no" or "nation_wide"
        }
        return HomeDelivery(isAvailable, isNationWide, radius)
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
                Log.d("DealerRepo", "Fetched DealerDetails: Description = ${details.description}")
                val metas = metasDeferred.await()
                val hours = hoursDeferred.await()
                val homeDeliveryMapped = mapHomeDelivery(metas.homeDelivery, metas.homeDeliveryRadius)

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
                    homeDelivery = homeDeliveryMapped,
                    homeTestDrive = HomeTestDrive(
                        isAvailable = metas.homeTestDrive ?: false,
                        radius = metas.homeTestDriveRadius ?: 0
                    ),
                    isVirtualAppointment = metas.virtualAppointment, // <-- MAP THIS FIELD
                    virtualAppointmentLink = metas.virtualAppointmentLink,
                    description = details.description,
                    createdOn = details.createdOn,
                    updatedOn = details.updatedOn,
                    slug = details.slug,
                    dealerStatusId = details.dealerStatusId,
                    latitude = details.latitude,
                    longitude = details.longitude,
                    zipcodeId = details.zipcodeId,
                    createdBy = details.createdBy,
                    isClient = details.isClient,
                    organizationId = details.organizationId,
                    updatedBy = details.updatedBy
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

            Result.success(response.list ?: emptyList())
        } catch (e: Exception) {
            Result.failure(Exception(getErrorMessage(e))) // Use error message helper
        }
    }

    override suspend fun saveDealerServices(
        dealerId: Int,
        request: DealerServicesRequest
    ): Result<Unit> {
        return try {
            val response = apiService.saveDealerServices(dealerId, request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
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

    override suspend fun getAllProductTypes(): Result<List<ProductType>> {
        return try {
            val response = apiService.getAllProductTypes()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDealerServices(dealerId: Int): Result<List<DealerService>> {
        return try {
            val response = apiService.getDealerServices(dealerId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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

    override suspend fun getInternetLeads(
        dealerId: Int,
        leadType: String,
        page: Int,
        itemsPerPage: Int
    ): Result<InternetLeadsResponse> {
        return try {
            val response = apiService.getInternetLeads(
                dealerId = dealerId,
                leadType = leadType,
                page = page,
                itemsPerPage = itemsPerPage
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



}
