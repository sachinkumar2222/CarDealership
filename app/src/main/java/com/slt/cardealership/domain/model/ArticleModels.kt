package com.slt.cardealership.domain.model

import com.google.gson.annotations.SerializedName

data class ArticleLink(
    @SerializedName("link") val link: String,
    @SerializedName("type_name") val typeName: String,
    @SerializedName("type_slug") val typeSlug: String
)
