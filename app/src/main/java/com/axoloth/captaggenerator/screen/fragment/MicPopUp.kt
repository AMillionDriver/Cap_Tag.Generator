package com.axoloth.captaggenerator.screen.fragment

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axoloth.captaggenerator.logic.fragment.MicViewModel
import com.axoloth.captaggenerator.logic.fragment.RecordingState
import java.util.Locale

@Composable
fun MicPopUp(
    viewModel: MicViewModel,
    onClose: () -> Unit
) {
    val state = viewModel.recordingState
    if (state == RecordingState.IDLE) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Mic Icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF8A2BE2)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Timer and Waveform placeholder
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = formatDuration(viewModel.recordingDuration),
                    color = Color.Red,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(16.dp))
                // Placeholder for Waveform
                Box(
                    modifier = Modifier
                        .width(200.dp)
                        .height(40.dp)
                        .background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (state == RecordingState.LOCKED) "Recording Locked" else "slide to cancel",
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Real-time transcribed text
            if (viewModel.transcribedText.isNotEmpty()) {
                Text(
                    text = viewModel.transcribedText,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(bottom = 16.dp)
                )
            }

            // Smart Workflow Options (Hybrid)
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Grammar Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (viewModel.autoCorrectGrammar) Color(0xFF8A2BE2) else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Perbaiki Tata Bahasa", color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Switch(
                        checked = viewModel.autoCorrectGrammar,
                        onCheckedChange = { viewModel.autoCorrectGrammar = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF8A2BE2)),
                        modifier = Modifier.scale(0.6f)
                    )
                }

                // Rewrite Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = if (viewModel.autoRewrite) Color(0xFF8A2BE2) else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tulis Ulang (${viewModel.rewriteStyle})", color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Switch(
                        checked = viewModel.autoRewrite,
                        onCheckedChange = { viewModel.autoRewrite = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF8A2BE2)),
                        modifier = Modifier.scale(0.6f)
                    )
                }

                // Translate Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = null,
                        tint = if (viewModel.autoTranslate) Color(0xFF8A2BE2) else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Terjemahkan ke ${viewModel.targetLanguage.uppercase()}", color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Switch(
                        checked = viewModel.autoTranslate,
                        onCheckedChange = { viewModel.autoTranslate = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF8A2BE2)),
                        modifier = Modifier.scale(0.6f)
                    )
                }
                
                Text(
                    text = "Bahasa terdeteksi: ${viewModel.detectedLanguage.uppercase()}",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state == RecordingState.PROCESSING) {
                CircularProgressIndicator(
                    color = Color(0xFF8A2BE2),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("AI sedang merapikan teks...", color = Color.White, fontSize = 12.sp)
            }

            if (state == RecordingState.LOCKED) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Cancel Button
                    IconButton(
                        onClick = { viewModel.cancelRecording() },
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color.Red, CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.White)
                    }

                    // Send Button
                    IconButton(
                        onClick = { viewModel.stopRecording(true); onClose() },
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0xFF8A2BE2), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        }
    }
}

private fun formatDuration(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
