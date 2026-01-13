package com.slt.cardealership.domain.model

import com.google.gson.annotations.SerializedName

data class DomainMenu(
    @SerializedName("id")
    val id: String,
    @SerializedName("menu_name")
    val menuName: String,
    @SerializedName("is_top_primary_menu")
    val isTopPrimaryMenu: Boolean,
    @SerializedName("is_footer_menu")
    val isFooterMenu: Boolean,
    @SerializedName("is_footer_bottom_menu")
    val isFooterBottomMenu: Boolean,
    @SerializedName("sql_domain_id")
    val sqlDomainId: Int,
    @SerializedName("menu_items")
    val menuItems: List<DomainMenuItem> = emptyList(),
    @SerializedName("target")
    val target: String?
)
