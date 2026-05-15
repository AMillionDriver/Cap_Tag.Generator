package com.axoloth.captaggenerator.service.security

import com.warrenstrange.googleauth.GoogleAuthenticator
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig
import java.net.URLEncoder

class TwoFactorSecurity {

    private val config: GoogleAuthenticatorConfig
    private val gAuth: GoogleAuthenticator

    // Strict configuration for better security
    init {
        // 1. SET PROPERTY DULU (Paling Penting!)
        System.setProperty("com.warrenstrange.googleauth.rng.algorithmProvider", "AndroidOpenSSL")

        // 2. BARU BUAT CONFIG DAN GAUTH
        config = GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
            .setWindowSize(1)
            .build()

        gAuth = GoogleAuthenticator(config)
    }



    /**
     * Generates a new raw secret key for the user.
     * This key is used for both QR Code and Manual Entry (after formatting).
     */
    fun generateRawSecretKey(): String {
        val credentials = gAuth.createCredentials()
        return credentials.key
    }

    /**
     * Formats the raw secret key into a human-readable string (e.g., ABCD-EFGH-IJKL-MNOP).
     * This is used for the "ALTERNATIVE" manual entry code in the UI.
     */
    fun formatSecretKeyForDisplay(rawSecret: String): String {
        return rawSecret.chunked(4).joinToString("-")
    }

    /**
     * Generates the otpauth URL for QR Code generation.
     * Encodes the email to handle special characters safely.
     */
    fun getOtpAuthUrl(secret: String, userEmail: String): String {
        val encodedEmail = URLEncoder.encode(userEmail, "UTF-8").replace("+", "%20")
        val issuer = "LapakAI"
        return "otpauth://totp/$issuer:$encodedEmail?secret=$secret&issuer=$issuer"
    }

    /**
     * Verifies if the provided code is valid for the current time window.
     */
    fun verifyCode(secret: String, code: Int): Boolean {
        return gAuth.authorize(secret, code)
    }

    /**
     * Verifies if the provided code is valid AND has not been used in the previous steps.
     * This ensures the user waits for a new token (30s) between verification steps.
     */
    fun verifySequentialCode(secret: String, code: Int, usedCodes: List<Int>): Boolean {
        // Prevent reuse of any code already entered in previous columns
        if (code in usedCodes) return false
        return verifyCode(secret, code)
    }
}
