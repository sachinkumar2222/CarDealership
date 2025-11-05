package com.slt.cardealership.domain.repo

import com.slt.cardealership.domain.model.Advertisement
import com.slt.cardealership.domain.model.AdvertisementDomain
import com.slt.cardealership.domain.model.AdvertisementGoal
import com.slt.cardealership.domain.model.AdvertisementGoalType
import com.slt.cardealership.domain.model.AdvertisementImage
import com.slt.cardealership.domain.model.Banner
import com.slt.cardealership.domain.model.DealerInfo
import com.slt.cardealership.domain.model.DealerMetasResponse
import com.slt.cardealership.domain.model.EvoxImageResponse
import com.slt.cardealership.domain.model.FaqDetails
import com.slt.cardealership.domain.model.FaqItem
import com.slt.cardealership.domain.model.FaqRequest
import com.slt.cardealership.domain.model.GalleryImage
import com.slt.cardealership.domain.model.GalleryImageUploadResponse
import com.slt.cardealership.domain.model.ModifyDealerRequest
import com.slt.cardealership.domain.model.Post
import com.slt.cardealership.domain.model.SeoTag
import com.slt.cardealership.domain.model.TrimListResponse
import com.slt.cardealership.domain.model.UpdateHoursRequest
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

    suspend fun uploadVehicleGalleryImages(vehicleId: String, imageFiles: List<File>): Result<Unit>

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
}