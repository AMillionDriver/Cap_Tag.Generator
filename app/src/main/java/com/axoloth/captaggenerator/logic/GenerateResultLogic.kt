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
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class GenerateResultViewModel(private val userDao: UserDao) : ViewModel() {
    var copywriting by mutableStateOf("")
    var productDescription by mutableStateOf("")
    var tagsAndHashtags by mutableStateOf("")
    
    var currentStep by mutableStateOf<GenerationStep>(GenerationStep.Copywriting)
    var isFinished by mutableStateOf(false)
    var isSaving by mutableStateOf(false)
    var selectedImageUriString by mutableStateOf<String?>(null)
    var generationErrorMessage by mutableStateOf<String?>(null)
    private set

    // Simpan parameter untuk fungsi Regenerate
    private var lastParams: GenerationParams? = null
    private var loadHistoryJob: Job? = null
    private var generationJob: Job? = null

    fun loadFromHistory(historyId: Int) {
        loadHistoryJob?.cancel()
        loadHistoryJob = viewModelScope.launch {
            val item = userDao.getHistoryById(historyId)
            item?.let {
                copywriting = it.copywriting
                productDescription = it.productDescription
                tagsAndHashtags = it.tagsAndHashtags
                selectedImageUriString = it.imageUri
                lastParams = GenerationParams(
                    productName = it.productName,
                    productModel = "", // Optional: history might not store this separately
                    productPurpose = "",
                    userKeywords = emptyList(),
                    tone = "",
                    businessName = "",
                    salesLink = ""
                )
                isFinished = true
            }
        }
    }

    fun startProcessing(
        productName: String,
        productModel: String,
        productPurpose: String,
        userKeywords: List<String>,
        tone: String,
        businessName: String = "",
        salesLink: String = ""
    ) {
        lastParams = GenerationParams(productName, productModel, productPurpose, userKeywords, tone, businessName, salesLink)
        isFinished = false
        generationErrorMessage = null
        
        generationJob?.cancel()
        generationJob = viewModelScope.launch {
            AiQueSystem.startGenerationQueue(
                productName, productModel, productPurpose, userKeywords, tone, businessName, salesLink
            ).collect { step ->
                currentStep = step
                when (step) {
                    is GenerationStep.Completed -> {
                        copywriting = step.copywriting
                        productDescription = step.productDescription
                        tagsAndHashtags = step.tagsAndHashtags
                        isFinished = true
                    }

                    is GenerationStep.Error -> {
                        generationErrorMessage = step.message
                        isFinished = false
                    }

                    else -> Unit
                }
            }
        }
    }

    fun consumeGenerationError(): String? {
        return generationErrorMessage.also {
            generationErrorMessage = null
            currentStep = GenerationStep.Copywriting
        }
    }

    fun regenerate() {
        lastParams?.let { params ->
            startProcessing(
                params.productName,
                params.productModel,
                params.productPurpose,
                params.userKeywords,
                params.tone,
                params.businessName,
                params.salesLink
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
        val tone: String,
        val businessName: String,
        val salesLink: String
    )
}
