package com.slt.cardealership.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PageType(
    val id: String,
    @SerialName("page_type_name") val pageTypeName: String,
    @SerialName("page_type_slug") val pageTypeSlug: String
)
