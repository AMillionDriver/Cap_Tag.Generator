package com.axoloth.captaggenerator.screen.fragment

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

enum class BiometricDialogStatus { IDLE, SUCCESS, ERROR }

@Composable
fun BiometricDialog(
    onDismiss: () -> Unit,
    onCancel: () -> Unit,
    status: BiometricDialogStatus = BiometricDialogStatus.IDLE,
    message: String? = null
) {
    val accentColor = when (status) {
        BiometricDialogStatus.IDLE -> Color(0xFF8A2BE2) // Purple
        BiometricDialogStatus.SUCCESS -> Color.Green
        BiometricDialogStatus.ERROR -> Color.Red
    }

    val instructionText = when (status) {
        BiometricDialogStatus.IDLE -> "Sentuh sensor sidik jari untuk mengonfirmasi aktivasi"
        BiometricDialogStatus.SUCCESS -> "Berhasil diverifikasi!"
        BiometricDialogStatus.ERROR -> message ?: "Gagal memverifikasi sidik jari"
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF1A1A1A).copy(alpha = 0.95f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Verifikasi Sidik Jari",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Fingerprint Icon with Glowing Ring effect
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(120.dp)
                ) {
                    // Outer ring that changes color based on status
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(
                                width = 2.dp,
                                brush = Brush.sweepGradient(
                                    colors = listOf(Color.Transparent, accentColor, Color.Transparent)
                                ),
                                shape = CircleShape
                            )
                    )
                    
                    // The main icon changes color based on status
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Fingerprint",
                        tint = accentColor,
                        modifier = Modifier.size(80.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = if (status == BiometricDialogStatus.SUCCESS) "Verified" else "Pindai Sidik Jari Anda",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = instructionText,
                    color = if (status == BiometricDialogStatus.ERROR) Color.Red else Color.Gray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    TextButton(onClick = onCancel) {
                        Text(
                            text = "Batal",
                            color = Color(0xFF8A2BE2),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun BiometricDialogPreview() {
    BiometricDialog(onDismiss = {}, onCancel = {})
}

@Preview
@Composable
fun BiometricDialogSuccessPreview() {
    BiometricDialog(onDismiss = {}, onCancel = {}, status = BiometricDialogStatus.SUCCESS)
}

@Preview
@Composable
fun BiometricDialogErrorPreview() {
    BiometricDialog(onDismiss = {}, onCancel = {}, status = BiometricDialogStatus.ERROR, message = "Sidik jari tidak dikenali")
}
