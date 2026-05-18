package com.axoloth.captaggenerator.logic.fragment

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Percent
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.vector.ImageVector

data class HistoryItem(
    val id: Int,
    val title: String,
    val time: String,
    val icon: ImageVector
)

object HistoryManager {
    private val _historyItems = mutableStateListOf(
        HistoryItem(1, "Caption Keripik Pedas #Gacor", "7 minutes ago", Icons.Default.Image),
        HistoryItem(2, "Promo Sambal #Viral", "3 minutes ago", Icons.Default.Percent),
        HistoryItem(3, "Ide Konten Sepatu", "1 hour ago", Icons.Default.Lightbulb),
        HistoryItem(4, "Promo Sambal #Viral", "3 minutes ago", Icons.Default.Percent),
        HistoryItem(5, "Ide Konten Sen...", "1 hour ago", Icons.Default.Lightbulb),
        HistoryItem(6, "Promo Sambal #Viral", "1 min ago", Icons.Default.Lightbulb)
    )
    val historyItems: List<HistoryItem> get() = _historyItems

    fun clearAll() {
        _historyItems.clear()
    }
    
    // Nanti bisa ditambahkan fungsi addItem saat Room diimplementasikan
}
