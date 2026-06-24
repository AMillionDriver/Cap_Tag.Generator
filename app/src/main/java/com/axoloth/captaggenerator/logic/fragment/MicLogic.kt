package com.axoloth.captaggenerator.logic.fragment

import android.app.Application
import android.content.pm.PackageManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.axoloth.captaggenerator.service.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

enum class RecordingState {
    IDLE, RECORDING, LOCKED, CANCELLED, SENT, PROCESSING
}

class MicViewModel(application: Application) : AndroidViewModel(application) {
    var recordingState by mutableStateOf(RecordingState.IDLE)
    var recordingDuration by mutableStateOf(0L) // in milliseconds
    var isLocked by mutableStateOf(false)
    var transcribedText by mutableStateOf("") // Text results from speech
    var detectedLanguage by mutableStateOf("id") // Default to Indonesian
    var speechErrorMessage by mutableStateOf<String?>(null)
        private set
    var micLevel by mutableStateOf(0f)
        private set
    var isListening by mutableStateOf(false)
        private set
    
    var hasPermission by mutableStateOf(
        ContextCompat.checkSelfPermission(
            application,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    )
    // Result handling
    var transcriptionDestination by mutableStateOf<String?>(null) // "model" or "purpose"
    var finalResultText by mutableStateOf("")

    // Smart Workflow Preferences (Hybrid Design)
    var autoCorrectGrammar by mutableStateOf(true)
    var autoRewrite by mutableStateOf(false)
    var rewriteStyle by mutableStateOf("Formal")
    var autoTranslate by mutableStateOf(false)
    var targetLanguage by mutableStateOf("en")

    private val speechService = MicMLKIT(application).apply {
        onPartialResult = { 
            speechErrorMessage = null
            transcribedText = it
        }
        onResult = { 
            speechErrorMessage = null
            transcribedText = it
            identifyLanguage(it)
        }
        onError = { message ->
            speechErrorMessage = when (message) {
                "No speech detected", "Tidak ada suara terdeteksi" ->
                    "Belum ada suara yang tertangkap. Coba dekatkan mic dan ulangi."
                "Permission missing" ->
                    "Izin mikrofon belum aktif."
                else -> message
            }
        }
        onRmsChanged = { rms ->
            micLevel = rms.coerceIn(0f, 12f) / 12f
        }
        onListeningStarted = {
            isListening = true
            speechErrorMessage = null
        }
        onListeningStopped = {
            isListening = false
        }
    }

    private val langIdentifier = MLKITIdentifikasiBahasa()
    private val grammarChecker = MLKITPemeriksaanTataBahasa()
    private val rewriter = MLKITPenulisanUlang()
    private val translator = MLKITTerjemahan()

    private var timerJob: Job? = null

    fun updatePermissionStatus(granted: Boolean) {
        hasPermission = granted
        if (!granted) {
            speechErrorMessage = "Izin mikrofon ditolak."
        }
    }

    fun startRecordingFlow() {
        if (!hasPermission) return
        startRecording()
    }

    fun startRecording() {
        transcribedText = ""
        finalResultText = ""
        transcriptionDestination = null
        speechErrorMessage = null
        micLevel = 0f
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
            if (transcribedText.isBlank()) {
                speechErrorMessage = "Belum ada transkrip. Coba rekam suara lagi."
                recordingState = RecordingState.RECORDING
                startTimer()
                speechService.startListening()
                return
            }
            recordingState = RecordingState.SENT
            processWorkflow()
        } else {
            recordingState = RecordingState.CANCELLED
            resetAfterDelay()
        }
    }

    fun cancelRecording() {
        stopTimer()
        speechService.cancelListening()
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
            if (currentText.isBlank()) {
                speechErrorMessage = "Belum ada transkrip. Coba rekam suara lagi."
                resetAfterDelay()
                return@launch
            }

            // 1. Identifikasi Bahasa (Background sudah jalan, tapi pastikan sekali lagi)
            identifyLanguage(currentText)
            delay(500) // Beri waktu sebentar untuk deteksi

            // 2. Pemeriksaan Tata Bahasa (Gemini)
            if (autoCorrectGrammar) {
                currentText = withTimeoutOrNull(TEXT_WORKFLOW_AI_TIMEOUT_MS) {
                    grammarChecker.checkGrammar(currentText, detectedLanguage)
                } ?: currentText
            }

            // 3. Penulisan Ulang (Gemini)
            if (autoRewrite) {
                currentText = withTimeoutOrNull(TEXT_WORKFLOW_AI_TIMEOUT_MS) {
                    rewriter.rewrite(currentText, rewriteStyle)
                } ?: currentText
            }

            // 4. Terjemahan (ML Kit On-Device)
            if (autoTranslate && detectedLanguage != targetLanguage) {
                val deferredTranslate = CompletableDeferred<String>()
                translator.translate(currentText, detectedLanguage, targetLanguage) { translated ->
                    if (!deferredTranslate.isCompleted) {
                        deferredTranslate.complete(translated)
                    }
                }
                currentText = withTimeoutOrNull(TRANSLATION_TIMEOUT_MS) {
                    deferredTranslate.await()
                } ?: currentText
            }

            finalResultText = currentText
            // Note: We don't reset immediately, we wait for destination selection in UI
        }
    }

    fun selectTranscriptionDestination(destination: String) {
        if (finalResultText.isBlank()) return
        transcriptionDestination = destination
    }

    fun applyResultToDestination() {
        // Dipanggil oleh parent UI setelah finalResultText berhasil dipindahkan ke field tujuan.
        reset()
        finalResultText = ""
        transcriptionDestination = null
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
        isListening = false
        micLevel = 0f
        speechErrorMessage = null
    }

    override fun onCleared() {
        super.onCleared()
        speechService.destroy()
        langIdentifier.close()
    }

    private companion object {
        const val TEXT_WORKFLOW_AI_TIMEOUT_MS = 45_000L
        const val TRANSLATION_TIMEOUT_MS = 10_000L
    }
}
