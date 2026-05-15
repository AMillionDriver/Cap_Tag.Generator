package com.axoloth.captaggenerator.screen

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.axoloth.captaggenerator.logic.GenerateScreenViewModel
import com.axoloth.captaggenerator.ui.theme.CapTagGeneratorTheme
import java.text.SimpleDateFormat
import java.util.*

private val GenerateBgColor = Color(0xFF0D0D0D)
private val GenerateCardBg = Color(0xFF161B22)
private val GenerateAccentPurple = Color(0xFF8A2BE2)
private val GenerateSecondaryText = Color(0xFF8E8E93)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateScreen(
    selectedImageUri: Uri?,
    onBackClick: () -> Unit,
    viewModel: GenerateScreenViewModel = viewModel()
) {
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
                ) {
                    if (selectedImageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(selectedImageUri),
                            contentDescription = "Selected Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Floating Corners
                    CornerMarkers()

                    // Bottom info bar in the image card
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
                                .clickable { onBackClick() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Input Section: Product Model
                OutlinedTextField(
                    value = viewModel.productModel,
                    onValueChange = { viewModel.productModel = it },
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
                    value = viewModel.productPurpose,
                    onValueChange = { viewModel.productPurpose = it },
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
                        value = viewModel.keywordsInput,
                        onValueChange = { 
                            viewModel.keywordsInput = it
                            if (it.endsWith(",")) {
                                viewModel.addKeyword()
                            }
                        },
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
                    viewModel.keywords.forEach { keyword ->
                        InputChip(
                            selected = true,
                            onClick = { viewModel.removeKeyword(keyword) },
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
                        value = viewModel.selectedTone,
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
                        viewModel.tones.forEach { tone ->
                            DropdownMenuItem(
                                text = { Text(tone, color = Color.White) },
                                onClick = {
                                    viewModel.selectedTone = tone
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

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
                        onClick = { viewModel.onGenerateClick() },
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
