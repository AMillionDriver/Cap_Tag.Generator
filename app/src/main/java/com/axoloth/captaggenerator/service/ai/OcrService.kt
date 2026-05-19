package com.axoloth.captaggenerator.service.ai

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

object OcrService {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Mengekstrak teks dari gambar menggunakan Google ML Kit.
     * Menggunakan model on-device yang disediakan oleh Google Play Services (Unbundled).
     */
    suspend fun extractTextFromImage(context: Context, imageUri: Uri): String {
        return try {
            val image = InputImage.fromFilePath(context, imageUri)
            val result = recognizer.process(image).await()
            
            // Membersihkan teks: menghapus baris baru berlebih dan spasi ganda
            result.text.replace("\n", " ").replace("\\s+".toRegex(), " ").trim()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}
