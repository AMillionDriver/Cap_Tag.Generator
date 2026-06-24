package com.axoloth.captaggenerator.service

import com.axoloth.captaggenerator.service.ai.fetch.AiFetchingLogic
import android.util.Log

class MLKITPenulisanUlang {

    suspend fun rewrite(text: String, style: String): String {
        val prompt = """
            Tulis ulang teks berikut dengan gaya: $style. 
            Pastikan maknanya tetap sama namun penyampaiannya lebih sesuai dengan gaya tersebut.
            Teks: $text
            
            Berikan HANYA hasil penulisan ulangnya saja.
        """.trimIndent()

        return try {
            AiFetchingLogic.generateTextWithFallback(prompt).value
        } catch (e: Exception) {
            Log.e("Rewrite", "Error: ${e.message}")
            text
        }
    }
}
