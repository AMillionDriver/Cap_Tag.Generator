package com.axoloth.captaggenerator.logic.fragment

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class RecordingState {
    IDLE, RECORDING, LOCKED, CANCELLED, SENT
}

class MicViewModel : ViewModel() {
    var recordingState by mutableStateOf(RecordingState.IDLE)
    var recordingDuration by mutableStateOf(0L) // in milliseconds
    var isLocked by mutableStateOf(false)
    
    private var timerJob: Job? = null

    fun startRecording() {
        recordingState = RecordingState.RECORDING
        recordingDuration = 0L
        startTimer()
    }

    fun lockRecording() {
        isLocked = true
        recordingState = RecordingState.LOCKED
    }

    fun stopRecording(isSent: Boolean) {
        stopTimer()
        recordingState = if (isSent) RecordingState.SENT else RecordingState.CANCELLED
        // Reset after a short delay or based on UI action
        viewModelScope.launch {
            delay(1000)
            reset()
        }
    }

    fun cancelRecording() {
        stopTimer()
        recordingState = RecordingState.CANCELLED
        viewModelScope.launch {
            delay(500)
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
}
