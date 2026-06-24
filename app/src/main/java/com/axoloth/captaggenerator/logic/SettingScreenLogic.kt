package com.axoloth.captaggenerator.logic

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.axoloth.captaggenerator.service.security.TwoFactorStore

class SettingScreenViewModel(context: Context) : ViewModel() {
    private val appContext = context.applicationContext
    private val sharedPrefs = appContext.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    var isTwoFactorEnabled by mutableStateOf(TwoFactorStore.isEnabled(appContext))
        private set

    var isBiometricEnabled by mutableStateOf(sharedPrefs.getBoolean("biometric_enabled", false))
        private set

    // Business Profile State
    var businessName by mutableStateOf(sharedPrefs.getString("business_name", "Warung Kopi Jaya") ?: "Warung Kopi Jaya")
        private set
    
    var isEditingBusinessName by mutableStateOf(false)

    var umkmCategory by mutableStateOf(sharedPrefs.getString("umkm_category", "Kuliner") ?: "Kuliner")
        private set

    var salesLink by mutableStateOf(sharedPrefs.getString("sales_link", "https://wa.me/628123456789") ?: "https://wa.me/628123456789")
        private set

    var isEditingSalesLink by mutableStateOf(false)

    // AI System State
    var selectedTone by mutableStateOf(sharedPrefs.getString("tone_of_voice", "Gacor (Santai)") ?: "Gacor (Santai)")
        private set

    val toneOptions = listOf(
        ToneOption("Professional", "Precise and formal communication.", "💼"),
        ToneOption("Friendly", "Warm and approachable tone.", "🤝"),
        ToneOption("Calm", "Soothing and patient delivery.", "🪷"),
        ToneOption("Enthusiastic", "Energetic and dynamic response.", "📢"),
        ToneOption("Direct", "Clear and concise interaction.", "🎯"),
        ToneOption("Gacor (Santai)", "Santai tapi tetep asik buat jualan.", "🔥")
    )

    fun onTwoFactorToggle(enabled: Boolean, onNavigateToSetup: () -> Unit) {
        if (enabled) {
            onNavigateToSetup()
        } else {
            // Full Reset: Bersihkan semua konfigurasi saat dimatikan
            clearTwoFactorConfig()
        }
    }

    fun updateTwoFactorStatus(enabled: Boolean) {
        isTwoFactorEnabled = enabled && TwoFactorStore.hasSecret(appContext)
        sharedPrefs.edit().putBoolean("2fa_enabled", isTwoFactorEnabled).apply()
    }

    fun refreshTwoFactorStatus() {
        isTwoFactorEnabled = TwoFactorStore.isEnabled(appContext)
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

    fun updateSalesLink(newLink: String) {
        salesLink = newLink
        sharedPrefs.edit().putString("sales_link", newLink).apply()
        isEditingSalesLink = false
    }

    fun updateTone(newTone: String) {
        selectedTone = newTone
        sharedPrefs.edit().putString("tone_of_voice", newTone).apply()
    }

    private fun clearTwoFactorConfig() {
        TwoFactorStore.clear(appContext)
        isTwoFactorEnabled = false
    }
}

data class ToneOption(val title: String, val description: String, val emoji: String)


