package com.axoloth.captaggenerator.service

import com.axoloth.captaggenerator.service.ai.fetch.AiFetchingLogic
import com.google.ai.client.generativeai.GenerativeModel
import android.util.Log

class MLKITPenulisanUlang {

    suspend fun rewrite(text: String, style: String): String {
        val encryptedKey = AiFetchingLogic.fetchAndSecureGeminiKey() ?: return text
        val apiKey = AiFetchingLogic.getReadyApiKey(encryptedKey)
        
        val generativeModel = GenerativeModel(
            modelName = "gemini-2.5-flash-lite",
            apiKey = apiKey
        )

        val prompt = """
            Tulis ulang teks berikut dengan gaya: $style. 
            Pastikan maknanya tetap sama namun penyampaiannya lebih sesuai dengan gaya tersebut.
            Teks: $text
            
            Berikan HANYA hasil penulisan ulangnya saja.
        """.trimIndent()

        return try {
            val response = generativeModel.generateContent(prompt)
            response.text ?: text
        } catch (e: Exception) {
            Log.e("Rewrite", "Error: ${e.message}")
            text
        }
    }
}
