package com.axoloth.captaggenerator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.axoloth.captaggenerator.logic.SettingScreenViewModel
import com.axoloth.captaggenerator.screen.SettingScreenViewModelFactory
import com.axoloth.captaggenerator.screen.fragment.BiometricDialog
import com.axoloth.captaggenerator.screen.fragment.BiometricDialogStatus
import com.axoloth.captaggenerator.service.security.FingerPrint
import com.axoloth.captaggenerator.screen.MainScreen
import com.axoloth.captaggenerator.ui.theme.CapTagGeneratorTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            
            // Database and Repository initialization
            var isDatabaseReady by remember { mutableStateOf(false) }
            
            LaunchedEffect(Unit) {
                // Background Pre-warm SQLCipher
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    // This is the heavy operation (JNI critical lock)
                    // We call it here specifically to ensure it happens on IO thread
                    try {
                        val safeDb = com.axoloth.captaggenerator.room.AppDatabase.getSafeInstance(context)
                        if (safeDb != null) {
                            isDatabaseReady = true
                        }
                    } catch (ignore: Exception) {
                        // Handle error or fallback
                        isDatabaseReady = true // Continue anyway, DB helper will retry
                    }
                }
            }

            CapTagGeneratorTheme {
                if (!isDatabaseReady) {
                    com.axoloth.captaggenerator.screen.fragment.SplashDatabaseScreen()
                } else {
                    val settingViewModel: SettingScreenViewModel = viewModel(
                        factory = SettingScreenViewModelFactory(context)
                    )
                    
                    var isAuthenticated by remember { mutableStateOf(!settingViewModel.isBiometricEnabled) }
                    var biometricStatus by remember { mutableStateOf(BiometricDialogStatus.IDLE) }
                    var biometricMessage by remember { mutableStateOf<String?>(null) }
                    
                    val activity = context as? FragmentActivity
                    val fingerprintService = remember(activity) { activity?.let { FingerPrint(it) } }

                    if (isAuthenticated) {
                        MainScreen()
                    } else {
                        // Tampilkan Dialog Biometric saat baru buka app
                        LaunchedEffect(Unit) {
                            fingerprintService?.startAuthentication { success, message ->
                                if (success) {
                                    biometricStatus = BiometricDialogStatus.SUCCESS
                                    // Beri delay sedikit biar animasi sukses kelihatan
                                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                        isAuthenticated = true
                                    }, 1000)
                                } else {
                                    biometricStatus = BiometricDialogStatus.ERROR
                                    biometricMessage = message
                                }
                            }
                        }

                        BiometricDialog(
                            onDismiss = { /* Wajib autentikasi, jangan tutup */ },
                            onCancel = { finish() }, // Jika batal, tutup aplikasi
                            status = biometricStatus,
                            message = biometricMessage
                        )
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {

}