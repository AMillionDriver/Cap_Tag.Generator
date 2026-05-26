package com.axoloth.captaggenerator.logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class GenerateScreenViewModel : ViewModel() {
    var productModel by mutableStateOf("")
    var productPurpose by mutableStateOf("")
    var keywordsInput by mutableStateOf("")
    val keywords = mutableStateListOf<String>()
    
    var selectedTone by mutableStateOf("Hype")
    val tones = listOf("Hype", "Formal", "Santai", "Profesional", "Lucu")

    fun addKeyword() {
        if (keywordsInput.isNotBlank()) {
            val newKeywords = keywordsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            keywords.addAll(newKeywords)
            keywordsInput = ""
        }
    }

    fun removeKeyword(keyword: String) {
        keywords.remove(keyword)
    }

    fun resetInputs() {
        productModel = ""
        productPurpose = ""
        keywordsInput = ""
        keywords.clear()
        selectedTone = "Hype"
    }

    fun onGenerateClick() {
        // Logic untuk generate nantinya
    }
}
