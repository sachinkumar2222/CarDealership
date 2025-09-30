package com.slt.cardealership.domain.repo

import com.slt.cardealership.domain.model.DealerInfo
import com.slt.cardealership.domain.model.UploadImageResponse
import java.io.File


interface DealerRepository {
    suspend fun getDealerInfo(dealerId: Long): DealerInfo
    //suspend fun uploadDealerImage(imageFile: File): Result<UploadImageResponse>
}
