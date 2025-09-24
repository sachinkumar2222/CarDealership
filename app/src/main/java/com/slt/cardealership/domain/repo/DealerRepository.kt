package com.slt.cardealership.domain.repo

import com.slt.cardealership.domain.model.DealerInfo


interface DealerRepository {
    suspend fun getDealerInfo(dealerId: String): DealerInfo
}
