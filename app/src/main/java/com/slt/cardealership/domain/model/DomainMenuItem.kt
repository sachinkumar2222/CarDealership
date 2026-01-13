package com.slt.cardealership.domain.model

import com.google.gson.annotations.SerializedName

data class DomainMenuItem(
    @SerializedName("menu_label")
    val menuLabel: String,
    @SerializedName("custom_url")
    val customUrl: String?,
    @SerializedName("page_slug")
    val pageSlug: String?,
    @SerializedName("target")
    val target: String?,
    @SerializedName("prms")
    val prms: String?,
    @SerializedName("child_items")
    val childItems: List<DomainMenuItem> = emptyList()
)
