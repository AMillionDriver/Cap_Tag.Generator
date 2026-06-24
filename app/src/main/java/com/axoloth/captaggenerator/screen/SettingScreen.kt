package com.axoloth.captaggenerator.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.axoloth.captaggenerator.logic.MainScreenViewModel
import com.axoloth.captaggenerator.logic.Screen
import com.axoloth.captaggenerator.logic.SettingScreenViewModel
import com.axoloth.captaggenerator.room.UserDao
import com.axoloth.captaggenerator.screen.fragment.BiometricDialog
import com.axoloth.captaggenerator.screen.fragment.BiometricDialogStatus
import com.axoloth.captaggenerator.screen.fragment.DatabaseTrendLogPopup
import com.axoloth.captaggenerator.screen.fragment.KategoriUMKMSheet
import com.axoloth.captaggenerator.screen.fragment.StoragePopUp
import com.axoloth.captaggenerator.screen.fragment.ToneOfVoicePopup
import com.axoloth.captaggenerator.service.security.FingerPrint
import com.axoloth.captaggenerator.ui.theme.CapTagGeneratorTheme

// Colors used in the settings screen to match the screenshot
private val SettingsCardBg = Color(0xFF1C1C1E)
private val SettingsIconBg = Color(0xFF2A2929)
private val SettingsAccentPurple = Color(0xFF8400FF)
private val SettingsSecondaryText = Color(0xFF8E8E93)
private val SettingsHeaderGray = Color(0xFF8E8E93)
private val SettingsDeleteRed = Color(0xFFFF453A)

class SettingScreenViewModelFactory(private val context: android.content.Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingScreenViewModel(context) as T
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    onBackClick: () -> Unit = {},
    mainViewModel: MainScreenViewModel = viewModel(),
    settingViewModel: SettingScreenViewModel = viewModel(factory = SettingScreenViewModelFactory(LocalContext.current)),
    userDao: UserDao? = null
) {
    CapTagGeneratorTheme(darkTheme = true) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Pengaturan", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            },
            containerColor = Color.Black
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Section: Profil Bisnis
                SettingsSectionHeader("PROFIL BISNIS")
                SettingsGroup {
                    if (settingViewModel.isEditingBusinessName) {
                        var tempName by remember { mutableStateOf(settingViewModel.businessName) }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(SettingsIconBg, RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Store, null, tint = SettingsAccentPurple)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("Nama Usaha", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedTextField(
                                value = tempName,
                                onValueChange = { tempName = it },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SettingsAccentPurple,
                                    unfocusedBorderColor = Color.Gray,
                                    cursorColor = SettingsAccentPurple
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { settingViewModel.isEditingBusinessName = false },
                                    border = BorderStroke(1.dp, Color.Gray),
                                    shape = RoundedCornerShape(20.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("Batal", color = Color.White, fontSize = 14.sp)
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Button(
                                    onClick = { settingViewModel.updateBusinessName(tempName) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SettingsAccentPurple),
                                    shape = RoundedCornerShape(20.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("Simpan", color = Color.White, fontSize = 14.sp)
                                }
                            }
                        }
                    } else {
                        SettingsItem(
                            icon = Icons.Default.Store,
                            title = "Nama Usaha",
                            subtitle = settingViewModel.businessName,
                            trailing = {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = SettingsAccentPurple, modifier = Modifier.size(20.dp))
                            },
                            onClick = { settingViewModel.isEditingBusinessName = true }
                        )
                    }
                    SettingsDivider()
                    var showCategorySheet by remember { mutableStateOf(false) }
                    
                    if (showCategorySheet) {
                        KategoriUMKMSheet(
                            onDismiss = { showCategorySheet = false },
                            onCategorySelected = { category ->
                                settingViewModel.updateUmkmCategory(category)
                            }
                        )
                    }

                    SettingsItem(
                        icon = Icons.Default.GridView,
                        title = "Kategori UMKM",
                        subtitle = settingViewModel.umkmCategory,
                        onClick = { showCategorySheet = true }
                    )
                    SettingsDivider()
                    if (settingViewModel.isEditingSalesLink) {
                        var tempLink by remember { mutableStateOf(settingViewModel.salesLink) }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(SettingsIconBg, RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Link, null, tint = SettingsAccentPurple)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("Default Link Jualan", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedTextField(
                                value = tempLink,
                                onValueChange = { tempLink = it },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SettingsAccentPurple,
                                    unfocusedBorderColor = Color.Gray,
                                    cursorColor = SettingsAccentPurple
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { settingViewModel.isEditingSalesLink = false },
                                    border = BorderStroke(1.dp, Color.Gray),
                                    shape = RoundedCornerShape(20.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("Batal", color = Color.White, fontSize = 14.sp)
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Button(
                                    onClick = { settingViewModel.updateSalesLink(tempLink) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SettingsAccentPurple),
                                    shape = RoundedCornerShape(20.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("Simpan", color = Color.White, fontSize = 14.sp)
                                }
                            }
                        }
                    } else {
                        SettingsItem(
                            icon = Icons.Default.Link,
                            title = "Default Link Jualan",
                            subtitle = settingViewModel.salesLink,
                            trailing = {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = SettingsAccentPurple, modifier = Modifier.size(20.dp))
                            },
                            onClick = { settingViewModel.isEditingSalesLink = true }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section: Keamanan
                SettingsSectionHeader("KEAMANAN")
                SettingsGroup {
                    SettingsItem(
                        icon = Icons.Default.Shield,
                        title = "Two Factor Authentication",
                        subtitle = "Gunakan Google Authenticator\nuntuk akses lebih baik dan fleksibel",
                        trailing = {
                            Switch(
                                checked = settingViewModel.isTwoFactorEnabled,
                                onCheckedChange = { enabled ->
                                    settingViewModel.onTwoFactorToggle(enabled) {
                                        mainViewModel.navigateTo(Screen.TwoFactorSetup)
                                    }
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SettingsAccentPurple)
                            )
                        }
                    )
                    SettingsDivider()
                    var showBiometricDialog by remember { mutableStateOf(false) }
                    var biometricStatus by remember { mutableStateOf(BiometricDialogStatus.IDLE) }
                    var biometricMessage by remember { mutableStateOf<String?>(null) }
                    var biometricPromptAttempt by remember { mutableStateOf(0) }
                    
                    val context = LocalContext.current
                    val activity = context as? FragmentActivity
                    val fingerprintService = remember(activity) { activity?.let { FingerPrint(it) } }
                    val scope = rememberCoroutineScope()

                    if (showBiometricDialog) {
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
                                scope.launch {
                                    if (success) {
                                        biometricStatus = BiometricDialogStatus.SUCCESS
                                        settingViewModel.updateBiometricStatus(true)
                                        delay(1000)
                                        showBiometricDialog = false
                                    } else {
                                        biometricStatus = BiometricDialogStatus.ERROR
                                        biometricMessage = message
                                    }
                                }
                            }
                        }
                        
                        BiometricDialog(
                            onDismiss = { 
                                showBiometricDialog = false
                                biometricStatus = BiometricDialogStatus.IDLE
                                biometricMessage = null
                            },
                            onCancel = { 
                                showBiometricDialog = false
                                biometricStatus = BiometricDialogStatus.IDLE
                                biometricMessage = null
                            },
                            onRetry = { biometricPromptAttempt++ },
                            status = biometricStatus,
                            message = biometricMessage
                        )
                    }

                    SettingsItem(
                        icon = Icons.Default.Fingerprint,
                        title = "Biometric Lock",
                        subtitle = "Gunakan Sidik Jari/Wajah untuk masuk",
                        trailing = {
                            Switch(
                                checked = settingViewModel.isBiometricEnabled,
                                onCheckedChange = { 
                                    if (it) {
                                        showBiometricDialog = true
                                        biometricStatus = BiometricDialogStatus.IDLE
                                        biometricMessage = null
                                    } else {
                                        settingViewModel.updateBiometricStatus(false)
                                    }
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SettingsAccentPurple)
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section: Sistem AI
                SettingsSectionHeader("SISTEM AI")
                SettingsGroup {
                    var showTonePopup by remember { mutableStateOf(false) }
                    var showTrendLogPopup by remember { mutableStateOf(false) }

                    if (showTonePopup) {
                        ToneOfVoicePopup(
                            viewModel = settingViewModel,
                            onDismiss = { showTonePopup = false }
                        )
                    }

                    if (showTrendLogPopup) {
                        DatabaseTrendLogPopup(
                            onDismiss = { showTrendLogPopup = false }
                        )
                    }

                    SettingsItem(
                        icon = Icons.Default.VolumeUp,
                        title = "Tone of Voice",
                        subtitle = settingViewModel.selectedTone,
                        trailing = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = SettingsSecondaryText) },
                        onClick = { showTonePopup = true }
                    )
                    SettingsDivider()
                    SettingsItem(
                        icon = Icons.Default.Storage,
                        title = "Database Tren",
                        subtitle = "Versi v2.1.0\nTerakhir Update: 14 Mei 2026",
                        trailing = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = SettingsSecondaryText) },
                        onClick = { showTrendLogPopup = true }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section: Penyimpanan
                SettingsSectionHeader("PENYIMPANAN")
                SettingsGroup {
                    var showStoragePopup by remember { mutableStateOf(false) }

                    if (showStoragePopup) {
                        StoragePopUp(
                            onDismiss = { showStoragePopup = false },
                            userDao = userDao
                        )
                    }

                    SettingsItem(
                        icon = Icons.Default.Delete,
                        title = "Bersihkan Cache & Riwayat",
                        titleColor = SettingsDeleteRed,
                        iconColor = SettingsDeleteRed,
                        onClick = { showStoragePopup = true }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section: Dukungan
                SettingsSectionHeader("DUKUNGAN")
                SettingsGroup {
                    SettingsItem(
                        icon = Icons.Default.MenuBook,
                        title = "Panduan Foto Produk",
                        trailing = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = SettingsSecondaryText) }
                    )
                    SettingsDivider()
                    SettingsItem(
                        icon = Icons.Default.Hub,
                        title = "Hubungi Developer",
                        subtitle = "lapakai.support@example.com",
                        trailing = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = SettingsSecondaryText) }
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        color = SettingsHeaderGray,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = SettingsCardBg,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(content = content)
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    titleColor: Color = Color.White,
    iconColor: Color = SettingsAccentPurple,
    trailing: @Composable (() -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(SettingsIconBg, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = titleColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = SettingsSecondaryText,
                    fontSize = 13.sp
                )
            }
        }
        if (trailing != null) {
            trailing()
        }
    }
}

@Composable
fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 72.dp),
        thickness = 0.5.dp,
        color = Color(0xFF3A3A3C)
    )
}

@Preview(showBackground = true)
@Composable
fun SettingScreenPreview() {
    SettingScreen()
}
