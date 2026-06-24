package com.axoloth.captaggenerator.truster

import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * 🛡️ Layer 2 — Identity Verification
 * Menangani interaksi langsung dengan Biometric Prompt Android (Face/Fingerprint).
 */
class FingerPrintLayerTwo(private val activity: FragmentActivity) {

    /**
     * Menampilkan Biometric Prompt resmi dari Android.
     */
    fun showBiometricPrompt(
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: (Int, CharSequence) -> Unit,
        onFailed: () -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess(result)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errorCode, errString)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFailed()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Verifikasi Identitas")
            .setSubtitle("Gunakan biometrik atau PIN/pola/password perangkat")
            .setAllowedAuthenticators(BiometricAuthConfig.allowedAuthenticators)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
