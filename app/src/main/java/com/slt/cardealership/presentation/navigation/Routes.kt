package com.slt.cardealership.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Routes {

    @Serializable
    object LoginScreen : Routes()

    @Serializable
    object HomeScreen : Routes()

    @Serializable
    object SplashScreen : Routes()

}