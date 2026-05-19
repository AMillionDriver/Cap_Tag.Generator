package com.axoloth.captaggenerator.service.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object KeyStoreManager {
    private const val KEY_ALIAS = "SQLCipher_Key_Alias"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_WRAPPER_ALIAS = "SQLCipher_Wrapper_Alias"

    // We use a wrapper key in Keystore to encrypt/decrypt the actual 32-byte database key
    // because SQLCipher requires a raw byte array key.
    
    fun getDatabaseKey(prefs: android.content.SharedPreferences): ByteArray {
        val encryptedKey = prefs.getString("encrypted_db_key", null)
        val rawKey = if (encryptedKey == null) {
            val generated = ByteArray(32).apply { SecureRandom().nextBytes(this) }
            val encrypted = encryptWithWrapper(generated)
            prefs.edit().putString("encrypted_db_key", encrypted).apply()
            generated
        } else {
            decryptWithWrapper(encryptedKey)
        }

        // Tambahkan Salt dari C++ untuk memperkuat keamanan (Local Obfuscation)
        val salt = NativeSecurity.getDatabaseSalt().toByteArray()
        val finalKey = ByteArray(rawKey.size)
        for (i in rawKey.indices) {
            finalKey[i] = (rawKey[i].toInt() xor salt[i % salt.size].toInt()).toByte()
        }
        return finalKey
    }

    private fun encryptWithWrapper(data: ByteArray): String {
        generateWrapperKeyIfNotExists()
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val secretKey = keyStore.getKey(KEY_WRAPPER_ALIAS, null) as SecretKey
        
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val encrypted = cipher.doFinal(data)
        
        val combined = ByteArray(iv.size + encrypted.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encrypted, 0, combined, iv.size, encrypted.size)
        
        return android.util.Base64.encodeToString(combined, android.util.Base64.DEFAULT)
    }

    private fun decryptWithWrapper(encryptedBase64: String): ByteArray {
        val combined = android.util.Base64.decode(encryptedBase64, android.util.Base64.DEFAULT)
        val ivSize = 12
        val iv = combined.copyOfRange(0, ivSize)
        val encryptedData = combined.copyOfRange(ivSize, combined.size)
        
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val secretKey = keyStore.getKey(KEY_WRAPPER_ALIAS, null) as SecretKey
        
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        
        return cipher.doFinal(encryptedData)
    }

    private fun generateWrapperKeyIfNotExists() {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (!keyStore.containsAlias(KEY_WRAPPER_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEY_WRAPPER_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
    }
}
