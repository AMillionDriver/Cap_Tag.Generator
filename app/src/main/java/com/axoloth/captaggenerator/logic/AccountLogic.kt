package com.axoloth.captaggenerator.logic

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.compose.ui.graphics.Color

data class AccountHistoryItem(
    val title: String,
    val date: String,
    val icon: ImageVector,
    val iconBackgroundColor: Color
)

class AccountViewModel : ViewModel() {
    var userName by mutableStateOf("Rizki Pratama")
        private set
    
    var profileImageUri by mutableStateOf<Uri?>(null)
        private set
        
    val businessName = "Toko Sepatu Berkah"
    val category = "Perdagangan & Eceran"

    val accountHistory = listOf(
        AccountHistoryItem(
            "Pendaftaran Akun", 
            "(12 Mei 2024)", 
            Icons.Default.AccessTime,
            Color(0xFF3430DC) // Blue
        ),
        AccountHistoryItem(
            "Aktivasi Profil Usaha", 
            "(15 Mei 2024)", 
            Icons.Default.Store,
            Color(0xFF161B22) // Dark
        ),
        AccountHistoryItem(
            "Transaksi Pertama Diproses", 
            "(18 Mei 2024)", 
            Icons.Default.Description,
            Color(0xFF1E293B) // Greyish
        ),
        AccountHistoryItem(
            "Update Laporan Bulanan", 
            "(30 Juni 2024)", 
            Icons.Default.BarChart,
            Color(0xFF8800FF) // Purple
        )
    )

    fun updateUserName(newName: String) {
        if (newName.isNotBlank()) {
            userName = newName
        }
    }

    fun updateProfileImage(uri: Uri?) {
        profileImageUri = uri
    }
}
