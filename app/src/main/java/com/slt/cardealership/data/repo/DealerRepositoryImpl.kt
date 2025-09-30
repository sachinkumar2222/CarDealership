package com.slt.cardealership.data.repo

import android.util.Log
import com.slt.cardealership.data.remote.network.ApiService
import com.slt.cardealership.domain.model.DealerInfo
import com.slt.cardealership.domain.model.UploadImageResponse
import com.slt.cardealership.domain.repo.DealerRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import javax.inject.Inject

class DealerRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
) : DealerRepository {

    override suspend fun getDealerInfo(dealerId: Long): DealerInfo {
        Log.d("DealerRepo", "Attempting to fetch info for dealerId: '$dealerId'")
        return apiService.getDealerInfo(dealerId)
    }

//    override suspend fun uploadDealerImage(imageFile: File): Result<UploadImageResponse> {
//        return try {
//            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
//            // The name MUST be "file" to match the successful request
//            val imagePart = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)
//
//            // Call the function with only the image part
//            val response = apiService.uploadDealerImage(imagePart)
//            Result.success(response)
//        } catch (e: Exception) {
//            Log.e("DealerRepo", "uploadDealerImage FAILED", e)
//            Result.failure(e)
//        }
//    }
}
