package com.axoloth.captaggenerator.logic

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.axoloth.captaggenerator.service.security.AuthManager
import com.axoloth.captaggenerator.service.security.TwoFactorStore
import com.axoloth.captaggenerator.service.security.TwoFactorSecurity
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class VerificationStatus { IDLE, SUCCESS, ERROR }

class TwoFactorViewModel(context: Context) : ViewModel() {
    private val appContext = context.applicationContext
    private val security = TwoFactorSecurity()
    
    val rawSecret: String = try {
        security.generateRawSecretKey()
    } catch (e: Exception) {
        "ERROR-GENERATING-KEY"
    }
    
    val displaySecret: String = security.formatSecretKeyForDisplay(rawSecret)
    val otpAuthUrl: String = security.getOtpAuthUrl(
        rawSecret,
        AuthManager(appContext).currentEmail() ?: "user@lapakai.com"
    )
    
    var qrBitmap by mutableStateOf<Bitmap?>(null)
        private set

    var isSaving by mutableStateOf(false)
        private set

    var saveErrorMessage by mutableStateOf<String?>(null)
        private set
        
    var code1 by mutableStateOf("")
    var status1 by mutableStateOf(VerificationStatus.IDLE)
    
    var code2 by mutableStateOf("")
    var status2 by mutableStateOf(VerificationStatus.IDLE)
    
    var code3 by mutableStateOf("")
    var status3 by mutableStateOf(VerificationStatus.IDLE)

    val verifiedCodes = mutableStateListOf<Int>()

    init {
        generateQrCode(otpAuthUrl)
    }

    private fun generateQrCode(content: String) {
        viewModelScope.launch {
            qrBitmap = try {
                withContext(Dispatchers.Default) {
                    createQrBitmap(content)
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun createQrBitmap(content: String): Bitmap {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, QR_BITMAP_SIZE, QR_BITMAP_SIZE)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = IntArray(width * height)

        for (y in 0 until height) {
            for (x in 0 until width) {
                pixels[y * width + x] = if (bitMatrix.get(x, y)) {
                    android.graphics.Color.BLACK
                } else {
                    android.graphics.Color.WHITE
                }
            }
        }

        return Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565).apply {
            setPixels(pixels, 0, width, 0, 0, width, height)
        }
    }

    fun updateCode1(value: String) {
        code1 = value.filter(Char::isDigit).take(6)
        if (status1 == VerificationStatus.ERROR) status1 = VerificationStatus.IDLE
    }

    fun updateCode2(value: String) {
        code2 = value.filter(Char::isDigit).take(6)
        if (status2 == VerificationStatus.ERROR) status2 = VerificationStatus.IDLE
    }

    fun updateCode3(value: String) {
        code3 = value.filter(Char::isDigit).take(6)
        if (status3 == VerificationStatus.ERROR) status3 = VerificationStatus.IDLE
    }

    fun verifyCode1(input: String, onNext: () -> Unit) {
        val codeInt = input.toIntOrNull()
        if (codeInt != null && security.verifySequentialCode(rawSecret, codeInt, verifiedCodes)) {
            status1 = VerificationStatus.SUCCESS
            verifiedCodes.add(codeInt)
            onNext()
        } else {
            status1 = VerificationStatus.ERROR
        }
    }

    fun verifyCode2(input: String, onNext: () -> Unit) {
        val codeInt = input.toIntOrNull()
        if (codeInt != null && security.verifySequentialCode(rawSecret, codeInt, verifiedCodes)) {
            status2 = VerificationStatus.SUCCESS
            verifiedCodes.add(codeInt)
            onNext()
        } else {
            status2 = VerificationStatus.ERROR
        }
    }

    fun verifyCode3(input: String) {
        val codeInt = input.toIntOrNull()
        if (codeInt != null && security.verifySequentialCode(rawSecret, codeInt, verifiedCodes)) {
            status3 = VerificationStatus.SUCCESS
            verifiedCodes.add(codeInt)
        } else {
            status3 = VerificationStatus.ERROR
        }
    }

    fun resetCode1() {
        verifiedCodes.remove(code1.toIntOrNull())
        status1 = VerificationStatus.IDLE
        code1 = ""
    }

    fun resetCode2() {
        verifiedCodes.remove(code2.toIntOrNull())
        status2 = VerificationStatus.IDLE
        code2 = ""
    }

    fun resetCode3() {
        verifiedCodes.remove(code3.toIntOrNull())
        status3 = VerificationStatus.IDLE
        code3 = ""
    }
    
    fun canSave(): Boolean {
        return status1 == VerificationStatus.SUCCESS && 
               status2 == VerificationStatus.SUCCESS && 
               status3 == VerificationStatus.SUCCESS
    }

    fun saveConfiguration(onSuccess: () -> Unit) {
        if (!canSave() || isSaving) return

        isSaving = true
        saveErrorMessage = null
        viewModelScope.launch {
            runCatching {
                TwoFactorStore.saveSecret(appContext, rawSecret)
            }.onSuccess {
                isSaving = false
                onSuccess()
            }.onFailure { error ->
                isSaving = false
                saveErrorMessage = error.message ?: "Gagal menyimpan konfigurasi 2FA."
            }
        }
    }

    private companion object {
        const val QR_BITMAP_SIZE = 512
    }
}

class TwoFactorViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TwoFactorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TwoFactorViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
