package com.axoloth.captaggenerator.screen.fragment

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.VectorConverter
import com.axoloth.captaggenerator.service.ai.onquesystem.GenerationStep

@Composable
fun GenerateSplash(currentStep: GenerationStep) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // Animasi Loader
            val infiniteTransition = rememberInfiniteTransition()
            val size by infiniteTransition.animateValue(
                initialValue = 100.dp,
                targetValue = 120.dp,
                typeConverter = Dp.VectorConverter,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )

            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(Color(0xFF8A2BE2).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    color = Color(0xFF8A2BE2),
                    strokeWidth = 4.dp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = when (currentStep) {
                    is GenerationStep.Copywriting -> "Menganalisis Detail Produk..."
                    is GenerationStep.Caption -> "Menyusun Caption Menarik (Estimasi 1 Menit)..."
                    is GenerationStep.Tags -> "Mengoptimasi Tag & Hashtag..."
                    else -> "Menyiapkan Hasil..."
                },
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Kami sedang meracik konten terbaik untuk Anda. Sistem antrean aktif untuk memastikan hasil maksimal.",
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}
