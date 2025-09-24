package com.slt.cardealership.domain.model


data class User(
    val id: String,
    val name: String,
    val email: String,
    val accessToken: String
)