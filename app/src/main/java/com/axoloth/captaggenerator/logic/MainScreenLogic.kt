package com.axoloth.captaggenerator.logic

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

sealed class Screen {
    object Main : Screen()
    object Settings : Screen()
    object TwoFactorSetup : Screen()
    object Generate : Screen()
}

class MainScreenViewModel : ViewModel() {
    // UI State
    var currentScreen by mutableStateOf<Screen>(Screen.Main)
        private set

    // Image State
    var selectedImageUri by mutableStateOf<Uri?>(null)
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

    fun handleMenuItemClick(label: String, closeDrawer: () -> Unit) {
        viewModelScope.launch {
            closeDrawer()
            if (label == "Pengaturan Aplikasi") {
                navigateTo(Screen.Settings)
            } else {
                _snackbarEvent.emit("(Dalam Proses Pengembangan)")
            }
        }
    }

    fun startGenerating() {
        navigateTo(Screen.Generate)
    }
}

