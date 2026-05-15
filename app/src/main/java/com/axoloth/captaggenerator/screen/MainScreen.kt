package com.axoloth.captaggenerator.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import androidx.lifecycle.viewmodel.compose.viewModel
import com.axoloth.captaggenerator.logic.MainScreenViewModel
import com.axoloth.captaggenerator.logic.Screen
import com.axoloth.captaggenerator.logic.SettingScreenViewModel
import com.axoloth.captaggenerator.screen.fragment.SideMenuContent
import com.axoloth.captaggenerator.ui.theme.CapTagGeneratorTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

import com.axoloth.captaggenerator.screen.fragment.TwoFactorScreen
import com.axoloth.captaggenerator.screen.GenerateScreen

val ColorIconCyan = Color(0xCCA6A2A6)
val ColorIconDf = Color(0xCC6400FF)
val ColorCardBackground = Color(0xFF161B22)
val ColorCyanPrimary = Color(0xFF3430DC)
val ColorCyanSecondary = Color(0xFF8800FF)
val ColorTextSecondary = Color.Gray

@Composable
fun MainScreen(viewModel: MainScreenViewModel = viewModel()) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var lastBackPressTime by remember { mutableLongStateOf(0L) }
    
    // Inisialisasi SettingViewModel di level MainScreen agar state-nya tersinkronisasi
    val settingViewModel: SettingScreenViewModel = viewModel(
        factory = SettingScreenViewModelFactory(LocalContext.current)
    )

    // Handle Snackbar Events
    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    CapTagGeneratorTheme(darkTheme = true) {
        when (viewModel.currentScreen) {
            is Screen.Settings -> {
                BackHandler { viewModel.navigateTo(Screen.Main) }
                SettingScreen(
                    onBackClick = { viewModel.navigateTo(Screen.Main) },
                    mainViewModel = viewModel,
                    settingViewModel = settingViewModel
                )
            }
            is Screen.TwoFactorSetup -> {
                BackHandler { viewModel.navigateTo(Screen.Settings) }
                TwoFactorScreen(
                    onBackClick = { viewModel.navigateTo(Screen.Settings) },
                    onSaveSuccess = { 
                        settingViewModel.updateTwoFactorStatus(true)
                        viewModel.navigateTo(Screen.Settings) 
                    }
                )
            }
            is Screen.Generate -> {
                BackHandler { viewModel.navigateTo(Screen.Main) }
                GenerateScreen(
                    selectedImageUri = viewModel.selectedImageUri,
                    onBackClick = { viewModel.navigateTo(Screen.Main) }
                )
            }
            is Screen.Main -> {
                BackHandler {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastBackPressTime < 2000) {
                        (context as? Activity)?.finish()
                    } else {
                        lastBackPressTime = currentTime
                        Toast.makeText(context, "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT).show()
                    }
                }
                MainDashboard(
                    viewModel = viewModel,
                    drawerState = drawerState,
                    snackbarHostState = snackbarHostState,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onMenuItemClick = { label ->
                        viewModel.handleMenuItemClick(label) {
                            scope.launch { drawerState.close() }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun MainDashboard(
    viewModel: MainScreenViewModel,
    drawerState: DrawerState,
    snackbarHostState: SnackbarHostState,
    onMenuClick: () -> Unit,
    onMenuItemClick: (String) -> Unit
) {
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.onImageSelected(uri)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SideMenuContent(
                onClose = { onMenuClick() },
                onItemClick = onMenuItemClick
            )
        },
        gesturesEnabled = true
    ) {
        Scaffold(
            topBar = { LapakHeader(onMenuClick = onMenuClick) },
            bottomBar = { 
                CustomBottomNav(
                    isImageSelected = viewModel.selectedImageUri != null,
                    onFabClick = {
                        if (viewModel.selectedImageUri != null) {
                            viewModel.startGenerating()
                        } else {
                            galleryLauncher.launch("image/*")
                        }
                    }
                ) 
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(16.dp)) }
                item { 
                    AnalysisSection(
                        selectedImageUri = viewModel.selectedImageUri,
                        onUploadClick = { galleryLauncher.launch("image/*") },
                        onClearClick = { viewModel.clearSelectedImage() }
                    ) 
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
                item {
                    Text(
                        text = "Aktivitas Terbaru",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
                items(recentActivities) { activity ->
                    ActivityItem(activity)
                }
            }
        }
    }
}

@Composable
fun LapakHeader(onMenuClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 0.dp)
            .statusBarsPadding(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onMenuClick) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = ColorIconCyan,
                modifier = Modifier.size(32.dp)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.SupportAgent,
                contentDescription = "Contact CS",
                tint = ColorIconCyan,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "Hubungi CS",
                fontSize = 10.sp,
                color = Color.White
            )
        }
    }

    // Logo "LapakAI"
    Box(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 75.dp)
    ) {
        Row {
            Text(
                text = "Lapak",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "AI",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = ColorIconDf
            )
        }
    }
}

@Composable
fun AnalysisSection(
    selectedImageUri: android.net.Uri?,
    onUploadClick: () -> Unit,
    onClearClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ColorCardBackground)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Analisis & Generasi Konten",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(ColorIconCyan, Color.Transparent, ColorIconCyan)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Transparent)
                    .clickable { if (selectedImageUri == null) onUploadClick() },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(selectedImageUri),
                        contentDescription = "Selected Product Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = ColorIconCyan,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "+",
                                fontSize = 32.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                tint = ColorIconCyan,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Unggah Foto Produk",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "AI Menganalisis Foto +\nData Tren Terkini!",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = ColorTextSecondary
                        )
                    }
                }
            }

            if (selectedImageUri != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = onClearClick) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hapus", color = Color.Red)
                    }
                    TextButton(onClick = onUploadClick) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = ColorIconCyan)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ganti Foto", color = ColorIconCyan)
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityItem(activity: ActivityData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(ColorCardBackground, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFF1E293B), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = activity.icon,
                contentDescription = null,
                tint = ColorIconCyan,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = activity.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = activity.time,
                style = MaterialTheme.typography.labelSmall,
                color = ColorTextSecondary
            )
        }
    }
}

@Composable
fun CustomBottomNav(
    isImageSelected: Boolean = false,
    onFabClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Background shape for bottom nav
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            color = ColorCardBackground,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.Home,
                        contentDescription = null,
                        tint = ColorIconCyan
                    )
                    Text("Home", fontSize = 10.sp, color = ColorIconCyan)
                }

                Spacer(modifier = Modifier.width(48.dp)) // Space for FAB

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.History,
                        contentDescription = null,
                        tint = ColorTextSecondary
                    )
                    Text(
                        "Riwayat\nPenggunaan",
                        fontSize = 10.sp,
                        color = ColorTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Floating Action Button
        Column(
            modifier = Modifier
                .offset(y = (-30).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(
                                ColorCyanPrimary,
                                ColorCyanSecondary
                            )
                        ),
                        shape = CircleShape
                    )
                    .clickable { onFabClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isImageSelected) Icons.Default.AutoAwesome else Icons.Default.Add,
                    contentDescription = "Action",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Text(
                text = if (isImageSelected) "Start Generate" else "Mulai Buat/Generate",
                fontSize = 10.sp,
                color = Color.White
            )
        }
    }
}

data class ActivityData(
    val title: String,
    val time: String,
    val icon: ImageVector
)

val recentActivities = listOf(
    ActivityData("Caption Keripik Pedas #Gacor", "7 minutes ago", Icons.Default.Image),
    ActivityData("Promo Sambal #Viral", "3 minutes ago", Icons.Default.Percent),
    ActivityData("Ide Konten Sepatu", "2 minutes ago", Icons.Default.Lightbulb)
)

@Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
fun MainScreenPreview() {
    CapTagGeneratorTheme(darkTheme = true) {
        MainScreen()
    }
}
