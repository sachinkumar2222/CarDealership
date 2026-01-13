package com.slt.cardealership.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.slt.cardealership.R
import com.slt.cardealership.ui.theme.BrandBlue

@Composable
fun SplashScreen(modifier: Modifier = Modifier) {

    // Load Lottie Composition
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("lott.json"))

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Top Section: Logo
        Spacer(modifier = Modifier.height(48.dp))
        Image(
            painter = painterResource(R.drawable.login),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(400.dp) // Large logo as requested
                .shadow(elevation = 0.dp, shape = RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
        )

        // Spacer to push bottom content to the very bottom
        Spacer(modifier = Modifier.weight(1f))

        // Bottom Section: Loading Indicator & Text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = "Loading Resources...",
                color = Color.Gray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                modifier = Modifier
                    .width(150.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = BrandBlue,
                trackColor = BrandBlue.copy(alpha = 0.2f),
                strokeCap = StrokeCap.Round
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Preview
@Composable
fun SplashScreenPreview() {
    SplashScreen()
}