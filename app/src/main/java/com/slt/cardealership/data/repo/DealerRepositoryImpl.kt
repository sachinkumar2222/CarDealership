package com.slt.cardealership.data.repo

import android.util.Log
import com.slt.cardealership.data.remote.network.ApiService
import com.slt.cardealership.domain.model.Amenities
import com.slt.cardealership.domain.model.Banner
import com.slt.cardealership.domain.model.DealerCategory
import com.slt.cardealership.domain.model.DealerInfo
import com.slt.cardealership.domain.model.DealerMetasResponse
import com.slt.cardealership.domain.model.GalleryImage
import com.slt.cardealership.domain.model.HomeDelivery
import com.slt.cardealership.domain.model.HomeTestDrive
import com.slt.cardealership.domain.repo.DealerRepository

import com.slt.cardealership.domain.model.Post
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
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

    override suspend fun addGalleryImage(dealerId: Long, imageFile: File): Result<Unit> {
        return try {
            val requestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("file", imageFile.name, requestBody)
            apiService.addGalleryImage(dealerId, imagePart)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
