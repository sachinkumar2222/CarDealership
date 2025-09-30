package com.slt.cardealership.data.remote.network

import com.slt.cardealership.domain.model.Article
import com.slt.cardealership.domain.model.ArticleListResponse
import com.slt.cardealership.domain.model.DealerHours
import com.slt.cardealership.domain.model.DealerInfo
import com.slt.cardealership.domain.model.DealerMetasResponse
import com.slt.cardealership.domain.model.UploadImageResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("dealer-api/Dealers/{dealerId}")
    suspend fun getDealerInfo(
        @Path("dealerId") dealerId: Long // Changed to Long for numeric ID
    ): DealerInfo

    @GET("dealer-api/dealer-metas/{dealerId}")
    suspend fun getDealerMetas(
        @Path("dealerId") dealerId: Long
    ): DealerMetasResponse

    @GET("dealer-api/Dealers/{dealerId}/Hours")
    suspend fun getDealerHours(@Path("dealerId") dealerId: Long): List<DealerHours>
}
