package com.axoloth.captaggenerator.logic

import android.graphics.Bitmap
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axoloth.captaggenerator.service.security.TwoFactorSecurity
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.launch

enum class VerificationStatus { IDLE, SUCCESS, ERROR }

class TwoFactorViewModel : ViewModel() {
    private val security = TwoFactorSecurity()
    
    val rawSecret: String = try {
        security.generateRawSecretKey()
    } catch (e: Exception) {
        "ERROR-GENERATING-KEY"
    }
    
    val displaySecret: String = security.formatSecretKeyForDisplay(rawSecret)
    val otpAuthUrl: String = security.getOtpAuthUrl(rawSecret, "user@lapakai.com")
    
    var qrBitmap by mutableStateOf<Bitmap?>(null)
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
                val writer = QRCodeWriter()
                val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
                val width = bitMatrix.width
                val height = bitMatrix.height
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                for (x in 0 until width) {
                    for (y in 0 until height) {
                        bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                    }
                }
                bitmap
            } catch (e: Exception) {
                null
            }
        }
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
}
