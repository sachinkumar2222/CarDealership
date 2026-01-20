package com.slt.cardealership.domain.model

import com.google.gson.annotations.SerializedName

data class PostCountItem(
    @SerializedName("domain_name") val domainName: String?,
    @SerializedName("count") val count: Int
)
