package com.axoloth.captaggenerator.logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axoloth.captaggenerator.service.ai.onquesystem.AiQueSystem
import com.axoloth.captaggenerator.service.ai.onquesystem.GenerationStep
import kotlinx.coroutines.launch

class GenerateResultViewModel : ViewModel() {
    var copywriting by mutableStateOf("")
    var productDescription by mutableStateOf("")
    var tagsAndHashtags by mutableStateOf("")
    
    var currentStep by mutableStateOf<GenerationStep>(GenerationStep.Copywriting)
    var isFinished by mutableStateOf(false)

    fun startProcessing(
        productName: String,
        productModel: String,
        productPurpose: String,
        userKeywords: List<String>,
        tone: String
    ) {
        viewModelScope.launch {
            AiQueSystem.startGenerationQueue(
                productName, productModel, productPurpose, userKeywords, tone
            ).collect { step ->
                currentStep = step
                if (step is GenerationStep.Completed) {
                    copywriting = step.copywriting
                    productDescription = step.productDescription
                    tagsAndHashtags = step.tagsAndHashtags
                    isFinished = true
                }
            }
        }
    }
}
