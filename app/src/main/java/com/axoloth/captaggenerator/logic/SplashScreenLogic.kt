package com.axoloth.captaggenerator.logic

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axoloth.captaggenerator.service.security.AuthManager
import kotlinx.coroutines.launch

class LoginViewModel(context: Context) : ViewModel() {
    private val authManager = AuthManager(context.applicationContext)

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isPasswordVisible by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var infoMessage by mutableStateOf<String?>(null)
        private set

    var isCredentialConfigured by mutableStateOf(authManager.hasCredentials())
        private set

    var isLoggedIn by mutableStateOf(authManager.isSessionValid())
        private set

    fun onLoginClick(onSuccess: () -> Unit) {
        if (isLoading) return

        isLoading = true
        errorMessage = null
        infoMessage = null

        viewModelScope.launch {
            val result = authManager.authenticateOrCreate(email, password)
            isLoading = false

            if (!result.isSuccess) {
                errorMessage = result.message ?: "Login gagal."
                return@launch
            }

            isCredentialConfigured = true
            infoMessage = result.message
            isLoggedIn = true
            password = ""
            onSuccess()
        }
    }

    fun logout() {
        authManager.clearSession()
        isLoggedIn = false
    }

    fun refreshSession() {
        isCredentialConfigured = authManager.hasCredentials()
        isLoggedIn = authManager.isSessionValid()
        if (!isCredentialConfigured) {
            infoMessage = "Buat akun lokal pertama untuk mengunci aplikasi ini."
        }
    }
}
