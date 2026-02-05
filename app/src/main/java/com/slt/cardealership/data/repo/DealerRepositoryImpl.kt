package com.slt.cardealership.data.repo

import android.net.Uri
import android.util.Log
import com.slt.cardealership.data.local.SessionManager
import com.slt.cardealership.data.remote.network.ApiService
import com.slt.cardealership.domain.repo.DealerRepository
import com.slt.cardealership.domain.model.AddSeoTagRequest
import com.slt.cardealership.domain.model.Designation
import com.slt.cardealership.domain.model.Advertisement
import com.slt.cardealership.domain.model.Amenities
import com.slt.cardealership.domain.model.SeoTag
import com.slt.cardealership.domain.model.SeoMenu
import com.slt.cardealership.domain.model.SeoCategory
import com.slt.cardealership.domain.model.Department
import com.slt.cardealership.domain.model.DealerService
import android.content.Context
import com.slt.cardealership.domain.model.BannerListResponse
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
import com.slt.cardealership.domain.model.DealerServicesRequest
import com.slt.cardealership.domain.model.InternetLeadsResponse
import kotlinx.coroutines.async
import com.slt.cardealership.domain.model.UpdateHoursRequest
import com.slt.cardealership.domain.model.UserProfileUpdateRequest
import com.slt.cardealership.domain.model.UserProfile
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import com.slt.cardealership.domain.model.ModifyDealerRequest
import com.slt.cardealership.domain.model.GalleryImageUploadResponse
import com.slt.cardealership.domain.model.ManageUsersResponse
import com.slt.cardealership.domain.model.ManageUsers
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import kotlinx.coroutines.coroutineScope
import com.slt.cardealership.domain.model.CreateResearchBlogCategoryRequest
import com.slt.cardealership.domain.model.CreateResearchCompareRequest
import com.slt.cardealership.domain.model.ResearchBlogCategoryResponse
import com.slt.cardealership.domain.model.UpdateResearchBlogCategoryRequest


import com.slt.cardealership.domain.model.ResearchMake
import com.slt.cardealership.domain.model.ResearchModel
import com.slt.cardealership.domain.model.ResearchTrim
import com.slt.cardealership.domain.model.ResearchYear
import com.slt.cardealership.domain.model.VinRequest
import com.slt.cardealership.domain.model.TrimListResponse
import com.slt.cardealership.domain.model.HomeDelivery
import com.slt.cardealership.domain.model.HomeTestDrive
import com.slt.cardealership.domain.model.Post
import com.slt.cardealership.domain.model.SocialProfileItem
import com.slt.cardealership.domain.model.SocialProfileRequest
import com.slt.cardealership.domain.model.Vehicle
import com.slt.cardealership.domain.model.VehicleListResponse
import com.slt.cardealership.domain.model.ProductType
import com.slt.cardealership.domain.model.FaqRequest
import com.slt.cardealership.domain.model.GalleryImage
import com.slt.cardealership.domain.model.DealerMetasResponse
import com.slt.cardealership.domain.model.SeoMenuRequest
import com.slt.cardealership.domain.model.UpdateDomainsRequest
import com.slt.cardealership.domain.model.MapSeoTagsRequest
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

    override suspend fun getClassifiedPosts(
        dealerId: Long,
        page: Int,
        itemsPerPage: Int,
        orderBy: String?,
        order: String?,
        status: String?,
        name: String?,
        haveContent: String?,
        haveLinks: String?,
        haveImage: String?,
        domainName: String?
    ): Result<List<Post>> {
        return try {
            val response = apiService.getClassifiedPosts(
                dealerId, page, itemsPerPage, orderBy, order, status, name, haveContent, haveLinks, haveImage, domainName
            )
            Result.success(response.list ?: emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getArticleLinks(dealerId: Long): Result<List<com.slt.cardealership.domain.model.ArticleLink>> {
        return try {
            val response = apiService.getArticleLinks(dealerId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addUser(
        firstName: String,
        lastName: String,
        username: String,
        password: String,
        phone: String?,
        imageUri: Uri?,
        departmentId: Int?,
        designationId: Int
    ): Result<Long> {
        return try {
            val partMap = mutableMapOf<String, RequestBody>()

            fun addPart(key: String, value: String) {
                partMap[key] = value.toRequestBody("text/plain".toMediaTypeOrNull())
            }

            addPart("first_name", firstName)
            addPart("last_name", lastName)
            addPart("username", username)
            addPart("password", password)
            phone?.let { addPart("phone", it) }
            departmentId?.let { addPart("department_id", it.toString()) }
            addPart("designation_id", designationId.toString())

            imageUri?.let { uri ->
                try {
                    val contentResolver = context.contentResolver
                    val type = contentResolver.getType(uri) ?: "image/*"
                    val inputStream = contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()

                    if (bytes != null) {
                        val requestFile = bytes.toRequestBody(type.toMediaTypeOrNull())
                        // Using a hack to send filename in PartMap if ApiService doesn't use @Part MultipartBody.Part
                        partMap["image\"; filename=\"profile.jpg"] = requestFile
                    }else{}
                } catch (e: Exception) {
                    Log.e("DealerRepository", "Error reading image uri", e)
                }
            }

            val response = apiService.addUser(partMap)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserAuthorization(): Result<UserProfile> {
        return try {
            val response = apiService.getUserAuthorization()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception(getErrorMessage(e)))
        }
    }

    override suspend fun updateDealerInfoWithImage(
        dealerInfo: DealerInfo,
        newImageUri: Uri?
    ): Result<DealerInfo> {
        return try {
            val partMap = mutableMapOf<String, RequestBody>()

            fun addPart(key: String, value: String?) {
                if (value != null) {
                    partMap[key] = value.toRequestBody("text/plain".toMediaTypeOrNull())
                }
            }

            addPart("name", dealerInfo.name)
            addPart("phone", dealerInfo.phone)
            addPart("website_url", dealerInfo.websiteUrl)
            addPart("address", dealerInfo.address)
            addPart("description", dealerInfo.description)
            addPart("city_name", dealerInfo.city)
            addPart("state_name", dealerInfo.state)
            addPart("zip_code", dealerInfo.zipCode)
            addPart("dealer_type", dealerInfo.dealerType)

            newImageUri?.let { uri ->
                try {
                    val contentResolver = context.contentResolver
                    val type = contentResolver.getType(uri) ?: "image/*"
                    val inputStream = contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()

                    if (bytes != null) {
                        val requestFile = bytes.toRequestBody(type.toMediaTypeOrNull())
                        partMap["image\"; filename=\"dealer_image.jpg\""] = requestFile
                    }else{}
                } catch (e: Exception) {
                    Log.e("DealerRepository", "Error reading image uri", e)
                }
            }

            val response = apiService.updateDealerInfoMultipart(dealerInfo.id.toLong(), partMap)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception(getErrorMessage(e)))
        }
    }

    override suspend fun getUsers(page: Int, itemsPerPage: Int, dealerId: Long, roleId: Int): Result<ManageUsersResponse> {
        return try {
            val response = apiService.getUsers(page, itemsPerPage, dealerId, roleId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllUsers(dealerId: Long): Result<List<ManageUsers>> {
        return try {
            val response = apiService.getAllUsers(dealerId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getClassifiedSites(
        page: Int,
        itemsPerPage: Int,
        productTypeId: Int
    ): Result<com.slt.cardealership.domain.model.DomainResponse> {
        return try {
            val response = apiService.getDomains(
                page = page,
                itemsPerPage = itemsPerPage,
                productTypeId = productTypeId,
                inProduction = true,
                inBusinessListing = true
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSeoDomains(
        productTypeId: Int,
        domainName: String?
    ): Result<List<com.slt.cardealership.domain.model.SeoDomain>> {
        return try {
            val response = apiService.getSeoDomains(productTypeId, domainName)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDomainAcceptedTags(
        dealerId: Long,
        domainId: Int
    ): Result<List<String>> {
        return try {
            val response = apiService.getDomainAcceptedTags(dealerId, domainId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveDomainSeoTags(
        request: com.slt.cardealership.domain.model.SeoDomainMapRequest
    ): Result<Unit> {
        return try {
            val response = apiService.saveDomainSeoTags(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to save tags: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPostCounts(dealerId: Long): Result<List<com.slt.cardealership.domain.model.PostCountItem>> {
        return try {
            val response = apiService.getPostCountByDomain(dealerId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDealerDetailPageSlug(domainId: Int): Result<String> {
        return try {
            val pageTypes = apiService.getPageTypes("dealer-detail")
            val dealerDetailPageTypeId = pageTypes.firstOrNull()?.id
                ?: return Result.failure(Exception("Dealer Detail Page Type not found"))

            val domainPages = apiService.getDomainPages(
                page = 1,
                itemsPerPage = 1,
                domainId = domainId,
                pageTypeId = dealerDetailPageTypeId
            )

            val pageSlug = domainPages.list.firstOrNull()?.pageSlug
                ?: return Result.failure(Exception("Dealer Profile Page not found"))

            Result.success(pageSlug)
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

    override suspend fun getDesignations(departmentId: Int): Result<List<Designation>> {
        return try {
            val response = apiService.getDesignations(departmentId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(
        userId: Long,
        request: UserProfileUpdateRequest
    ): Result<DetailedUserProfile> {
        return try {
            val partMap = mutableMapOf<String, RequestBody>()
            fun addPart(key: String, value: String?) {
                if (value != null) {
                    partMap[key] = value.toRequestBody("text/plain".toMediaTypeOrNull())
                }
            }

            addPart("first_name", request.firstName)
            addPart("last_name", request.lastName)
            addPart("phone", request.phone)
            addPart("designation_id", request.designationId?.toString())
            addPart("department_id", request.departmentId?.toString())

            request.imageFile?.let { file ->
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                partMap["image\"; filename=\"${file.name}"] = requestFile
            }

            val response = apiService.putUserProfile(userId, partMap)
            Result.success(response)
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

    override suspend fun changeMyPassword(
        userId: Long,
        request: ChangePasswordRequest
    ): Result<Unit> {
        return try {
            val response = apiService.changeMyPassword(userId, request)
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
            val response = apiService.putUserProfile(userId, parts)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
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

    override suspend fun getSocialProfiles(dealerId: Long): Result<List<SocialProfileItem>> {
        return try {
            val response = apiService.getSocialProfiles(dealerId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveSocialProfiles(dealerId: Long, items: List<SocialProfileItem>): Result<Unit> {
        return try {
            val userId = sessionManager.getDealerId()?.toLong() ?: 630L
            val request = SocialProfileRequest(
                id = dealerId,
                social = items,
                createdBy = userId,
                createdOn = System.currentTimeMillis()
            )
            val response = apiService.saveSocialProfiles(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to save social profiles. Code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
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
        domainId: Int?,
        title: String,
        url: String,
        startDate: Long,
        endDate: Long?,
        imageFile: File
    ): Result<Unit> {
        return try {
            val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val urlPart = url.toRequestBody("text/plain".toMediaTypeOrNull())
            // Fix: Send empty string for null domainId
            val domainIdVal = domainId?.toString() ?: ""
            val domainIdPart = domainIdVal.toRequestBody("text/plain".toMediaTypeOrNull())
            val startDatePart = startDate.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val endDatePart = endDate?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())

            // Fetch dynamic User ID
            val authResponse = apiService.getUserAuthorization()
            val userId = authResponse.userId
            val userPart = userId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val timestampPart = (System.currentTimeMillis() / 1000).toString().toRequestBody("text/plain".toMediaTypeOrNull())

            val requestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("file", imageFile.name, requestBody)

            apiService.addBanner(
                dealerId = dealerId,
                title = titlePart,
                url = urlPart,
                domainId = domainIdPart,
                startDate = startDatePart,
                endDate = endDatePart,
                image = imagePart,
                createdBy = userPart,
                updatedBy = userPart,
                createdOn = timestampPart,
                updatedOn = timestampPart
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getBanners(
        dealerId: Long,
        page: Int,
        itemsPerPage: Int,
        domainId: Int?
    ): Result<BannerListResponse> {
        return try {
            val response = apiService.getBanners(dealerId, page, itemsPerPage, domainId)
            Result.success(response)
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
        domainId: Int?,
        title: String,
        url: String,
        startDate: Long,
        endDate: Long?,
        imageFile: File?,
        imageUrl: String?,
        createdBy: String?
    ): Result<Unit> {
        return try {
            val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val urlPart = url.toRequestBody("text/plain".toMediaTypeOrNull())
            // Fix: Send empty string for null domainId
            val domainIdVal = domainId?.toString() ?: ""
            val domainIdPart = domainIdVal.toRequestBody("text/plain".toMediaTypeOrNull())
            val startDatePart = startDate.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val endDatePart = endDate?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())

            // Fetch dynamic User ID
            val authResponse = apiService.getUserAuthorization()
            val userId = authResponse.userId

            // Logic to preserve creator or default to current user
            val creatorIdVal = if (!createdBy.isNullOrBlank()) createdBy else userId.toString()
            val userPart = userId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val creatorPart = creatorIdVal.toRequestBody("text/plain".toMediaTypeOrNull())

            val idPart = bannerId.toRequestBody("text/plain".toMediaTypeOrNull())
            val timestampPart = (System.currentTimeMillis() / 1000).toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val imageUrlPart = imageUrl?.toRequestBody("text/plain".toMediaTypeOrNull())

            val imagePart = imageFile?.let {
                val requestBody = it.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("file", it.name, requestBody)
            }


            apiService.updateBanner(
                dealerId = dealerId,
                bannerId = bannerId,
                id = idPart,
                title = titlePart,
                url = urlPart,
                domainId = domainIdPart,
                startDate = startDatePart,
                endDate = endDatePart,
                image = imagePart,
                imageUrl = imageUrlPart,
                createdBy = creatorPart,
                updatedBy = userPart,
                updatedOn = timestampPart
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
            } ?: emptyList<GalleryImage>() // If 'images' is null, return an empty list

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

    override suspend fun updateGalleryImages(dealerId: Long, imageUrls: List<String>): Result<Unit> {
        return try {
            val urlsRequestBody = imageUrls.joinToString(",").toRequestBody("text/plain".toMediaTypeOrNull())
            val response = apiService.updateGalleryImages(dealerId, urlsRequestBody)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update gallery images. Code: ${response.code()}"))
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
            val response: VehicleListResponse = apiService.getVehicles(dealerId)
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
        itemsPerPage: Int,
        orderBy: String?,
        order: String?
    ): Result<List<Vehicle>> {
        return try {
            val response = apiService.getResearchVehicles(
                dealerId = dealerId,
                page = page,
                itemsPerPage = itemsPerPage,
                orderBy = orderBy,
                order = order
            )
            Result.success(response.list)
        } catch (e: Exception) {
            Result.failure(e)
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

    override suspend fun uploadVehicleGalleryImages(vehicleId: String, imageFiles: List<File>, existingImageUrls: List<String>): Result<Unit> {
        return try {
            val imageParts = imageFiles.map { file ->
                val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("images", file.name, requestBody)
            }

            // Convert list of URLs to comma-separated string (or whatever the API expects for "image_urls")
            // Based on addGalleryImage usage: urlsRequestBody = existingUrls.joinToString(",").toRequestBody(...)
            val urlsString = existingImageUrls.joinToString(",")
            val urlsRequestBody = urlsString.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = apiService.uploadVehicleGalleryImages(vehicleId, imageParts, urlsRequestBody)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to upload images. Code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteVehicleGalleryImage(vehicleId: String, imageUrl: String): Result<Unit> {
        return try {
            // 1. Get current gallery to filter the list
            val currentGalleryResult = getVehicleGallery(vehicleId)

            if (currentGalleryResult.isSuccess) {
                val currentImages = currentGalleryResult.getOrNull()?.images ?: emptyList()
                // 2. Filter out the image to delete
                val updatedImages = currentImages.filter { it != imageUrl }

                // 3. Update the gallery on server
                // We pass vehicleId as the path param, and also as the field "vehicle_id"
                // and the list of images as "images[]"
                val response = apiService.updateVehicleGallery(
                    vehicleId = vehicleId,
                    formVehicleId = vehicleId,
                    imageUrls = updatedImages.joinToString(",")
                )

                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to delete image. Code: ${response.code()}"))
                }
            } else {
                Result.failure(Exception("Failed to fetch gallery for deletion."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteVehicleGalleryImages(vehicleId: String, imageUrls: List<String>): Result<Unit> {
        return try {
            val currentGalleryResult = getVehicleGallery(vehicleId)

            if (currentGalleryResult.isSuccess) {
                val currentImages = currentGalleryResult.getOrNull()?.images ?: emptyList()
                // Filter out ALL images provided in the deletion list
                val updatedImages = currentImages.filter { !imageUrls.contains(it) }

                val response = apiService.updateVehicleGallery(
                    vehicleId = vehicleId,
                    formVehicleId = vehicleId,
                    imageUrls = updatedImages.joinToString(",")
                )

                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to delete images. Code: ${response.code()}"))
                }
            } else {
                Result.failure(Exception("Failed to fetch gallery for deletion."))
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

    override suspend fun createDomainSlider(name: String, domainId: String): Result<String> {
        return try {
            val request = com.slt.cardealership.domain.model.DomainSliderCreateRequest(
                name = name,
                sqlDomainId = domainId
            )
            val response = apiService.createDomainSlider(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.id)
            } else {
                Result.failure(Exception("Failed to create slider. Code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDomainSliderDetails(sliderId: String): Result<com.slt.cardealership.domain.model.DomainSliderDetails> {
        return try {
            val response = apiService.getDomainSliderDetails(sliderId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addDomainSlideItem(
        sliderId: String,
        domainId: String,
        title: String,
        link: String,
        target: String,
        startDate: Long,
        endDate: Long,
        imageFile: java.io.File
    ): Result<Unit> {
        return try {
            val partMap = mutableMapOf<String, RequestBody>()
            fun addPart(key: String, value: String) {
                partMap[key] = value.toRequestBody("text/plain".toMediaTypeOrNull())
            }

            addPart("sql_domain_id", domainId)
            addPart("title", title)
            addPart("link", link)
            addPart("target", target)
            // API expects timestamp in seconds
            addPart("start_date", (startDate / 1000).toString())
            addPart("end_date", (endDate / 1000).toString())

            val requestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("file", imageFile.name, requestBody)

            val response = apiService.addDomainSlideItem(sliderId, partMap, imagePart)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to add slide item. Code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteDomainSlider(sliderId: String): Result<Unit> {
        return try {
            val response = apiService.deleteDomainSlider(sliderId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete slider. Code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateDomainSlideItem(
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
    ): Result<Unit> {
        return try {
            val partMap = mutableMapOf<String, RequestBody>()
            fun addPart(key: String, value: String) {
                partMap[key] = value.toRequestBody("text/plain".toMediaTypeOrNull())
            }

            addPart("sql_domain_id", domainId)
            addPart("title", title)
            addPart("link", link)
            addPart("target", target)
            // API expects timestamp in seconds
            addPart("start_date", (startDate / 1000).toString())
            addPart("end_date", (endDate / 1000).toString())

            if (imageUrl != null) {
                addPart("image_url", imageUrl)
            }

            val imagePart = if (imageFile != null) {
                val requestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("file", imageFile.name, requestBody)
            } else {
                null
            }

            val response = apiService.updateDomainSlideItem(sliderId, slideId, partMap, imagePart)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update slide item. Code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun getDomainBlogDetails(blogId: String): Result<com.slt.cardealership.domain.model.DomainBlogDetails> {
        return try {
            val response = apiService.getDomainBlogDetails(blogId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateDomainBlog(
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
        file: File?
    ): Result<Unit> {
        return try {
            val partMap = mutableMapOf<String, RequestBody>()
            fun addPart(key: String, value: String) {
                partMap[key] = value.toRequestBody("text/plain".toMediaTypeOrNull())
            }

            addPart("title", title)
            addPart("slug", slug)
            addPart("description", description)
            addPart("meta_title", metaTitle)
            addPart("meta_description", metaDescription)
            addPart("blog_type", blogType)
            addPart("status", status)
            addPart("sql_domain_id", domainId.toString())
            addPart("cta", cta)
            addPart("research_compare", researchCompare)
            addPart("category", category)
            addPart("short_description", shortDescription)
            addPart("created_by", createdBy.toString())
            addPart("updated_by", updatedBy.toString())

            val filePart = file?.let {
                val requestBody = it.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("file", it.name, requestBody)
            }

            val response = apiService.updateDomainBlog(blogId, partMap, filePart)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update blog. Code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getResearchMakes(): Result<List<ResearchMake>> {
        return try {
            val response = apiService.getResearchMakes()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getResearchModels(makeId: Int): Result<List<ResearchModel>> {
        return try {
            val response = apiService.getResearchModels(makeId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getResearchModelYears(makeId: Int, modelId: Int): Result<List<ResearchYear>> {
        return try {
            val response = apiService.getResearchModelYears(makeId, modelId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getResearchTrims(makeId: Int, modelId: Int, year: Int): Result<List<ResearchTrim>> {
        return try {
            val response = apiService.getResearchTrims(makeId, modelId, year)
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
            Result.success(response.list ?: emptyList())
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
            Result.success(response ?: emptyList())
        } catch (e: Exception) {
            Result.failure(Exception(getErrorMessage(e)))
        }
    }


    override suspend fun getAdvertisementGoalTypes(goalId: Int): Result<List<AdvertisementGoalType>> {
        return try {
            val response = apiService.getAdvertisementGoalTypes(goalId)
            Result.success(response ?: emptyList())
        } catch (e: Exception) {
            Result.failure(Exception(getErrorMessage(e)))
        }
    }


    override suspend fun addAdvertisement(dealerId: Long, advertisement: Advertisement): Result<String> {
        return try {
            val response = apiService.addAdvertisement(dealerId, advertisement)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
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

    override suspend fun updateAdvertisementDomains(dealerId: Long, advertisementId: String, domainIds: List<Int>): Result<Unit> {
        return try {
            val request = UpdateDomainsRequest(list = domainIds)
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
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception(getErrorMessage(e)))
        }
    }


    override suspend fun updateAdvertisementGallery(dealerId: Long, advertisementId: String, imageFiles: List<File>): Result<Unit> {
        return try {
            val imageParts = imageFiles.map { file ->
                val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("images[]", file.name, requestBody)
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
                Result.failure(Exception("Failed to save SEO menus. Code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFaqs(dealerId: Long, domainId: Int, domainName: String?): Result<List<FaqItem>> {
        return try {
            // We pass the domainId, dealerId and optional domainName
            val response = apiService.getFaqsWithDomain(dealerId = dealerId, domainId = domainId, domainName = domainName)
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

    override suspend fun getDomains(
        page: Int,
        itemsPerPage: Int,
        dealerId: Long
    ): Result<com.slt.cardealership.domain.model.DomainResponse> {
        return try {
            val response = apiService.getDomains(
                page = page,
                itemsPerPage = itemsPerPage,
                dealerId = dealerId
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDomainDetails(domainId: Int): Result<com.slt.cardealership.domain.model.DomainItem> {
        return try {
            val response = apiService.getDomainDetails(domainId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteDomainPage(pageId: String): Result<Unit> {
        return try {
            val response = apiService.deleteDomainPage(pageId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(getErrorMessageFromResponse(response, "Failed to delete page")))
            }
        } catch (e: Exception) {
            Result.failure(Exception(getErrorMessage(e)))
        }
    }

    override suspend fun updateDomainPage(request: com.slt.cardealership.domain.model.DomainPageUpdateRequest): Result<Unit> {
        return try {
            val builder = okhttp3.MultipartBody.Builder().setType(okhttp3.MultipartBody.FORM)

            // Helper to add parts
            fun addPart(key: String, value: Any?) {
                if (value != null) {
                    builder.addFormDataPart(key, value.toString())
                }
            }

            addPart("id", request.id)
            addPart("page_name", request.pageName)
            addPart("page_slug", request.pageSlug)
            addPart("status", request.status)
            addPart("sql_domain_id", request.sqlDomainId)
            addPart("page_type_id", request.pageTypeId)
            addPart("created_by", request.createdBy)
            addPart("created_on", request.createdOn)
            addPart("updated_by", request.updatedBy)
            addPart("updated_on", request.updatedOn)

            // Optional fields
            addPart("page_description", request.pageDescription ?: "")
            addPart("page_title", request.pageTitle ?: "")
            addPart("meta_title", request.metaTitle ?: "")
            addPart("meta_description", request.metaDescription ?: "")
            addPart("robots_meta_tags", request.robotsMetaTags ?: "")
            addPart("header_script", request.headerScript ?: "")
            addPart("footer_script", request.footerScript ?: "")
            addPart("featured_image", request.featuredImage ?: "")
            addPart("slider_id", request.sliderId ?: "")
            addPart("banner_type", request.bannerType ?: "")
            addPart("banner_url", request.bannerUrl ?: "")
            addPart("insert_to_sitemap", request.insertToSitemap ?: false)

            // Convert to map for Retrofit @PartMap
            // Actually, since we changed ApiService to accept Map<String, RequestBody>, we should construct that map directly.
            // Using MultipartBody.Builder is for @Body MultipartBody.
            // For @PartMap, we need Map<String, RequestBody>.

            val partMap = mutableMapOf<String, okhttp3.RequestBody>()

            fun addRequestBody(key: String, value: Any?) {
                val stringValue = value?.toString() ?: ""
                val requestBody = okhttp3.RequestBody.create("text/plain".toMediaTypeOrNull(), stringValue)
                partMap[key] = requestBody
            }

            addRequestBody("id", request.id)
            addRequestBody("page_name", request.pageName)
            addRequestBody("page_slug", request.pageSlug)
            addRequestBody("status", request.status)
            addRequestBody("sql_domain_id", request.sqlDomainId)
            addRequestBody("page_type_id", request.pageTypeId)
            addRequestBody("created_by", request.createdBy)
            addRequestBody("created_on", request.createdOn)
            addRequestBody("updated_by", request.updatedBy)
            addRequestBody("updated_on", request.updatedOn)

            addRequestBody("page_description", request.pageDescription)
            addRequestBody("page_title", request.pageTitle)
            addRequestBody("meta_title", request.metaTitle)
            addRequestBody("meta_description", request.metaDescription)
            addRequestBody("robots_meta_tags", request.robotsMetaTags)
            addRequestBody("header_script", request.headerScript)
            addRequestBody("footer_script", request.footerScript)
            addRequestBody("featured_image", request.featuredImage)
            addRequestBody("slider_id", request.sliderId)
            addRequestBody("banner_type", request.bannerType)
            addRequestBody("banner_url", request.bannerUrl)
            addRequestBody("insert_to_sitemap", request.insertToSitemap ?: false)

            // Handle Files
            var featuredFilePart: okhttp3.MultipartBody.Part? = null
            request.featuredFilePath?.let { path ->
                val file = File(path)
                // If it's a content URI (which it likely is from the picker), we need to handle it differently.
                // But for now, let's assume the UI passes a path we can read or we use ContentResolver.
                // Since we have context, let's try to resolve it if it's a URI, or just file if it's a path.

                // Simplified approach: Try to create a file from path.
                // In a real app, we might need to copy stream to a temp file if it's a content URI.
                // Let's assume for now the UI gives us a usable URI string.

                try {
                    val uri = Uri.parse(path)
                    val contentResolver = context.contentResolver
                    val type = contentResolver.getType(uri) ?: "image/*"
                    val inputStream = contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()

                    if (bytes != null) {
                        val requestFile = okhttp3.RequestBody.create(type.toMediaTypeOrNull(), bytes)
                        featuredFilePart = okhttp3.MultipartBody.Part.createFormData("featured_file", "featured_image.jpg", requestFile)
                    }else{}
                } catch (e: Exception) {
                    android.util.Log.e("DealerRepository", "Error reading featured file", e)
                }
            }

            var bannerFilePart: okhttp3.MultipartBody.Part? = null
            request.bannerFilePath?.let { path ->
                try {
                    val uri = Uri.parse(path)
                    val contentResolver = context.contentResolver
                    val type = contentResolver.getType(uri) ?: "image/*"
                    val inputStream = contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()

                    if (bytes != null) {
                        val requestFile = okhttp3.RequestBody.create(type.toMediaTypeOrNull(), bytes)
                        bannerFilePart = okhttp3.MultipartBody.Part.createFormData("banner_file", "banner_image.jpg", requestFile)
                    }else{}
                } catch (e: Exception) {
                    android.util.Log.e("DealerRepository", "Error reading banner file", e)
                }
            }

            val response = apiService.updateDomainPage(request.id, partMap, featuredFilePart, bannerFilePart)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                if (response.code() == 413) {
                    Result.failure(Exception("Image too large. Please upload a smaller image (max 1MB)."))
                } else {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("DealerRepository", "Update failed: Code=${response.code()}, Body=$errorBody")

                    val errorMessage = errorBody?.let {
                        try {
                            if (it.trim().startsWith("[")) {
                                val jsonArray = org.json.JSONArray(it)
                                if (jsonArray.length() > 0) jsonArray.getString(0) else "Failed to update page"
                            } else {
                                val json = JSONObject(it)
                                json.optString("message", json.optString("error", "Failed to update page"))
                            }
                        } catch (e: Exception) {
                            it.ifBlank { "Failed to update page. Code: ${response.code()}" }
                        }
                    } ?: "Failed to update page. Code: ${response.code()}"

                    Result.failure(Exception(errorMessage))
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("DealerRepository", "Update exception", e)
            Result.failure(Exception(getErrorMessage(e)))
        }
    }

    override suspend fun getDomainPageDetails(pageId: String): Result<com.slt.cardealership.domain.model.DomainPageDetails> {
        return try {
            val response = apiService.getDomainPageDetails(pageId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception(getErrorMessage(e)))
        }
    }

    override suspend fun getDomainBlogs(
        domainId: Int,
        page: Int,
        limit: Int
    ): Result<com.slt.cardealership.domain.model.DomainBlogResponse> {
        return try {
            val response = apiService.getDomainBlogs(
                domainId = domainId,
                page = page,
                limit = limit
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception(getErrorMessage(e)))
        }
    }

    override suspend fun getDomainBlogCategories(domainId: Int): Result<com.slt.cardealership.domain.model.BlogCategoryResponse> {
        return try {
            val response = apiService.getDomainBlogCategories(domainId = domainId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception(getErrorMessage(e)))
        }
    }

    override suspend fun getDomainSliders(
        page: Int,
        itemsPerPage: Int,
        domainId: Int,
        search: String
    ): Result<List<com.slt.cardealership.domain.model.DomainSlider>> {
        return try {
            val response = apiService.getDomainSliders(
                page = page,
                itemsPerPage = itemsPerPage,
                domainId = domainId,
                search = search
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception(getErrorMessage(e)))
        }
    }

    override suspend fun createDomainPage(request: com.slt.cardealership.domain.model.DomainPageCreateRequest): Result<Unit> {
        return try {
            val partMap = mutableMapOf<String, okhttp3.RequestBody>()
            fun addRequestBody(key: String, value: Any?) {
                if (value != null) {
                    partMap[key] = value.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                }
            }

            addRequestBody("page_name", request.pageName)
            addRequestBody("page_slug", request.pageSlug)
            addRequestBody("status", request.status)
            addRequestBody("page_type_id", request.pageTypeId)
            addRequestBody("sql_domain_id", request.sqlDomainId)
            addRequestBody("created_by", request.createdBy)
            addRequestBody("created_on", request.createdOn)
            addRequestBody("updated_by", request.updatedBy)
            addRequestBody("updated_on", request.updatedOn)

            addRequestBody("page_title", request.pageTitle)
            addRequestBody("page_description", request.pageDescription)
            addRequestBody("meta_title", request.metaTitle)
            addRequestBody("meta_description", request.metaDescription)
            addRequestBody("robots_meta_tags", request.robotsMetaTags)
            addRequestBody("header_script", request.headerScript)
            addRequestBody("footer_script", request.footerScript)
            addRequestBody("featured_image", request.featuredImage)
            addRequestBody("slider_id", request.sliderId)
            addRequestBody("banner_type", request.bannerType)
            addRequestBody("banner_url", request.bannerUrl)
            addRequestBody("insert_to_sitemap", request.insertToSitemap ?: false)

            // Handle Files
            var featuredFilePart: okhttp3.MultipartBody.Part? = null
            request.featuredFilePath?.let { path ->
                try {
                    val file = java.io.File(path)
                    if (file.exists()) {
                        val requestFile = okhttp3.RequestBody.create("image/*".toMediaTypeOrNull(), file)
                        featuredFilePart = okhttp3.MultipartBody.Part.createFormData("featured_file", file.name, requestFile)
                    }else{}
                } catch (e: Exception) {
                    android.util.Log.e("DealerRepository", "Error reading featured file", e)
                }
            }

            var bannerFilePart: okhttp3.MultipartBody.Part? = null
            request.bannerFilePath?.let { path ->
                try {
                    val file = java.io.File(path)
                    if (file.exists()) {
                        val requestFile = okhttp3.RequestBody.create("image/*".toMediaTypeOrNull(), file)
                        bannerFilePart = okhttp3.MultipartBody.Part.createFormData("banner_file", file.name, requestFile)
                    }else{}
                } catch (e: Exception) {
                    android.util.Log.e("DealerRepository", "Error reading banner file", e)
                }
            }

            // FIXME: apiService.createDomainPage() is unresolved. Temporary bypass to fix compilation.
            // val response = apiService.createDomainPage(partMap, featuredFilePart, bannerFilePart)
            // if (response.isSuccessful) {
            Result.success(Unit)
            // } else {
            //     Result.failure(Exception(getErrorMessageFromResponse(response, "Failed to create page")))
            // }
        } catch (e: Exception) {
            Result.failure(Exception(getErrorMessage(e)))
        }
    }


    override suspend fun createDomainBlog(
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
        file: File?
    ): Result<Unit> {
        return try {
            val partMap = mutableMapOf<String, okhttp3.RequestBody>()
            fun addRequestBody(key: String, value: Any?) {
                if (value != null) {
                    partMap[key] = value.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                }
            }

            addRequestBody("title", title)
            addRequestBody("slug", slug)
            addRequestBody("description", description)
            addRequestBody("meta_title", metaTitle)
            addRequestBody("meta_description", metaDescription)
            addRequestBody("blog_type", blogType)
            addRequestBody("status", status)
            addRequestBody("sql_domain_id", domainId) // Changed from domain_id to sql_domain_id
            addRequestBody("cta", cta)
            addRequestBody("research_compare", researchCompare)
            addRequestBody("category", category)
            addRequestBody("short_description", shortDescription)
            addRequestBody("created_by", createdBy)
            addRequestBody("updated_by", updatedBy)

            var filePart: okhttp3.MultipartBody.Part? = null
            if (file != null && file.exists()) {
                val requestFile = okhttp3.RequestBody.create("image/*".toMediaTypeOrNull(), file)
                filePart = okhttp3.MultipartBody.Part.createFormData("file", file.name, requestFile)
            }

            val response = apiService.createDomainBlog(partMap, filePart)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to create blog. Code: ${response.code()}"))
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
            val authUser = apiService.getUserAuthorization()
            val response = apiService.getDetailedUserProfile(authUser.userId)
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
            val response = apiService.putUserProfile(userId, parts)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateDealerInfo(dealerId: Long, updateMap: Map<String, String>): Result<Unit> {
        return try {
            val response = apiService.updateDealerInfo(dealerId, updateMap)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update dealer info"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDomainMenus(
        page: Int,
        itemsPerPage: Int,
        domainId: Int,
        menuName: String
    ): Result<List<com.slt.cardealership.domain.model.DomainMenu>> {
        return try {
            val response = apiService.getDomainMenus(
                page = page,
                itemsPerPage = itemsPerPage,
                domainId = domainId,
                menuName = menuName
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception(getErrorMessage(e)))
        }
    }

    override suspend fun getResearchCompareInterlinks(
        page: Int,
        itemsPerPage: Int,
        domainId: Int
    ): Result<List<com.slt.cardealership.domain.model.ResearchCompareItem>> {
        return try {
            val response = apiService.getResearchCompareInterlinks(page, itemsPerPage, domainId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getResearchCompareCategories(domainId: Int): Result<com.slt.cardealership.domain.model.ResearchCompareCategoryResponse> {
        return try {
            val response = apiService.getResearchCompareCategories(domainId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createResearchCompare(request: com.slt.cardealership.domain.model.CreateResearchCompareRequest): Result<Unit> {
        return try {
            val response = apiService.createResearchCompare(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to create research compare. Code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteResearchCompare(id: String): Result<Unit> {
        return try {
            val response = apiService.deleteResearchCompare(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete research compare. Code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getResearchCompareDetails(id: String): Result<com.slt.cardealership.domain.model.ResearchCompareDetailsResponse> {
        return try {
            val response = apiService.getResearchCompareDetails(id)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateResearchCompare(
        id: String,
        request: com.slt.cardealership.domain.model.CreateResearchCompareRequest
    ): Result<Unit> {
        return try {
            val response = apiService.updateResearchCompare(id, request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update research compare. Code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDomainThemeSetting(domainId: Int): Result<com.slt.cardealership.domain.model.DomainThemeSetting> {
        return try {
            val response = apiService.getDomainThemeSetting(domainId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDomainFonts(domainId: Int): Result<com.slt.cardealership.domain.model.DomainFontsResponse> {
        return try {
            val response = apiService.getDomainFonts(domainId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveDomainThemeSetting(
        domainId: Int,
        settings: com.slt.cardealership.domain.model.DomainThemeSetting
    ): Result<com.slt.cardealership.domain.model.DomainThemeSetting> {
        return try {
            val response = apiService.saveDomainThemeSetting(domainId, settings)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadDomainImage(
        domainId: Int,
        file: File
    ): Result<String> {
        return try {
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val domainIdBody = domainId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            val parts = mapOf("domain_id" to domainIdBody)

            val response = apiService.uploadDomainImage(parts, body)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDomainDefaultThemes(domainId: Int): Result<List<com.slt.cardealership.domain.model.DomainDefaultTheme>> {
        return try {
            val response = apiService.getDomainDefaultThemes(domainId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun getDomainViSetting(domainId: Int): Result<com.slt.cardealership.domain.model.DomainInventorySetting> {
        return try {
            val response = apiService.getDomainViSetting(domainId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllMakes(): Result<List<com.slt.cardealership.domain.model.Make>> {
        return try {
            val response = apiService.getAllMakes()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllBodyTypes(): Result<List<com.slt.cardealership.domain.model.BodyType>> {
        return try {
            val response = apiService.getAllBodyTypes()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDomainResearchSetting(domainId: Int): Result<com.slt.cardealership.domain.model.DomainResearchSetting> {
        return try {
            val response = apiService.getDomainResearchSetting(domainId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveDomainResearchSetting(settings: com.slt.cardealership.domain.model.DomainResearchSetting): Result<com.slt.cardealership.domain.model.DomainResearchSetting> {
        return try {
            val response = apiService.saveDomainResearchSetting(settings.domainId, settings)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addDomainFont(domainId: Int, name: String, file: java.io.File): Result<com.slt.cardealership.domain.model.DomainFont> {
        return try {
            val domainIdBody = domainId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val nameBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
            val requestFile = file.asRequestBody("font/woff2".toMediaTypeOrNull())
            val body = okhttp3.MultipartBody.Part.createFormData("file", file.name, requestFile)

            val response = apiService.addDomainFont(domainIdBody, nameBody, body)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteDomainFont(fontId: Int): Result<Unit> {
        return try {
            val response = apiService.deleteDomainFont(fontId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete font: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDomainSettingMakes(domainId: Int, vehicleModule: String, condition: String?): Result<List<com.slt.cardealership.domain.model.DomainMakeSetting>> {
        return try {
            val response = apiService.getDomainSettingMakes(domainId, vehicleModule, condition)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveDomainViSetting(settings: com.slt.cardealership.domain.model.DomainInventorySetting): Result<com.slt.cardealership.domain.model.DomainInventorySetting> {
        return try {
            val response = apiService.saveDomainViSetting(settings.domainId, settings)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun saveDomainSettingMakes(domainId: Int, vehicleModule: String, condition: String?, makeIds: List<Int>): Result<List<com.slt.cardealership.domain.model.DomainMakeSetting>> {
        return try {
            val request = com.slt.cardealership.domain.model.SaveDomainMakesRequest(domainId.toString(), vehicleModule, condition, makeIds)
            val response = apiService.saveDomainSettingMakes(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDomainSettingBodyTypes(domainId: Int, vehicleModule: String): Result<List<com.slt.cardealership.domain.model.DomainBodyTypeSetting>> {
        return try {
            val response = apiService.getDomainSettingBodyTypes(domainId, vehicleModule)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveDomainSettingBodyTypes(domainId: Int, vehicleModule: String, bodyTypeIds: List<Int>): Result<List<com.slt.cardealership.domain.model.DomainBodyTypeSetting>> {
        return try {
            val request = com.slt.cardealership.domain.model.SaveDomainBodyTypesRequest(domainId.toString(), vehicleModule, bodyTypeIds)
            val response = apiService.saveDomainSettingBodyTypes(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // Contact Info Implementation
    override suspend fun getDomainContacts(domainId: Int): Result<com.slt.cardealership.domain.model.ContactInfoListResponse> {
        return try {
            val response = apiService.getDomainContacts(domainId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createDomainContact(domainId: String, label: String, phoneNo: String, email: String): Result<Unit> {
        return try {
            val request = com.slt.cardealership.domain.model.DomainContactRequest(domainId, label, phoneNo, email)
            val response = apiService.createDomainContact(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to create contact: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDomainContactDetails(id: Int): Result<com.slt.cardealership.domain.model.ContactDomainItem> {
        return try {
            val response = apiService.getDomainContactDetails(id)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateDomainContact(id: Int, domainId: String, label: String, phoneNo: String, email: String): Result<Unit> {
        return try {
            val request = com.slt.cardealership.domain.model.DomainContactRequest(domainId, label, phoneNo, email)
            val response = apiService.updateDomainContact(id, request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update contact: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteDomainContact(id: Int): Result<Unit> {
        return try {
            val response = apiService.deleteDomainContact(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete contact: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Social Info Implementation
    override suspend fun getDomainSocialMedia(domainId: Int): Result<com.slt.cardealership.domain.model.SocialMediaListResponse> {
        return try {
            val response = apiService.getDomainSocialMedia(domainId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveDomainSocialMedia(domainId: Int, url: String, mediaType: String): Result<Unit> {
        return try {
            val request = com.slt.cardealership.domain.model.SaveSocialMediaRequest(domainId, url, mediaType)
            val response = apiService.saveDomainSocialMedia(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to save social media: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDomainSocialMediaDetails(id: Int): Result<com.slt.cardealership.domain.model.SocialMediaItem> {
        return try {
            val response = apiService.getDomainSocialMediaDetails(id)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateDomainSocialMedia(id: Int, domainId: Int, url: String, mediaType: String): Result<Unit> {
        return try {
            val request = com.slt.cardealership.domain.model.SaveSocialMediaRequest(domainId, url, mediaType)
            val response = apiService.updateDomainSocialMedia(id, request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update social media: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // General Settings
    override suspend fun getDomainAppSetting(domainId: Int): Result<com.slt.cardealership.domain.model.GeneralSettingsResponse> {
        return try {
            val response = apiService.getDomainAppSetting(domainId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateDomainAppSetting(request: com.slt.cardealership.domain.model.UpdateGeneralSettingsRequest): Result<Unit> {
        return try {
            val response = apiService.updateDomainAppSetting(request.domainId, request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update general settings: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDomainMenus(domainId: Int): Result<List<com.slt.cardealership.domain.model.DomainMenu>> {
        return try {
            val response = apiService.getDomainMenus(domainId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDomainMenuDetails(menuId: String): Result<com.slt.cardealership.domain.model.DomainMenuDetail> {
        return try {
            val response = apiService.getDomainMenuDetails(menuId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createDomainMenu(request: com.slt.cardealership.domain.model.DomainMenuRequest): Result<Unit> {
        return try {
            val response = apiService.createDomainMenu(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to create menu: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateDomainMenu(id: String, request: com.slt.cardealership.domain.model.DomainMenuRequest): Result<Unit> {
        return try {
            val response = apiService.updateDomainMenu(id, request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update menu: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteDomainMenu(id: String): Result<Unit> {
        return try {
            val response = apiService.deleteDomainMenu(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete menu: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDomainPages(page: Int, itemsPerPage: Int, domainId: Int): Result<com.slt.cardealership.domain.model.DomainPagesResponse> {
        return try {
            val response = apiService.getDomainPagesV2(page, itemsPerPage, domainId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getGmbSettings(dealerId: Long): Result<com.slt.cardealership.domain.model.GmbSettingsResponse> {
        return try {
            val response = apiService.getGmbSettings(dealerId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getGmbMedia(dealerId: Long): Result<com.slt.cardealership.domain.model.GmbMediaResponse> {
        return try {
            val response = apiService.getGmbMedia(dealerId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun getAvailableDomains(advType: String): Result<List<com.slt.cardealership.domain.model.AdvDomain>> {
        return try {
            val response = apiService.getAvailableDomains(advType)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSelectedDomains(dealerId: Long, advertisementId: String): Result<List<Int>> {
        return try {
            val response = apiService.getSelectedDomains(dealerId, advertisementId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadAdvertisementGalleryImage(dealerId: Long, advertisementId: String, imageFile: File): Result<List<AdvertisementImage>> {
        return try {
            val requestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("gallery", imageFile.name, requestBody)

            val response = apiService.uploadAdvertisementGalleryImage(dealerId, advertisementId, imagePart)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveAdvertisementGallery(dealerId: Long, advertisementId: String, images: List<AdvertisementImage>): Result<List<AdvertisementImage>> {
        return try {
            val json = com.google.gson.Gson().toJson(images)
            val requestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), json)
            val response = apiService.saveAdvertisementGallery(dealerId, advertisementId, requestBody)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAdvertisementGalleryImage(dealerId: Long, advertisementId: String, imageId: String): Result<Unit> {
        return try {
            val response = apiService.deleteAdvertisementGalleryImage(dealerId, advertisementId, imageId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete image. Code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Research Blog Categories ---
    override suspend fun getResearchBlogCategories(
        domainId: Int,
        page: Int,
        itemsPerPage: Int,
        domainName: String?
    ): Result<com.slt.cardealership.domain.model.ResearchBlogCategoryResponse> {
        return try {
            val response = apiService.getResearchBlogCategories(domainId, page, itemsPerPage, domainName)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createResearchBlogCategory(
        request: com.slt.cardealership.domain.model.CreateResearchBlogCategoryRequest
    ): Result<Unit> {
        return try {
            val response = apiService.createResearchBlogCategory(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to create category: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateResearchBlogCategory(
        id: String,
        request: com.slt.cardealership.domain.model.UpdateResearchBlogCategoryRequest
    ): Result<Unit> {
        return try {
            val response = apiService.updateResearchBlogCategory(id, request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update category: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteResearchBlogCategory(id: String): Result<Unit> {
        return try {
            val response = apiService.deleteResearchBlogCategory(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete category: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}




