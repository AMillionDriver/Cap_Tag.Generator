package com.axoloth.captaggenerator.service.ai.onquesystem

import com.axoloth.captaggenerator.service.ai.fetch.AiUnavailableException
import com.axoloth.captaggenerator.service.ai.fetch.AiFetchingLogic
import android.util.Log
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
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
    data class Error(val message: String) : GenerationStep()
}

object AiQueSystem {
    private const val AI_GENERATION_TIMEOUT_MS = 60_000L
    private const val COPYWRITING_STEP_DELAY_MS = 1_500L
    private const val CAPTION_STEP_DELAY_MS = 2_000L
    private const val TAGS_STEP_DELAY_MS = 1_000L

    /**
     * Menjalankan antrean proses AI dengan strategi Single-Hit JSON.
     * Mengambil semua data dalam satu request untuk menghemat token dan RPM.
     */
    fun startGenerationQueue(
        productName: String,
        productModel: String,
        productPurpose: String,
        userKeywords: List<String>,
        tone: String,
        businessName: String = "",
        salesLink: String = ""
    ): Flow<GenerationStep> = flow {
        coroutineScope {
            // 1. Tahap Inisialisasi & Request
            emit(GenerationStep.Copywriting)

            // Jalankan request AI di background sesegera mungkin sambil progress UI tetap berjalan.
            val aiResultDeferred = async {
                runCatching {
                    withTimeout(AI_GENERATION_TIMEOUT_MS) {
                        fetchAiData(
                            productName,
                            productModel,
                            productPurpose,
                            userKeywords,
                            tone,
                            businessName,
                            salesLink
                        )
                    }
                }
            }

            delay(COPYWRITING_STEP_DELAY_MS)

            // 2. Tahap Caption
            emit(GenerationStep.Caption)
            delay(CAPTION_STEP_DELAY_MS)

            // 3. Tahap Tags & Hashtags
            emit(GenerationStep.Tags)
            delay(TAGS_STEP_DELAY_MS)

            // 4. Selesai - Gabungkan hasil asli dari AI, atau tampilkan error jika semua provider gagal.
            val aiResult = aiResultDeferred.await()
            val aiResponse = aiResult.getOrNull()
            if (aiResponse != null) {
                emit(
                    GenerationStep.Completed(
                        aiResponse.copywriting,
                        aiResponse.description,
                        aiResponse.tags
                    )
                )
            } else {
                val message = (aiResult.exceptionOrNull() as? AiUnavailableException)?.message
                    ?: AiFetchingLogic.USER_FACING_UNAVAILABLE_MESSAGE
                emit(GenerationStep.Error(message))
            }
        }
    }

    private suspend fun fetchAiData(
        name: String, model: String, purpose: String, keywords: List<String>, tone: String, businessName: String, salesLink: String
    ): AiResponse {
        val prompt = """
            Kamu adalah ahli copywriting marketing profesional. 
            Buatlah konten promosi untuk produk berikut:
            Nama Produk: $name
            Model: $model
            Tujuan: $purpose
            Kata Kunci: ${keywords.joinToString(", ")}
            Nada Bicara: $tone
            
            INFORMASI USAHA (Wajib disisipkan di akhir copywriting secara natural sebagai Call to Action):
            Nama Usaha: $businessName
            Link Jualan: $salesLink

            Berikan respon HANYA dalam format JSON mentah dengan struktur berikut:
            {
              "copywriting": "teks copywriting panjang max 1000 token",
              "description": "deskripsi produk mendalam",
              "tags": "daftar hashtag dan tag dipisahkan koma"
            }
        """.trimIndent()

        return AiFetchingLogic.generateWithFallback(
            prompt = prompt,
            expectJson = true,
            transform = ::parseAiResponse
        ).also { result ->
            Log.d("AiQueSystem", "AI response accepted from ${result.providerName}")
        }.value
    }

    private fun parseAiResponse(rawText: String): AiResponse? {
        return runCatching {
            Log.d("AiQueSystem", "Raw AI Response: $rawText")

            // Bersihkan teks jika AI menambahkan markdown (```json ... ```)
            val cleanedJson = rawText
                .replace("```json", "", ignoreCase = true)
                .replace("```", "")
                .trim()

            val jsonObject = JSONObject(cleanedJson)
            AiResponse(
                copywriting = jsonObject.getString("copywriting"),
                description = jsonObject.getString("description"),
                tags = jsonObject.getString("tags")
            )
        }.getOrNull()
    }

    private data class AiResponse(
        val copywriting: String,
        val description: String,
        val tags: String
    )
}
