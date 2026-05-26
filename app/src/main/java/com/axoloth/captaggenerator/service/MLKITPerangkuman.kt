package com.axoloth.captaggenerator.service

import com.axoloth.captaggenerator.service.ai.fetch.AiFetchingLogic
import com.google.ai.client.generativeai.GenerativeModel
import android.util.Log

class MLKITPerangkuman {

    suspend fun summarize(text: String): String {
        val encryptedKey = AiFetchingLogic.fetchAndSecureGeminiKey() ?: return text
        val apiKey = AiFetchingLogic.getReadyApiKey(encryptedKey)
        
        val generativeModel = GenerativeModel(
            modelName = "gemini-2.5-flash-lite",
            apiKey = apiKey
        )

        val prompt = """
            Rangkum teks berikut menjadi lebih singkat, padat, dan jelas tanpa menghilangkan poin pentingnya.
            Teks: $text
            
            Berikan HANYA hasil rangkumannya saja.
        """.trimIndent()

        return try {
            val response = generativeModel.generateContent(prompt)
            response.text ?: text
        } catch (e: Exception) {
            Log.e("Summarize", "Error: ${e.message}")
            text
        }
    }
}
