package com.axoloth.captaggenerator.service

import com.axoloth.captaggenerator.service.ai.fetch.AiFetchingLogic
import android.util.Log

class MLKITPerangkuman {

    suspend fun summarize(text: String): String {
        val prompt = """
            Rangkum teks berikut menjadi lebih singkat, padat, dan jelas tanpa menghilangkan poin pentingnya.
            Teks: $text
            
            Berikan HANYA hasil rangkumannya saja.
        """.trimIndent()

        return try {
            AiFetchingLogic.generateTextWithFallback(prompt).value
        } catch (e: Exception) {
            Log.e("Summarize", "Error: ${e.message}")
            text
        }
    }
}
