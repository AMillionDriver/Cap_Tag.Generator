package com.axoloth.captaggenerator.service.ai.onquesystem

import com.axoloth.captaggenerator.service.ai.fetch.AiFetchingLogic
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject

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
     * Menjalankan antrean proses AI dengan strategi Single-Hit JSON.
     * Mengambil semua data dalam satu request untuk menghemat token dan RPM.
     */
    fun startGenerationQueue(
        productName: String,
        productModel: String,
        productPurpose: String,
        userKeywords: List<String>,
        tone: String
    ): Flow<GenerationStep> = flow {
        
        // 1. Tahap Inisialisasi & Request (Visual Delay 10 detik)
        emit(GenerationStep.Copywriting)
        
        // Jalankan request AI di background sesegera mungkin
        val aiResultDeferred = try {
            fetchAiData(productName, productModel, productPurpose, userKeywords, tone)
        } catch (e: Exception) {
            null
        }

        delay(1500) // Visual delay untuk tahap 1

        // 2. Tahap Caption (Visual Delay 15 detik)
        emit(GenerationStep.Caption)
        delay(1500)

        // 3. Tahap Tags & Hashtags (Visual Delay 10 detik)
        emit(GenerationStep.Tags)
        delay(1000)

        // 4. Selesai - Gabungkan hasil asli dari AI (atau fallback jika gagal)
        if (aiResultDeferred != null) {
            emit(GenerationStep.Completed(
                aiResultDeferred.copywriting,
                aiResultDeferred.description,
                aiResultDeferred.tags
            ))
        } else {
            // Fallback jika AI gagal
            emit(GenerationStep.Completed(
                "Gagal mengambil data dari AI. Silakan coba lagi.",
                "Detail produk tidak tersedia.",
                "#Error #Retry"
            ))
        }
    }

    private suspend fun fetchAiData(
        name: String, model: String, purpose: String, keywords: List<String>, tone: String
    ): AiResponse? {
        val encryptedKey = AiFetchingLogic.fetchAndSecureGeminiKey() ?: return null
        val apiKey = AiFetchingLogic.getReadyApiKey(encryptedKey)
        
        val generativeModel = GenerativeModel(
            modelName = "gemini-2.5-flash-lite",
            apiKey = apiKey,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
            }
        )

        val prompt = """
            Kamu adalah ahli copywriting marketing profesional. 
            Buatlah konten promosi untuk produk berikut:
            Nama Produk: $name
            Model: $model
            Tujuan: $purpose
            Kata Kunci: ${keywords.joinToString(", ")}
            Nada Bicara: $tone

            Berikan respon HANYA dalam format JSON mentah dengan struktur berikut:
            {
              "copywriting": "teks copywriting panjang max 1000 token",
              "description": "deskripsi produk mendalam",
              "tags": "daftar hashtag dan tag dipisahkan koma"
            }
        """.trimIndent()

        return try {
            val response = generativeModel.generateContent(prompt)
            val jsonText = response.text ?: return null
            
            Log.d("AiQueSystem", "Raw AI Response: $jsonText")
            
            // Bersihkan teks jika AI menambahkan markdown (```json ... ```)
            val cleanedJson = jsonText.replace("```json", "").replace("```", "").trim()
            
            val jsonObject = JSONObject(cleanedJson)
            AiResponse(
                copywriting = jsonObject.getString("copywriting"),
                description = jsonObject.getString("description"),
                tags = jsonObject.getString("tags")
            )
        } catch (e: Exception) {
            Log.e("AiQueSystem", "Error in fetchAiData: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    private data class AiResponse(
        val copywriting: String,
        val description: String,
        val tags: String
    )
}
