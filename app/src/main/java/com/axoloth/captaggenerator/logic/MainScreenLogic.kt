package com.axoloth.captaggenerator.logic

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axoloth.captaggenerator.logic.fragment.MenuToggleLogic
import com.axoloth.captaggenerator.service.ai.OcrService
import android.content.Context
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

sealed class Screen {
    object Main : Screen()
    object Settings : Screen()
    object TwoFactorSetup : Screen()
    data class Generate(val ocrText: String = "") : Screen()
    object GenerateProcessing : Screen()
    data class GenerateResult(val historyId: Int? = null) : Screen()
    data class WebView(val url: String) : Screen()
    object History : Screen()
    object Account : Screen()
}

class MainScreenViewModel : ViewModel() {
    // UI State
    var currentScreen by mutableStateOf<Screen>(Screen.Main)
        private set

    // Image State
    var selectedImageUri by mutableStateOf<Uri?>(null)
        private set

    var isProcessingOcr by mutableStateOf(false)
        private set

    // Snackbar event flow
    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent = _snackbarEvent.asSharedFlow()

    fun navigateTo(screen: Screen) {
        currentScreen = screen
    }

    fun onImageSelected(uri: Uri?) {
        selectedImageUri = uri
    }

    fun clearSelectedImage() {
        selectedImageUri = null
    }

    fun handleMenuItemClick(label: String, closeDrawer: () -> Unit, context: Context) {
        viewModelScope.launch {
            closeDrawer()
            when (label) {
                "Pengaturan Aplikasi" -> navigateTo(Screen.Settings)
                "Pengaturan Akun" -> navigateTo(Screen.Account)
                "Tentang LapakAI" -> MenuToggleLogic.handleTentangLapakAI(this@MainScreenViewModel)
                "Umpan Balik" -> {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                        data = android.net.Uri.parse("https://forms.gle/Tfh2MY1TUS7if8WC7")
                    }
                    context.startActivity(intent)
                }
                "Pusat Bantuan" -> {
                    navigateTo(Screen.WebView("https://axolothdev.blogspot.com/2026/06/cara-penggunaan-lapakai.html"))
                }
                else -> _snackbarEvent.emit("(Dalam Proses Pengembangan)")
            }
        }
    }

    fun startGenerating(context: Context) {
        val uri = selectedImageUri
        if (uri != null) {
            viewModelScope.launch {
                try {
                    isProcessingOcr = true
                    val extractedText = OcrService.extractTextFromImage(context.applicationContext, uri)
                    navigateTo(Screen.Generate(extractedText))
                } finally {
                    isProcessingOcr = false
                    OcrService.close()
                }
            }
        } else {
            navigateTo(Screen.Generate())
        }
    }

    override fun onCleared() {
        super.onCleared()
        OcrService.close()
    }
}
