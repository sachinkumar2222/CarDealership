package com.slt.cardealership.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slt.cardealership.R
import com.slt.cardealership.presentation.info.LoadingAnimation
import com.slt.cardealership.ui.theme.CarDealershipTheme

@Composable
fun SplashScreen(modifier: Modifier = Modifier) {

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color=Color(0xFFF0F8FF))
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.login),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(320.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LoadingAnimation()
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Signing in...",
                color = Color.DarkGray,
                fontSize = 18.sp,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Image(
                painter = painterResource(R.drawable.backcar),
                contentDescription = "Car Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp) // Adjust height as you like
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}