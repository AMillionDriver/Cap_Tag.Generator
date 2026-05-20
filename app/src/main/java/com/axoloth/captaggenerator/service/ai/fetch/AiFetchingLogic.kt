package com.axoloth.captaggenerator.service.ai.fetch

import android.util.Base64
import android.util.Log
import com.axoloth.captaggenerator.service.security.AesSecurity
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.tasks.await

object AiFetchingLogic {
    private const val TAG = "AiFetchingLogic"
    private const val GEMINI_AI_KEY = "Gemini_AI"

    /**
     * Mengambil API Key dari Firebase Remote Config, mendekode Base64, 
     * lalu mengamankannya dengan AES.
     */
    suspend fun fetchAndSecureGeminiKey(): String? {
        return try {
            val remoteConfig = FirebaseRemoteConfig.getInstance()
            
            // Konfigurasi fetch (misal: refresh setiap 1 jam dalam mode normal)
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build()
            remoteConfig.setConfigSettingsAsync(configSettings)

            // Ambil data dari Firebase
            val fetchSuccessful = remoteConfig.fetchAndActivate().await()
            
            if (fetchSuccessful || remoteConfig.info.lastFetchStatus == FirebaseRemoteConfig.LAST_FETCH_STATUS_SUCCESS) {
                val maskedKey = remoteConfig.getString(GEMINI_AI_KEY)
                
                if (maskedKey.isNotEmpty()) {
                    // 1. Decode Base64 (Masking dari Firebase)
                    val decodedBytes = Base64.decode(maskedKey, Base64.DEFAULT)
                    val rawApiKey = String(decodedBytes, Charsets.UTF_8)
                    
                    // 2. Enkripsi ulang dengan AesSecurity agar aman di memori/storage lokal
                    val encryptedKey = AesSecurity.encrypt(rawApiKey)
                    
                    Log.d(TAG, "Gemini API Key successfully fetched and secured.")
                    return encryptedKey
                }
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching Gemini Key: ${e.message}")
            null
        }
    }

    /**
     * Memperoleh API Key asli yang siap digunakan untuk request AI.
     */
    fun getReadyApiKey(encryptedKey: String): String {
        return try {
            AesSecurity.decrypt(encryptedKey)
        } catch (e: Exception) {
            Log.e(TAG, "Error decrypting API Key: ${e.message}")
            ""
        }
    }
}
