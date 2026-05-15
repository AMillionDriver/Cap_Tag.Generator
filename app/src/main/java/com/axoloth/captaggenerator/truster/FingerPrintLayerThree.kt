package com.axoloth.captaggenerator.truster

/**
 * 🔐 Layer 3 — Session Validation & Access Control
 * Menentukan apakah user yang sudah lolos fingerprint benar-benar boleh masuk ke app/session.
 */
class FingerPrintLayerThree {

    sealed class LayerThreeStatus {
        object Authorized : LayerThreeStatus()
        data class Denied(val message: String) : LayerThreeStatus()
    }

    /**
     * Memeriksa validitas session/token user setelah verifikasi identitas sukses.
     */
    fun validateSession(): LayerThreeStatus {
        // 1. Ambil session/token lokal (Simulasi)
        val isTokenExpired = checkTokenExpiration()
        val isAccountActive = checkAccountStatus()

        return if (!isTokenExpired && isAccountActive) {
            // Jika session valid, izinkan akses penuh
            LayerThreeStatus.Authorized
        } else {
            // Jika session invalid, arahkan ke login ulang meskipun fingerprint sukses
            LayerThreeStatus.Denied("Sesi tidak valid. Silakan login kembali dengan password.")
        }
    }

    private fun checkTokenExpiration(): Boolean {
        // TODO: Implementasi pengecekan expiry token yang sebenarnya.
        return false // Anggap tidak expired untuk development
    }

    private fun checkAccountStatus(): Boolean {
        // TODO: Implementasi pengecekan status akun ke server/database.
        return true // Anggap aktif untuk development
    }
}
