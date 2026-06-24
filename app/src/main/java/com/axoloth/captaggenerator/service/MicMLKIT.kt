package com.axoloth.captaggenerator.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

class MicMLKIT(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    
    // Callbacks to send results to ViewModel
    var onResult: (String) -> Unit = {}
    var onError: (String) -> Unit = {}
    var onPartialResult: (String) -> Unit = {}
    var onRmsChanged: (Float) -> Unit = {}
    var onListeningStarted: () -> Unit = {}
    var onListeningStopped: () -> Unit = {}

    /**
     * Lazy Init: SpeechRecognizer hanya dibuat saat user mulai merekam.
     */
    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("Speech recognition tidak tersedia di perangkat ini.")
            return
        }

        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            setupListener()
        }

        val intent = createRecognizerIntent(context.packageName)

        if (isListening) {
            speechRecognizer?.cancel()
            isListening = false
        }
        speechRecognizer?.startListening(intent)
        isListening = true
        onListeningStarted()
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        isListening = false
        onListeningStopped()
    }

    fun cancelListening() {
        speechRecognizer?.cancel()
        isListening = false
        onListeningStopped()
    }

    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        isListening = false
    }

    private fun setupListener() {
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
                onListeningStarted()
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {
                onRmsChanged(rmsdB)
            }
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                isListening = false
                onListeningStopped()
            }
            
            override fun onError(error: Int) {
                isListening = false
                onListeningStopped()
                val message = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Speech recognizer client error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permission missing"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer busy"
                    SpeechRecognizer.ERROR_SERVER -> "Speech recognition server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Tidak ada suara terdeteksi"
                    else -> "Speech recognition error: $error"
                }
                onError(message)
            }

            override fun onResults(results: Bundle?) {
                isListening = false
                onListeningStopped()
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    onResult(matches[0])
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    onPartialResult(matches[0])
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    companion object {
        internal fun createRecognizerIntent(packageName: String): Intent {
            return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.forLanguageTag("id-ID").toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, Locale.forLanguageTag("id-ID").toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1000L)
            }
        }
    }
}
