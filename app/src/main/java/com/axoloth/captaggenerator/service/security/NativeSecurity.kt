package com.axoloth.captaggenerator.service.security

/**
 * Interface untuk mengakses string rahasia yang tersimpan di C++ (Native Layer).
 * Kode di sini sangat sulit didekompilasi oleh hacker.
 */
object NativeSecurity {

    init {
        System.loadLibrary("native-lib")
    }

    /**
     * Mengambil Salt rahasia dari C++ untuk SQLCipher.
     */
    external fun getDatabaseSalt(): String

    /**
     * Mengambil Hash verifikasi untuk API Key AI dari Firebase.
     */
    external fun getAiVerificationHash(): String
}
