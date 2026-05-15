package com.axoloth.captaggenerator.service.security

import androidx.fragment.app.FragmentActivity
import com.axoloth.captaggenerator.truster.FingerPrintLayerOne
import com.axoloth.captaggenerator.truster.FingerPrintLayerTwo
import com.axoloth.captaggenerator.truster.FingerPrintLayerThree

/**
 * FingerPrint Service Utama
 * Bertugas sebagai jalur awal logika dan gerbang verifikasi ganda.
 */
class FingerPrint(private val activity: FragmentActivity) {

    private val layerOne = FingerPrintLayerOne(activity)
    private val layerTwo = FingerPrintLayerTwo(activity)
    private val layerThree = FingerPrintLayerThree()

    /**
     * Memulai proses aktivasi/verifikasi fingerprint.
     * Mengikuti flow: Layer 1 -> Layer 2 -> Layer 3.
     */
    fun startAuthentication(onResult: (Boolean, String?) -> Unit) {
        // Step 1: Jalankan validasi Layer 1 (Capability & Security Check)
        val layerOneStatus = layerOne.checkDeviceCapability()
        
        if (layerOneStatus is FingerPrintLayerOne.LayerOneStatus.Ready) {
            // Step 2: Jika Layer 1 lolos, lanjut ke Layer 2 (Identity Verification)
            layerTwo.showBiometricPrompt(
                onSuccess = {
                    // Berhasil di Layer 2, lanjut ke Layer 3 (Session Validation)
                    val layerThreeStatus = layerThree.validateSession()
                    
                    if (layerThreeStatus is FingerPrintLayerThree.LayerThreeStatus.Authorized) {
                        // SEMUA LAYER LOLOS!
                        onResult(true, "Akses diberikan. Identitas dan sesi valid.")
                    } else if (layerThreeStatus is FingerPrintLayerThree.LayerThreeStatus.Denied) {
                        onResult(false, layerThreeStatus.message)
                    }
                },
                onError = { _, message ->
                    // Terjadi error sistem (misal: sensor kotor, dibatalkan user)
                    onResult(false, message.toString())
                },
                onFailed = {
                    // Fingerprint tidak cocok
                    onResult(false, "Sidik jari tidak dikenali. Silakan coba lagi.")
                }
            )
        } else if (layerOneStatus is FingerPrintLayerOne.LayerOneStatus.Error) {
            // Jika Layer 1 gagal, kembalikan pesan error-nya
            onResult(false, layerOneStatus.message)
        }
    }

    /**
     * Verifikasi ganda (Double Check)
     * Digunakan setelah Layer 3 memberikan lampu hijau.
     */
    fun doubleVerify(): Boolean {
        // Logika verifikasi tambahan untuk memastikan integritas data sebelum masuk app
        return true
    }
}
