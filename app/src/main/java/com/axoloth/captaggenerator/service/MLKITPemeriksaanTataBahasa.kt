package com.axoloth.captaggenerator.service

import com.axoloth.captaggenerator.service.ai.fetch.AiFetchingLogic
import android.util.Log

class MLKITPemeriksaanTataBahasa {

    suspend fun checkGrammar(text: String, language: String): String {
        val prompt = """
            Perbaiki tata bahasa, ejaan, dan tanda baca dari teks berikut tanpa mengubah maknanya secara drastis. 
            Bahasa: $language
            Teks: $text
            
            Berikan HANYA hasil perbaikannya saja.
        """.trimIndent()

        return try {
            AiFetchingLogic.generateTextWithFallback(prompt).value
        } catch (e: Exception) {
            Log.e("GrammarCheck", "Error: ${e.message}")
            text
        }
    }
}
