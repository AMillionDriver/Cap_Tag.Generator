package com.axoloth.captaggenerator.screen.fragment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
    onClose: () -> Unit,
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

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = when (state) {
                    RecordingState.RECORDING -> if (viewModel.isListening) {
                        "Mendengarkan suara..."
                    } else {
                        "Rekaman siap dikirim"
                    }
                    RecordingState.LOCKED -> "Recording Locked"
                    RecordingState.PROCESSING -> "Memproses transkrip..."
                    RecordingState.CANCELLED -> "Rekaman dibatalkan"
                    RecordingState.SENT -> "Mengirim transkrip..."
                    RecordingState.IDLE -> ""
                },
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (viewModel.transcribedText.isNotEmpty()) {
                Text(
                    text = viewModel.transcribedText,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(bottom = 16.dp)
                )
            }

            viewModel.speechErrorMessage?.let { message ->
                Text(
                    text = message,
                    color = Color(0xFFFF6B6B),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(bottom = 12.dp)
                )
            }

            if (state == RecordingState.RECORDING || state == RecordingState.LOCKED) {
                RecordingControls(viewModel = viewModel, onClose = onClose)
            }

            if (state == RecordingState.PROCESSING) {
                ProcessingResult(viewModel = viewModel, onClose = onClose)
            }
        }
    }
}

@Composable
private fun RecordingControls(
    viewModel: MicViewModel,
    onClose: () -> Unit
) {
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
        Box(
            modifier = Modifier
                .width(150.dp)
                .height(40.dp)
                .background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(if (viewModel.micLevel > 0.08f) viewModel.micLevel else 0.08f)
                    .background(Color(0xFF8A2BE2), RoundedCornerShape(20.dp))
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Column(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MicOptionRow(
            icon = Icons.Default.AutoAwesome,
            label = "Perbaiki Tata Bahasa",
            checked = viewModel.autoCorrectGrammar,
            onCheckedChange = { viewModel.autoCorrectGrammar = it }
        )
        MicOptionRow(
            icon = Icons.Default.Edit,
            label = "Tulis Ulang (${viewModel.rewriteStyle})",
            checked = viewModel.autoRewrite,
            onCheckedChange = { viewModel.autoRewrite = it }
        )
        MicOptionRow(
            icon = Icons.Default.Translate,
            label = "Terjemahkan ke ${viewModel.targetLanguage.uppercase()}",
            checked = viewModel.autoTranslate,
            onCheckedChange = { viewModel.autoTranslate = it }
        )

        Text(
            text = "Bahasa terdeteksi: ${viewModel.detectedLanguage.uppercase()}",
            color = Color.Gray,
            fontSize = 10.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButton(
            onClick = { viewModel.cancelRecording() },
            modifier = Modifier
                .size(56.dp)
                .background(Color.Red, CircleShape)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.White)
        }

        IconButton(
            onClick = {
                viewModel.stopRecording(true)
                onClose()
            },
            enabled = viewModel.transcribedText.isNotBlank(),
            modifier = Modifier
                .size(56.dp)
                .background(
                    if (viewModel.transcribedText.isNotBlank()) Color(0xFF8A2BE2) else Color.DarkGray,
                    CircleShape
                )
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
        }
    }
}

@Composable
private fun MicOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (checked) Color(0xFF8A2BE2) else Color.Gray,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF8A2BE2)),
            modifier = Modifier.scale(0.6f)
        )
    }
}

@Composable
private fun ProcessingResult(
    viewModel: MicViewModel,
    onClose: () -> Unit
) {
    if (viewModel.finalResultText.isEmpty()) {
        CircularProgressIndicator(
            color = Color(0xFF8A2BE2),
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("AI sedang merapikan teks...", color = Color.White, fontSize = 12.sp)
        return
    }

    Card(
        modifier = Modifier.fillMaxWidth(0.9f),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C26)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Masukkan rekaman ke mana?", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.selectTranscriptionDestination("model") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8A2BE2))
            ) {
                Text("Deskripsi Produk")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.selectTranscriptionDestination("purpose") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8A2BE2))
            ) {
                Text("Deskripsi Tujuan")
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { viewModel.reset(); onClose() }) {
                Text("Batal", color = Color.Gray)
            }
        }
    }
}

private fun formatDuration(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
