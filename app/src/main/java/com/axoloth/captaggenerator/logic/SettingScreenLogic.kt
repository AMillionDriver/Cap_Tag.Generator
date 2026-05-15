package com.axoloth.captaggenerator.logic

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class SettingScreenViewModel(context: Context) : ViewModel() {
    private val sharedPrefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    var isTwoFactorEnabled by mutableStateOf(sharedPrefs.getBoolean("2fa_enabled", false))
        private set

    var isBiometricEnabled by mutableStateOf(sharedPrefs.getBoolean("biometric_enabled", false))
        private set

    // Business Profile State
    var businessName by mutableStateOf(sharedPrefs.getString("business_name", "Warung Kopi Jaya") ?: "Warung Kopi Jaya")
        private set
    
    var isEditingBusinessName by mutableStateOf(false)

    var umkmCategory by mutableStateOf(sharedPrefs.getString("umkm_category", "Kuliner") ?: "Kuliner")
        private set

    fun onTwoFactorToggle(enabled: Boolean, onNavigateToSetup: () -> Unit) {
        if (enabled) {
            onNavigateToSetup()
        } else {
            // Full Reset: Bersihkan semua konfigurasi saat dimatikan
            clearTwoFactorConfig()
        }
    }

    fun updateTwoFactorStatus(enabled: Boolean) {
        isTwoFactorEnabled = enabled
        sharedPrefs.edit().putBoolean("2fa_enabled", enabled).apply()
    }

    fun updateBiometricStatus(enabled: Boolean) {
        isBiometricEnabled = enabled
        sharedPrefs.edit().putBoolean("biometric_enabled", enabled).apply()
    }

    fun updateBusinessName(newName: String) {
        businessName = newName
        sharedPrefs.edit().putString("business_name", newName).apply()
        isEditingBusinessName = false
    }

    fun updateUmkmCategory(newCategory: String) {
        umkmCategory = newCategory
        sharedPrefs.edit().putString("umkm_category", newCategory).apply()
    }

    private fun clearTwoFactorConfig() {
        isTwoFactorEnabled = false
        sharedPrefs.edit().putBoolean("2fa_enabled", false).apply()
        // Placeholder untuk menghapus data dari penyimpanan permanen di masa depan:
        // preferences.edit().remove("2fa_secret").apply()
        // database.twoFactorDao().deleteAll()
        println("2FA Configuration Wiped: Clean start enabled.")
    }
}

