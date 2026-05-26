package com.axoloth.captaggenerator.logic.fragment

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.axoloth.captaggenerator.service.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class RecordingState {
    IDLE, RECORDING, LOCKED, CANCELLED, SENT, PROCESSING
}

class MicViewModel(application: Application) : AndroidViewModel(application) {
    var recordingState by mutableStateOf(RecordingState.IDLE)
    var recordingDuration by mutableStateOf(0L) // in milliseconds
    var isLocked by mutableStateOf(false)
    var transcribedText by mutableStateOf("") // Text results from speech
    var detectedLanguage by mutableStateOf("id") // Default to Indonesian
    
    // Smart Workflow Preferences (Hybrid Design)
    var autoCorrectGrammar by mutableStateOf(true)
    var autoRewrite by mutableStateOf(false)
    var rewriteStyle by mutableStateOf("Formal")
    var autoTranslate by mutableStateOf(false)
    var targetLanguage by mutableStateOf("en")

    private val speechService = MicMLKIT(application).apply {
        onPartialResult = { transcribedText = it }
        onResult = { 
            transcribedText = it 
            identifyLanguage(it)
        }
        onError = { /* Handle speech errors */ }
    }

    private val langIdentifier = MLKITIdentifikasiBahasa()
    private val grammarChecker = MLKITPemeriksaanTataBahasa()
    private val rewriter = MLKITPenulisanUlang()
    private val translator = MLKITTerjemahan()

    private var timerJob: Job? = null

    fun startRecording() {
        transcribedText = ""
        recordingState = RecordingState.RECORDING
        speechService.startListening()
        startTimer()
    }

    fun lockRecording() {
        isLocked = true
        recordingState = RecordingState.LOCKED
    }

    fun stopRecording(isSent: Boolean) {
        stopTimer()
        speechService.stopListening()
        
        if (isSent) {
            recordingState = RecordingState.SENT
            processWorkflow()
        } else {
            recordingState = RecordingState.CANCELLED
            resetAfterDelay()
        }
    }

    fun cancelRecording() {
        stopTimer()
        speechService.stopListening()
        recordingState = RecordingState.CANCELLED
        resetAfterDelay()
    }

    private fun identifyLanguage(text: String) {
        langIdentifier.identifyLanguage(text) { langCode ->
            if (langCode != "unknown" && langCode != "error") {
                detectedLanguage = langCode
            }
        }
    }

    private fun processWorkflow() {
        recordingState = RecordingState.PROCESSING
        viewModelScope.launch {
            var currentText = transcribedText

            // 1. Identifikasi Bahasa (Background sudah jalan, tapi pastikan sekali lagi)
            identifyLanguage(currentText)
            delay(500) // Beri waktu sebentar untuk deteksi

            // 2. Pemeriksaan Tata Bahasa (Gemini)
            if (autoCorrectGrammar) {
                currentText = grammarChecker.checkGrammar(currentText, detectedLanguage)
            }

            // 3. Penulisan Ulang (Gemini)
            if (autoRewrite) {
                currentText = rewriter.rewrite(currentText, rewriteStyle)
            }

            // 4. Terjemahan (ML Kit On-Device)
            if (autoTranslate && detectedLanguage != targetLanguage) {
                val deferredTranslate = CompletableDeferred<String>()
                translator.translate(currentText, detectedLanguage, targetLanguage) { translated ->
                    deferredTranslate.complete(translated)
                }
                currentText = deferredTranslate.await()
            }

            transcribedText = currentText
            recordingState = RecordingState.IDLE
            reset()
        }
    }

    private fun resetAfterDelay() {
        viewModelScope.launch {
            delay(1000)
            reset()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            while (recordingState == RecordingState.RECORDING || recordingState == RecordingState.LOCKED) {
                recordingDuration = System.currentTimeMillis() - startTime
                delay(100)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun reset() {
        stopTimer()
        recordingState = RecordingState.IDLE
        recordingDuration = 0L
        isLocked = false
    }

    override fun onCleared() {
        super.onCleared()
        speechService.destroy()
    }
}

// Helper untuk menunggu callback translator
class CompletableDeferred<T> {
    private var result: T? = null
    private var isCompleted = false

    fun complete(value: T) {
        result = value
        isCompleted = true
    }

    suspend fun await(): T {
        while (!isCompleted) {
            delay(50)
        }
        return result!!
    }
}
