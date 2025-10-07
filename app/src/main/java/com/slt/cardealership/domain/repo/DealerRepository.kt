package com.slt.cardealership.domain.repo

import com.slt.cardealership.domain.model.Banner
import com.slt.cardealership.domain.model.DealerInfo
import com.slt.cardealership.domain.model.DealerMetasResponse
import com.slt.cardealership.domain.model.GalleryImage
import com.slt.cardealership.domain.model.Post

interface DealerRepository {
    suspend fun getCombinedDealerInfo(dealerId: Long): Result<DealerInfo>
    suspend fun getPosts(dealerId: Long): Result<List<Post>>
    suspend fun getDealerMetas(dealerId: Long): DealerMetasResponse
    suspend fun getBanners(dealerId: Long): Result<List<Banner>>
    suspend fun getGalleryImages(dealerId: Long): Result<List<GalleryImage>>
}
