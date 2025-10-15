package com.slt.cardealership.domain.repo

import com.slt.cardealership.domain.model.Banner
import com.slt.cardealership.domain.model.DealerInfo
import com.slt.cardealership.domain.model.DealerMetasResponse
import com.slt.cardealership.domain.model.GalleryImage
import com.slt.cardealership.domain.model.Post
import com.slt.cardealership.domain.model.Vehicle
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
    suspend fun addGalleryImage(dealerId: Long, imageFile: File, existingUrls: List<String>): Result<Unit>
    suspend fun deleteGalleryImage(dealerId: Long, imageId: String): Result<Unit> // <-- ADD THIS LINE

    suspend fun getVehicles(dealerId: Long): Result<List<Vehicle>>
    suspend fun getVehicleDetails(dealerId: Long, vehicleId: String): Result<Vehicle>
    suspend fun addVehicle(dealerId: Long, vehicle: Vehicle): Result<Vehicle>
    suspend fun updateVehicle(dealerId: Long, vehicleId: String, vehicle: Vehicle): Result<Vehicle>
    suspend fun deleteVehicle(dealerId: Long, vehicleId: String): Result<Unit>
    suspend fun decodeVin(vin: String): Result<Vehicle>
}