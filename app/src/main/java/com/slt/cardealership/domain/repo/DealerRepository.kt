package com.slt.cardealership.domain.repo

import com.slt.cardealership.domain.model.Banner
import com.slt.cardealership.domain.model.DealerInfo
import com.slt.cardealership.domain.model.DealerMetasResponse
import com.slt.cardealership.domain.model.GalleryImage
import com.slt.cardealership.domain.model.Post
import java.io.File

interface DealerRepository {
    suspend fun getCombinedDealerInfo(dealerId: Long): Result<DealerInfo>
    suspend fun getPosts(dealerId: Long): Result<List<Post>>
    suspend fun getDealerMetas(dealerId: Long): DealerMetasResponse
    suspend fun getBanners(dealerId: Long): Result<List<Banner>>
    suspend fun getGalleryImages(dealerId: Long): Result<List<GalleryImage>>
    suspend fun addBanner(
        dealerId: Long,
        title: String,
        url: String,
        startDate: String,
        imageFile: File
    ): Result<Unit>
    suspend fun getBannerDetails(dealerId: Long, bannerId: String): Result<Banner>
    suspend fun updateBanner(
        dealerId: Long,
        bannerId: String,
        title: String,
        url: String,
        startDate: String,
        imageFile: File? // Image file is optional
    ): Result<Unit>

    suspend fun addGalleryImage(dealerId: Long, imageFile: File): Result<Unit>
}
