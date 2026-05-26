package com.axoloth.captaggenerator.service

import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentifier

class MLKITIdentifikasiBahasa {
    private val languageIdentifier: LanguageIdentifier = LanguageIdentification.getClient()

    fun identifyLanguage(text: String, onResult: (String) -> Unit) {
        if (text.isBlank()) return
        
        languageIdentifier.identifyLanguage(text)
            .addOnSuccessListener { languageCode ->
                if (languageCode != "und") {
                    onResult(languageCode)
                } else {
                    onResult("unknown")
                }
            }
            .addOnFailureListener {
                onResult("error")
            }
    }
}
