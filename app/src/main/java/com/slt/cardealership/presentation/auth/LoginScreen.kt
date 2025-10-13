@file:JvmName("LoginScreenKt")

package com.slt.cardealership.presentation.auth

import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.slt.cardealership.R
import com.slt.cardealership.common.ResultState
import com.slt.cardealership.presentation.navigation.Routes

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel()) {
        val state by viewModel.authState.collectAsState()
        val activity = LocalActivity.current
    val blueGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF2196F3), // Light Blue
            Color(0xFF1565C0)  // Dark Blue
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color=Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top 60%: Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f), // 60% of screen height
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.bac),
                contentDescription = "Car Dealership background",
                modifier = Modifier.fillMaxSize(0.9f),
                contentScale = ContentScale.Fit
            )

            // Login image slightly bigger
            Image(
                painter = painterResource(id = R.drawable.login),
                contentDescription = "Car Dealership Logo",
                modifier = Modifier
                    .fillMaxWidth(0.9f) // slightly bigger than 0.8f
                    .aspectRatio(1f),   // maintain square ratio
                contentScale = ContentScale.Fit
            )
        }

        // Bottom 40%: Texts + Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.4f), // 40% of screen height
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Access Your Dealer Portal",
                    lineHeight = 44.sp,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Manage your inventory, leads, and sales pipeline.",
                    fontSize = 18.sp,
                    lineHeight = 24.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                Button(
                    onClick = { viewModel.signIn(activity) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(blueGradient, shape = RoundedCornerShape(12.dp))
                ) {
                    Text(
                        text = "SECURE SIGN-IN",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
