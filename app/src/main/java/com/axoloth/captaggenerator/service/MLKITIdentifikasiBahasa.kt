package com.axoloth.captaggenerator.service

import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentifier
import kotlinx.coroutines.launch

class MLKITIdentifikasiBahasa {
    private var languageIdentifier: LanguageIdentifier? = null

    /**
     * Lazy Init: Client identifikasi bahasa hanya dibuat saat akan digunakan.
     */
    fun identifyLanguage(text: String, onResult: (String) -> Unit) {
        if (text.isBlank()) return
        
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            if (languageIdentifier == null) {
                languageIdentifier = LanguageIdentification.getClient()
            }
            
            languageIdentifier!!.identifyLanguage(text)
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
    
    fun close() {
        languageIdentifier?.close()
        languageIdentifier = null
    }
}
