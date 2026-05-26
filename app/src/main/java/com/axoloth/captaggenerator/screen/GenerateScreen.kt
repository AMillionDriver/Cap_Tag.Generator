package com.axoloth.captaggenerator.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.axoloth.captaggenerator.logic.GenerateScreenViewModel
import com.axoloth.captaggenerator.logic.GenerateResultViewModel
import com.axoloth.captaggenerator.logic.Screen
import com.axoloth.captaggenerator.logic.MainScreenViewModel
import com.axoloth.captaggenerator.logic.SettingScreenViewModel
import com.axoloth.captaggenerator.logic.fragment.MicViewModel
import com.axoloth.captaggenerator.logic.fragment.RecordingState
import com.axoloth.captaggenerator.screen.fragment.MicPopUp
import com.axoloth.captaggenerator.ui.theme.CapTagGeneratorTheme
import java.text.SimpleDateFormat
import java.util.*

private val GenerateCardBg = Color(0xFF161B22)
private val GenerateAccentPurple = Color(0xFF8A2BE2)
private val GenerateSecondaryText = Color(0xFF8E8E93)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateScreen(
    selectedImageUri: Uri?,
    onBackClick: () -> Unit,
    ocrText: String = "",
    viewModel: GenerateScreenViewModel = viewModel(),
    mainViewModel: MainScreenViewModel = viewModel(),
    resultViewModel: GenerateResultViewModel = viewModel(),
    micViewModel: MicViewModel = viewModel(),
    settingViewModel: SettingScreenViewModel = viewModel(factory = SettingScreenViewModelFactory(LocalContext.current)),
) {
    // Sync business info from settings as default if not already set
    LaunchedEffect(Unit) {
        if (viewModel.businessName.isEmpty()) {
            viewModel.businessName = settingViewModel.businessName
        }
        if (viewModel.salesLink.isEmpty()) {
            viewModel.salesLink = settingViewModel.salesLink
        }
    }

    // Reset inputs when the selected image changes or is removed
    LaunchedEffect(selectedImageUri) {
        viewModel.resetInputs()
        // Re-sync after reset
        viewModel.businessName = settingViewModel.businessName
        viewModel.salesLink = settingViewModel.salesLink
    }

    // Inisialisasi awal jika ada teks dari OCR
    androidx.compose.runtime.LaunchedEffect(ocrText) {
        if (ocrText.isNotBlank()) {
            // Jika ada OCR baru, masukkan ke productModel
            viewModel.productModel = ocrText
        }
    }

    GenerateScreenContent(
        selectedImageUri = selectedImageUri,
        onImageSelected = { mainViewModel.onImageSelected(it) },
        onBackClick = onBackClick,
        productModel = viewModel.productModel,
        onProductModelChange = { viewModel.productModel = it },
        productPurpose = viewModel.productPurpose,
        onProductPurposeChange = { viewModel.productPurpose = it },
        keywordsInput = viewModel.keywordsInput,
        onKeywordsInputChange = {
            viewModel.keywordsInput = it
            if (it.endsWith(",")) {
                viewModel.addKeyword()
            }
        },
        keywords = viewModel.keywords,
        onRemoveKeyword = { viewModel.removeKeyword(it) },
        selectedTone = viewModel.selectedTone,
        onSelectedToneChange = { viewModel.selectedTone = it },
        tones = viewModel.tones,
        businessName = viewModel.businessName,
        onBusinessNameChange = { viewModel.businessName = it },
        salesLink = viewModel.salesLink,
        onSalesLinkChange = { viewModel.salesLink = it },
        onStartProcessing = {
            resultViewModel.startProcessing(
                productName = viewModel.productModel,
                productModel = viewModel.productModel,
                productPurpose = viewModel.productPurpose,
                userKeywords = viewModel.keywords.toList(),
                tone = viewModel.selectedTone,
                businessName = viewModel.businessName,
                salesLink = viewModel.salesLink
            )
            // Clear inputs after starting process so they are fresh for next time
            viewModel.resetInputs()
            mainViewModel.navigateTo(Screen.GenerateProcessing)
        },
        micViewModel = micViewModel
    )
}



@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GenerateScreenContent(
    selectedImageUri: Uri?,
    onImageSelected: (Uri?) -> Unit = {},
    onBackClick: () -> Unit,
    productModel: String,
    onProductModelChange: (String) -> Unit,
    productPurpose: String,
    onProductPurposeChange: (String) -> Unit,
    keywordsInput: String,
    onKeywordsInputChange: (String) -> Unit,
    keywords: List<String>,
    onRemoveKeyword: (String) -> Unit,
    selectedTone: String,
    onSelectedToneChange: (String) -> Unit,
    tones: List<String>,
    businessName: String,
    onBusinessNameChange: (String) -> Unit,
    salesLink: String,
    onSalesLinkChange: (String) -> Unit,
    onStartProcessing: () -> Unit,
    micViewModel: MicViewModel = viewModel()
) {
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        onImageSelected(uri)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CapTagGeneratorTheme(darkTheme = true) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                "Generate Caption",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = onBackClick,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .background(Color.White, CircleShape)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.Black
                                )
                            }
                        },
                        actions = {
                            Spacer(modifier = Modifier.width(48.dp)) // To balance the center title
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Black,
                            titleContentColor = Color.White
                        )
                    )
                },
                containerColor = Color.Black
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(Color.Black)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Image Preview Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .border(
                                width = 2.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(GenerateAccentPurple, Color.Transparent, GenerateAccentPurple)
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .clip(RoundedCornerShape(24.dp))
                            .background(GenerateCardBg)
                            .clickable { if (selectedImageUri == null) galleryLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(selectedImageUri),
                                contentDescription = "Selected Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Add Image",
                                    tint = GenerateAccentPurple,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Tambahkan Foto Produk",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Klik untuk memilih gambar",
                                    color = GenerateSecondaryText,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Floating Corners
                        CornerMarkers()

                        // Bottom info bar in the image card
                        if (selectedImageUri != null) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                Text(
                                    text = "Uploaded: $date",
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable { onImageSelected(null) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Input Section: Product Model
                    OutlinedTextField(
                        value = productModel,
                        onValueChange = onProductModelChange,
                        label = { Text("Deskripsikan tentang model product anda") },
                        placeholder = { Text("Misal:") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GenerateAccentPurple,
                            unfocusedBorderColor = GenerateSecondaryText,
                            focusedLabelColor = GenerateAccentPurple,
                            cursorColor = GenerateAccentPurple
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Text(
                        text = "Misal: Sepatu lari premium, Headphones noise-cancelling...",
                        color = GenerateSecondaryText,
                        fontSize = 10.sp,
                        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Input Section: Product Purpose
                    OutlinedTextField(
                        value = productPurpose,
                        onValueChange = onProductPurposeChange,
                        label = { Text("Deskripsikan tujuan penggunaan product anda") },
                        placeholder = { Text("Misal:") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GenerateAccentPurple,
                            unfocusedBorderColor = GenerateSecondaryText,
                            focusedLabelColor = GenerateAccentPurple,
                            cursorColor = GenerateAccentPurple
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Text(
                        text = "Misal: Untuk olahraga, Untuk kerja fokus...",
                        color = GenerateSecondaryText,
                        fontSize = 10.sp,
                        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Input Section: Keywords
                    Text(
                        text = "Kata kunci opsional (pisahkan dengan koma)",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth().padding(start = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = keywordsInput,
                            onValueChange = onKeywordsInputChange,
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Tambah kata kunci...") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GenerateAccentPurple,
                                unfocusedBorderColor = GenerateSecondaryText
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        keywords.forEach { keyword ->
                            InputChip(
                                selected = true,
                                onClick = { onRemoveKeyword(keyword) },
                                label = { Text(keyword) },
                                trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp)) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Input Section: Tone of Voice
                    var expanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedTone,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Nada Bicara Caption") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GenerateAccentPurple,
                                unfocusedBorderColor = GenerateSecondaryText
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { expanded = !expanded }
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f).background(GenerateCardBg)
                        ) {
                            tones.forEach { tone ->
                                DropdownMenuItem(
                                    text = { Text(tone, color = Color.White) },
                                    onClick = {
                                        onSelectedToneChange(tone)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Business Info Section (Smart Metadata Injection)
                    Text(
                        text = "Profil Bisnis di Caption",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth().padding(start = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = GenerateCardBg.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            OutlinedTextField(
                                value = businessName,
                                onValueChange = onBusinessNameChange,
                                label = { Text("Nama Usaha") },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = TextStyle(fontSize = 14.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GenerateAccentPurple,
                                    unfocusedBorderColor = GenerateSecondaryText
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = salesLink,
                                onValueChange = onSalesLinkChange,
                                label = { Text("Link Jualan") },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = TextStyle(fontSize = 14.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GenerateAccentPurple,
                                    unfocusedBorderColor = GenerateSecondaryText
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Voice Input FAB
                    FloatingActionButton(
                        onClick = { /* Handle normal click if needed */ },
                        containerColor = GenerateAccentPurple,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(72.dp)
                            .pointerInput(Unit) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { micViewModel.startRecording() },
                                    onDragEnd = {
                                        if (micViewModel.recordingState == RecordingState.RECORDING) {
                                            micViewModel.stopRecording(true)
                                        }
                                    },
                                    onDragCancel = { micViewModel.cancelRecording() },
                                    onDrag = { _, dragAmount ->
                                        if (micViewModel.recordingState == RecordingState.RECORDING) {
                                            if (dragAmount.y < -100) { // Swipe up to lock
                                                micViewModel.lockRecording()
                                            } else if (dragAmount.x < -100) { // Swipe left to cancel
                                                micViewModel.cancelRecording()
                                            }
                                        }
                                    }
                                )
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Input",
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = onBackClick,
                            modifier = Modifier.weight(1f).height(56.dp),
                            border = BorderStroke(2.dp, GenerateAccentPurple),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Text("Batal", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onStartProcessing,
                            modifier = Modifier.weight(1f).height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GenerateAccentPurple),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Text("Mulai Sekarang", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Ensure your data is descriptive for best results",
                        color = GenerateSecondaryText,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Overlay Mic PopUp
        MicPopUp(
            viewModel = micViewModel,
            onClose = { /* Handle popup close */ }
        )
    }
}

@Composable
fun CornerMarkers() {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Top Left
        Canvas(modifier = Modifier.size(20.dp).align(Alignment.TopStart)) {
            val strokeWidth = 3.dp.toPx()
            drawLine(Color.White, start = androidx.compose.ui.geometry.Offset.Zero, end = androidx.compose.ui.geometry.Offset(size.width, 0f), strokeWidth = strokeWidth)
            drawLine(Color.White, start = androidx.compose.ui.geometry.Offset.Zero, end = androidx.compose.ui.geometry.Offset(0f, size.height), strokeWidth = strokeWidth)
        }
        // Top Right
        Canvas(modifier = Modifier.size(20.dp).align(Alignment.TopEnd)) {
            val strokeWidth = 3.dp.toPx()
            drawLine(Color.White, start = androidx.compose.ui.geometry.Offset(size.width, 0f), end = androidx.compose.ui.geometry.Offset.Zero, strokeWidth = strokeWidth)
            drawLine(Color.White, start = androidx.compose.ui.geometry.Offset(size.width, 0f), end = androidx.compose.ui.geometry.Offset(size.width, size.height), strokeWidth = strokeWidth)
        }
        // Bottom Left
        Canvas(modifier = Modifier.size(20.dp).align(Alignment.BottomStart)) {
            val strokeWidth = 3.dp.toPx()
            drawLine(Color.White, start = androidx.compose.ui.geometry.Offset(0f, size.height), end = androidx.compose.ui.geometry.Offset(size.width, size.height), strokeWidth = strokeWidth)
            drawLine(Color.White, start = androidx.compose.ui.geometry.Offset(0f, size.height), end = androidx.compose.ui.geometry.Offset.Zero, strokeWidth = strokeWidth)
        }
        // Bottom Right
        Canvas(modifier = Modifier.size(20.dp).align(Alignment.BottomEnd)) {
            val strokeWidth = 3.dp.toPx()
            drawLine(Color.White, start = androidx.compose.ui.geometry.Offset(size.width, size.height), end = androidx.compose.ui.geometry.Offset(0f, size.height), strokeWidth = strokeWidth)
            drawLine(Color.White, start = androidx.compose.ui.geometry.Offset(size.width, size.height), end = androidx.compose.ui.geometry.Offset(size.width, 0f), strokeWidth = strokeWidth)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        content = { content() }
    )
}



@Preview(showBackground = true)
@Composable
fun GenerateScreenPreview() {
    GenerateScreenContent(
        selectedImageUri = null,
        onImageSelected = {},
        onBackClick = {},
        productModel = "Sepatu Lari Nike",
        onProductModelChange = {},
        productPurpose = "Untuk maraton",
        onProductPurposeChange = {},
        keywordsInput = "",
        onKeywordsInputChange = {},
        keywords = listOf("nyaman", "ringan", "keren"),
        onRemoveKeyword = {},
        selectedTone = "Hype",
        onSelectedToneChange = {},
        tones = listOf("Hype", "Formal", "Santai", "Profesional", "Lucu"),
        businessName = "Toko Olahraga Jaya",
        onBusinessNameChange = {},
        salesLink = "https://wa.me/628123456789",
        onSalesLinkChange = {},
        onStartProcessing = {}
    )
}
