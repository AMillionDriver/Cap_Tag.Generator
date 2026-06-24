package com.axoloth.captaggenerator.truster

import android.content.Context
import androidx.biometric.BiometricManager

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

        // BIOMETRIC_STRONG menjaga jalur biometric tetap memakai sensor Class 3.
        // DEVICE_CREDENTIAL memberi fallback ke PIN/Pola/Password perangkat jika biometric tidak tersedia.
        return when (biometricManager.canAuthenticate(BiometricAuthConfig.allowedAuthenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                // 2. Cek Session Login Lokal (Simulasi)
                if (isLocalSessionValid()) {
                    LayerOneStatus.Ready
                } else {
                    LayerOneStatus.Error("Sesi login berakhir. Silakan login manual.", false)
                }
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                LayerOneStatus.Error("Perangkat tidak mendukung biometrik dan kunci layar perangkat belum tersedia.", true)
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                LayerOneStatus.Error("Autentikasi perangkat sedang tidak tersedia. Coba lagi beberapa saat.", true)
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                LayerOneStatus.Error("Aktifkan sidik jari/wajah atau PIN, pola, dan password perangkat terlebih dahulu.", true)
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
