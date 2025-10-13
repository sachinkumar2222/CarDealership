package com.slt.cardealership.presentation.navigation

import android.annotation.SuppressLint
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.slt.cardealership.presentation.home.HomeScreen
import com.slt.cardealership.presentation.auth.AuthEvent
import com.slt.cardealership.presentation.auth.AuthViewModel
import com.slt.cardealership.presentation.auth.LoginScreen
import com.slt.cardealership.utils.SplashScreen

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("ContextCastToActivity")
@Composable
fun App() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()


    LaunchedEffect(key1 = Unit) {
        authViewModel.events.collect { event ->
            when (event) {
                is AuthEvent.NavigateToHome -> {
                    navController.navigate(Routes.HomeScreen) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                    }
                }
                is AuthEvent.NavigateToLogin -> {
                    navController.navigate(Routes.LoginScreen) {
                        // Also clear history here
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                    }
                }
                is AuthEvent.ShowSnackbar -> {
                    // Handle snackbar display here if you've added it
                }
            }
        }
    }


        Box(modifier = Modifier.systemBarsPadding()) {
            NavHost(
                navController = navController,
                startDestination = Routes.SplashScreen
            ) {
                composable<Routes.LoginScreen> {
                    LoginScreen(
                            viewModel = authViewModel)
                }
                composable<Routes.HomeScreen>{
                    HomeScreen(mainNavController = navController)
                }
                composable<Routes.SplashScreen> {
                    SplashScreen()
                }
            }
        }
}