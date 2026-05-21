package com.axoloth.captaggenerator.logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axoloth.captaggenerator.room.HistoryEntity
import com.axoloth.captaggenerator.room.UserDao
import com.axoloth.captaggenerator.service.ai.onquesystem.AiQueSystem
import com.axoloth.captaggenerator.service.ai.onquesystem.GenerationStep
import kotlinx.coroutines.launch

class GenerateResultViewModel(private val userDao: UserDao) : ViewModel() {
    var copywriting by mutableStateOf("")
    var productDescription by mutableStateOf("")
    var tagsAndHashtags by mutableStateOf("")
    
    var currentStep by mutableStateOf<GenerationStep>(GenerationStep.Copywriting)
    var isFinished by mutableStateOf(false)
    var isSaving by mutableStateOf(false)

    // Simpan parameter untuk fungsi Regenerate
    private var lastParams: GenerationParams? = null

    fun startProcessing(
        productName: String,
        productModel: String,
        productPurpose: String,
        userKeywords: List<String>,
        tone: String
    ) {
        lastParams = GenerationParams(productName, productModel, productPurpose, userKeywords, tone)
        isFinished = false
        
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

    fun regenerate() {
        lastParams?.let { params ->
            startProcessing(
                params.productName,
                params.productModel,
                params.productPurpose,
                params.userKeywords,
                params.tone
            )
        }
    }

    fun saveToHistory(imageUri: String?, onComplete: () -> Unit) {
        val params = lastParams ?: return
        viewModelScope.launch {
            isSaving = true
            val history = HistoryEntity(
                productName = params.productName,
                copywriting = copywriting,
                productDescription = productDescription,
                tagsAndHashtags = tagsAndHashtags,
                imageUri = imageUri
            )
            userDao.insertHistory(history)
            isSaving = false
            onComplete()
        }
    }

    private data class GenerationParams(
        val productName: String,
        val productModel: String,
        val productPurpose: String,
        val userKeywords: List<String>,
        val tone: String
    )
}
