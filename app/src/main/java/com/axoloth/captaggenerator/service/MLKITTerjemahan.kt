package com.axoloth.captaggenerator.service

import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions

class MLKITTerjemahan {

    fun translate(
        text: String,
        sourceLang: String,
        targetLang: String,
        onResult: (String) -> Unit
    ) {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLang)
            .setTargetLanguage(targetLang)
            .build()
            
        val translator = Translation.getClient(options)
        
        translator.downloadModelIfNeeded()
            .addOnSuccessListener {
                translator.translate(text)
                    .addOnSuccessListener { translatedText ->
                        onResult(translatedText)
                        translator.close()
                    }
                    .addOnFailureListener {
                        onResult(text) // Fallback to original
                        translator.close()
                    }
            }
            .addOnFailureListener {
                onResult(text)
                translator.close()
            }
    }
}
