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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.axoloth.captaggenerator.logic.LoginViewModel
import com.axoloth.captaggenerator.logic.LoginViewModelFactory
import com.axoloth.captaggenerator.logic.SettingScreenViewModel
import com.axoloth.captaggenerator.room.AppDatabase
import com.axoloth.captaggenerator.screen.SettingScreenViewModelFactory
import com.axoloth.captaggenerator.screen.fragment.BiometricDialog
import com.axoloth.captaggenerator.screen.fragment.BiometricDialogStatus
import com.axoloth.captaggenerator.screen.fragment.DatabaseErrorScreen
import com.axoloth.captaggenerator.screen.fragment.SplashDatabaseScreen
import com.axoloth.captaggenerator.screen.fragment.TwoFactorChallengeScreen
import com.axoloth.captaggenerator.service.security.FingerPrint
import com.axoloth.captaggenerator.service.security.TwoFactorStore
import com.axoloth.captaggenerator.screen.MainScreen
import com.axoloth.captaggenerator.ui.theme.CapTagGeneratorTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private sealed interface DatabaseStartupState {
    data object Loading : DatabaseStartupState
    data class Ready(val database: AppDatabase) : DatabaseStartupState
    data class Error(val message: String) : DatabaseStartupState
}

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val mainViewModel: com.axoloth.captaggenerator.logic.MainScreenViewModel = viewModel()

            // Handle App Shortcut intents
            LaunchedEffect(intent) {
                intent?.getStringExtra("shortcut_action")?.let { action ->
                    when (action) {
                        "action_generate" -> mainViewModel.navigateTo(com.axoloth.captaggenerator.logic.Screen.Generate())
                        "action_history" -> mainViewModel.navigateTo(com.axoloth.captaggenerator.logic.Screen.History)
                    }
                }
            }
            
            var databaseState by remember {
                mutableStateOf<DatabaseStartupState>(DatabaseStartupState.Loading)
            }
            var databaseRetryAttempt by remember { mutableStateOf(0) }

            LaunchedEffect(databaseRetryAttempt) {
                databaseState = DatabaseStartupState.Loading
                databaseState = try {
                    val database = AppDatabase.getSafeInstance(context.applicationContext)
                    DatabaseStartupState.Ready(database)
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (error: Exception) {
                    DatabaseStartupState.Error(
                        error.message ?: "Terjadi kesalahan saat membuka database."
                    )
                }
            }

            CapTagGeneratorTheme {
                when (val state = databaseState) {
                    DatabaseStartupState.Loading -> SplashDatabaseScreen()
                    is DatabaseStartupState.Error -> DatabaseErrorScreen(
                        message = state.message,
                        onRetry = { databaseRetryAttempt++ },
                        onExit = { finish() }
                    )
                    is DatabaseStartupState.Ready -> {
                        val settingViewModel: SettingScreenViewModel = viewModel(
                            factory = SettingScreenViewModelFactory(context)
                        )

                        val loginViewModel: LoginViewModel = viewModel(
                            factory = LoginViewModelFactory(context)
                        )

                        var isLoggedIn by remember { mutableStateOf(loginViewModel.isLoggedIn) }
                        var isTwoFactorVerified by remember {
                            mutableStateOf(!settingViewModel.isTwoFactorEnabled)
                        }
                        var isAuthenticated by remember { mutableStateOf(!settingViewModel.isBiometricEnabled) }
                        var biometricStatus by remember { mutableStateOf(BiometricDialogStatus.IDLE) }
                        var biometricMessage by remember { mutableStateOf<String?>(null) }
                        var biometricPromptAttempt by remember { mutableStateOf(0) }
                        var twoFactorCode by remember { mutableStateOf("") }
                        var twoFactorErrorMessage by remember { mutableStateOf<String?>(null) }
                        var isTwoFactorLoading by remember { mutableStateOf(false) }

                        val activity = context as? FragmentActivity
                        val fingerprintService = remember(activity) { activity?.let { FingerPrint(it) } }
                        val scope = rememberCoroutineScope()

                        LaunchedEffect(isLoggedIn, settingViewModel.isTwoFactorEnabled) {
                            if (!isLoggedIn) {
                                isTwoFactorVerified = false
                                twoFactorCode = ""
                                twoFactorErrorMessage = null
                            } else if (!settingViewModel.isTwoFactorEnabled) {
                                isTwoFactorVerified = true
                            }
                        }

                        if (!isLoggedIn) {
                            com.axoloth.captaggenerator.screen.LoginScreen(
                                viewModel = loginViewModel,
                                onLoginSuccess = {
                                    isLoggedIn = true
                                    isTwoFactorVerified = !settingViewModel.isTwoFactorEnabled
                                    isAuthenticated = !settingViewModel.isBiometricEnabled
                                }
                            )
                        } else if (settingViewModel.isTwoFactorEnabled && !isTwoFactorVerified) {
                            TwoFactorChallengeScreen(
                                code = twoFactorCode,
                                errorMessage = twoFactorErrorMessage,
                                isLoading = isTwoFactorLoading,
                                onCodeChange = {
                                    twoFactorCode = it
                                    twoFactorErrorMessage = null
                                },
                                onVerifyClick = {
                                    if (!isTwoFactorLoading) {
                                        scope.launch {
                                            isTwoFactorLoading = true
                                            twoFactorErrorMessage = null
                                            val isValid = TwoFactorStore.verifyCode(
                                                context.applicationContext,
                                                twoFactorCode
                                            )
                                            isTwoFactorLoading = false

                                            if (isValid) {
                                                isTwoFactorVerified = true
                                                twoFactorCode = ""
                                            } else {
                                                twoFactorErrorMessage = "Kode 2FA tidak valid atau sudah kedaluwarsa."
                                            }
                                        }
                                    }
                                },
                                onLogoutClick = {
                                    loginViewModel.logout()
                                    isLoggedIn = false
                                    isTwoFactorVerified = false
                                    twoFactorCode = ""
                                },
                                onExitClick = { finish() }
                            )
                        } else if (isAuthenticated) {
                            MainScreen(
                                database = state.database,
                                viewModel = mainViewModel
                            )
                        } else {
                            // Tampilkan Dialog Biometric saat baru buka app
                            LaunchedEffect(biometricPromptAttempt) {
                                biometricStatus = BiometricDialogStatus.IDLE
                                biometricMessage = null

                                val service = fingerprintService
                                if (service == null) {
                                    biometricStatus = BiometricDialogStatus.ERROR
                                    biometricMessage = "Autentikasi perangkat tidak siap."
                                    return@LaunchedEffect
                                }

                                service.startAuthentication { success, message ->
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
                                onRetry = { biometricPromptAttempt++ },
                                status = biometricStatus,
                                message = biometricMessage
                            )
                        }
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
