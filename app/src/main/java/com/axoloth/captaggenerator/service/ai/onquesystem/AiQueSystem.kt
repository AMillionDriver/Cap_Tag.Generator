package com.axoloth.captaggenerator.service.ai.onquesystem

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

sealed class GenerationStep {
    object Copywriting : GenerationStep()
    object Caption : GenerationStep()
    object Tags : GenerationStep()
    data class Completed(
        val copywriting: String,
        val productDescription: String,
        val tagsAndHashtags: String
    ) : GenerationStep()
}

object AiQueSystem {

    /**
     * Menjalankan antrean proses AI dengan delay visual untuk menghemat token
     * dan menghindari Rate Limit.
     */
    fun startGenerationQueue(
        productName: String,
        productModel: String,
        productPurpose: String,
        userKeywords: List<String>,
        tone: String
    ): Flow<GenerationStep> = flow {
        
        // 1. Tahap Copywriting & Deskripsi (Delay 30 detik)
        emit(GenerationStep.Copywriting)
        delay(30000) 
        val mockCopywriting = "Discover the $productName, where style meets innovation. Track your fitness, monitor health (HR/SpO2), and stay connected effortlessly."
        val mockDescription = "Premium $productModel with advanced $productPurpose features. $tone delivery."

        // 2. Tahap Caption (Delay 60 detik)
        emit(GenerationStep.Caption)
        delay(60000)
        // Simulasi hit AI di sini

        // 3. Tahap Tags & Hashtags (Delay 30 detik)
        emit(GenerationStep.Tags)
        delay(30000)
        val mockTags = userKeywords.joinToString(", ") + ", Smartwatch, Technology, #$productName #AI #Innovation"

        // 4. Selesai
        emit(GenerationStep.Completed(mockCopywriting, mockDescription, mockTags))
    }
}
