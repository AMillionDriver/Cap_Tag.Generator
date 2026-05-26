package com.axoloth.captaggenerator.service.ai

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

object OcrService {
    private var recognizer: TextRecognizer? = null

    /**
     * Mengekstrak teks dari gambar menggunakan Google ML Kit.
     * Menggunakan model on-device yang disediakan oleh Google Play Services (Unbundled).
     * Lazy Init: Recognizer hanya dibuat saat dibutuhkan pertama kali.
     */
    suspend fun extractTextFromImage(context: Context, imageUri: Uri): String {
        return try {
            if (recognizer == null) {
                recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            }
            
            val image = InputImage.fromFilePath(context, imageUri)
            val result = recognizer!!.process(image).await()
            
            // Membersihkan teks: menghapus baris baru berlebih dan spasi ganda
            result.text.replace("\n", " ").replace("\\s+".toRegex(), " ").trim()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
    
    fun close() {
        recognizer?.close()
        recognizer = null
    }
}
