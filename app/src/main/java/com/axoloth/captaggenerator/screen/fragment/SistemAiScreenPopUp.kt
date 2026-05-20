package com.axoloth.captaggenerator.screen.fragment

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.axoloth.captaggenerator.logic.SettingScreenViewModel
import com.axoloth.captaggenerator.logic.ToneOption

private val PopupBg = Color(0xFF1C1C1E)
private val AccentPurple = Color(0xFF8400FF)
private val SecondaryText = Color(0xFF8E8E93)
private val ItemBg = Color(0xFF2A2929)
private val BorderBlue = Color(0xFF3430DC).copy(alpha = 0.5f)

@Composable
fun ToneOfVoicePopup(
    viewModel: SettingScreenViewModel,
    onDismiss: () -> Unit
) {
    var tempSelectedTone by remember { mutableStateOf(viewModel.selectedTone) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = PopupBg,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Tone of Voice Configuration",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = SecondaryText)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(viewModel.toneOptions) { option ->
                        val isSelected = tempSelectedTone == option.title
                        ToneOptionItem(
                            option = option,
                            isSelected = isSelected,
                            onClick = { tempSelectedTone = option.title }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        viewModel.updateTone(tempSelectedTone)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BorderBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Confirm", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ToneOptionItem(
    option: ToneOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ItemBg, RoundedCornerShape(16.dp))
            .then(
                if (isSelected) Modifier.border(1.5.dp, BorderBlue, RoundedCornerShape(16.dp))
                else Modifier
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(option.emoji, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(option.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(option.description, color = SecondaryText, fontSize = 12.sp)
        }
        if (isSelected) {
            Icon(Icons.Default.Check, null, tint = BorderBlue)
        } else {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = SecondaryText)
        }
    }
}

@Composable
fun DatabaseTrendLogPopup(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = PopupBg,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(ItemBg, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Storage, null, tint = AccentPurple)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Log Perubahan Database Tren", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Database Versi: v2.1.0", color = AccentPurple, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    val logs = listOf(
                        TrendLog("14 Mei 2026, 10:30", "UPDATE", "Penyempurnaan Algoritma Deteksi Tren Core."),
                        TrendLog("12 Mei 2026, 16:15", "TAMBAH", "Sumber Data Baru API Google Trends Regional (South East Asia)."),
                        TrendLog("10 Mei 2026, 11:00", "UPDATE", "Pembaruan Skema Database untuk Metrik Tren Baru."),
                        TrendLog("05 Mei 2026, 08:45", "HAPUS", "Metrik Tren Kedaluwarsa."),
                        TrendLog("01 Mei 2026, 14:00", "TAMBAH", "Modul Visualisasi Tren v1.5.")
                    )

                    items(logs) { log ->
                        TrendLogItem(log)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Tutup", color = SecondaryText)
                }
            }
        }
    }
}

data class TrendLog(val date: String, val tag: String, val message: String)

@Composable
fun TrendLogItem(log: TrendLog) {
    val tagColor = when(log.tag) {
        "UPDATE" -> AccentPurple
        "TAMBAH" -> BorderBlue
        "HAPUS" -> Color(0xFFFF453A)
        else -> SecondaryText
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(log.date, color = SecondaryText, fontSize = 11.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = tagColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, tagColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        "[${log.tag}]",
                        color = tagColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(log.message, color = Color.White, fontSize = 13.sp)
        }
    }
}
