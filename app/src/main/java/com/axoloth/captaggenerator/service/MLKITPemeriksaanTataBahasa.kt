package com.axoloth.captaggenerator.service

import com.axoloth.captaggenerator.service.ai.fetch.AiFetchingLogic
import com.google.ai.client.generativeai.GenerativeModel
import android.util.Log

class MLKITPemeriksaanTataBahasa {

    suspend fun checkGrammar(text: String, language: String): String {
        val encryptedKey = AiFetchingLogic.fetchAndSecureGeminiKey() ?: return text
        val apiKey = AiFetchingLogic.getReadyApiKey(encryptedKey)
        
        val generativeModel = GenerativeModel(
            modelName = "gemini-2.5-flash-lite",
            apiKey = apiKey
        )

        val prompt = """
            Perbaiki tata bahasa, ejaan, dan tanda baca dari teks berikut tanpa mengubah maknanya secara drastis. 
            Bahasa: $language
            Teks: $text
            
            Berikan HANYA hasil perbaikannya saja.
        """.trimIndent()

        return try {
            val response = generativeModel.generateContent(prompt)
            response.text ?: text
        } catch (e: Exception) {
            Log.e("GrammarCheck", "Error: ${e.message}")
            text
        }
    }
}
