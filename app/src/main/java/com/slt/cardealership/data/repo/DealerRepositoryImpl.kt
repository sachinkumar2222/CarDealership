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

    override suspend fun getBanners(dealerId: Long): Result<List<Banner>> {
        return try {
            Result.success(apiService.getBanners(dealerId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getGalleryImages(dealerId: Long): Result<List<GalleryImage>> {
        return try {
            Result.success(apiService.getGalleryImages(dealerId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
