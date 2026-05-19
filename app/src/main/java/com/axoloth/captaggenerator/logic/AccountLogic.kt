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
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class AccountHistoryItem(
    val title: String,
    val date: String,
    val icon: ImageVector,
    val iconBackgroundColor: Color
)

class AccountViewModel(private val repository: UserRepository) : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    var userName by mutableStateOf("Rizki Pratama")
        private set
    
    var profileImageUri by mutableStateOf<Uri?>(null)
        private set
        
    val businessName = "Toko Sepatu Berkah"
    val category = "Perdagangan & Eceran"

    init {
        viewModelScope.launch {
            repository.getUser().collectLatest { userData ->
                _user.value = userData
                userData?.let {
                    userName = it.userName
                    profileImageUri = it.profileImageUri?.let { uriString -> Uri.parse(uriString) }
                }
            }
        }
    }

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
            val currentUser = _user.value ?: User(newName, businessName, category, profileImageUri?.toString())
            viewModelScope.launch {
                repository.saveUser(currentUser.copy(userName = newName))
            }
        }
    }

    fun updateProfileImage(uri: Uri?) {
        val currentUser = _user.value ?: User(userName, businessName, category, uri?.toString())
        viewModelScope.launch {
            repository.saveUser(currentUser.copy(profileImageUri = uri?.toString()))
        }
    }
}

