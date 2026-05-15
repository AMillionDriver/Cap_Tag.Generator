package com.axoloth.captaggenerator.truster

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL

/**
 * 🧱 Layer 1 — Device Capability & Security Check
 * Memastikan device memang siap dan aman untuk menggunakan biometric authentication.
 */
class FingerPrintLayerOne(private val context: Context) {

    sealed class LayerOneStatus {
        object Ready : LayerOneStatus()
        data class Error(val message: String, val canResolve: Boolean = false) : LayerOneStatus()
    }

    /**
     * Menjalankan semua pengecekan keamanan di Layer 1.
     */
    fun checkDeviceCapability(): LayerOneStatus {
        val biometricManager = BiometricManager.from(context)

        // 1. Cek Hardware & Pendaftaran (BIOMETRIC_STRONG)
        // BIOMETRIC_STRONG memastikan kita menggunakan sensor yang aman (Class 3)
        // DEVICE_CREDENTIAL memungkinkan fallback ke PIN/Pattern jika sensor tidak ada
        return when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                // 2. Cek Session Login Lokal (Simulasi)
                if (isLocalSessionValid()) {
                    LayerOneStatus.Ready
                } else {
                    LayerOneStatus.Error("Sesi login berakhir. Silakan login manual.", false)
                }
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                LayerOneStatus.Error("Perangkat tidak mendukung hardware sidik jari.", false)
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                LayerOneStatus.Error("Sensor sidik jari sedang tidak tersedia.", true)
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                LayerOneStatus.Error("Sidik jari belum didaftarkan di pengaturan perangkat.", true)
            }
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                LayerOneStatus.Error("Update keamanan diperlukan untuk menggunakan fitur ini.", true)
            }
            else -> {
                LayerOneStatus.Error("Terjadi kesalahan yang tidak dikenal pada sistem keamanan.", false)
            }
        }
    }

    /**
     * Memeriksa apakah session login lokal masih tersedia.
     * (Token masih ada, user belum logout).
     */
    private fun isLocalSessionValid(): Boolean {
        // TODO: Implementasi pengecekan token/session yang sebenarnya nanti.
        // Sementara kita anggap selalu valid untuk keperluan development UI.
        return true
    }
}
