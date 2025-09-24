package com.slt.cardealership.data.repo

import android.util.Log
import com.slt.cardealership.data.remote.network.ApiService
import com.slt.cardealership.domain.model.DealerInfo
import com.slt.cardealership.domain.repo.DealerRepository
import javax.inject.Inject

class DealerRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : DealerRepository {

    override suspend fun getDealerInfo(dealerId: String): DealerInfo {
        Log.d("DealerRepo", "Attempting to fetch info for dealerId: '$dealerId'")
        return apiService.getDealerInfo(dealerId)
    }
}
