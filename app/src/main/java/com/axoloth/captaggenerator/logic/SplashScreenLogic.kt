package com.axoloth.captaggenerator.logic

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class LoginViewModel(context: Context) : ViewModel() {
    private val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isPasswordVisible by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    var isLoggedIn by mutableStateOf(sharedPrefs.getBoolean("is_logged_in", false))
        private set

    fun onLoginClick(onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Email dan password tidak boleh kosong"
            return
        }
        
        isLoading = true
        errorMessage = null
        
        // Simulasi Login (Nanti bisa dihubungkan ke Firebase Auth)
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            isLoading = false
            updateLoginStatus(true)
            onSuccess()
        }, 1500)
    }

    fun onGuestLogin(onSuccess: () -> Unit) {
        updateLoginStatus(true)
        onSuccess()
    }

    fun logout() {
        updateLoginStatus(false)
    }

    private fun updateLoginStatus(status: Boolean) {
        isLoggedIn = status
        sharedPrefs.edit().putBoolean("is_logged_in", status).apply()
    }
}
