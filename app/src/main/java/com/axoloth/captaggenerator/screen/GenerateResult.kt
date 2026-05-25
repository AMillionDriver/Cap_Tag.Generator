package com.axoloth.captaggenerator.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.tooling.preview.Preview
import com.axoloth.captaggenerator.logic.GenerateResultViewModel
import com.axoloth.captaggenerator.logic.Screen
import com.axoloth.captaggenerator.logic.MainScreenViewModel
import com.axoloth.captaggenerator.ui.theme.CapTagGeneratorTheme

private val CardBg = Color(0xFF000000)
private val SecondaryText = Color(0xFF8E8E93)

@Composable
fun GenerateResult(
    imageUri: Uri?,
    productName: String,
    viewModel: GenerateResultViewModel,
    onBackClick: () -> Unit,
    onRegenerateClick: () -> Unit = {}
) {
    val context = LocalContext.current
    
    GenerateResultContent(
        imageUri = imageUri,
        productName = productName,
        copywriting = viewModel.copywriting,
        productDescription = viewModel.productDescription,
        tagsAndHashtags = viewModel.tagsAndHashtags,
        isSaving = viewModel.isSaving,
        onBackClick = onBackClick,
        onSaveClick = {
            viewModel.saveToHistory(imageUri?.toString()) {
                Toast.makeText(context, "Berhasil disimpan ke Riwayat", Toast.LENGTH_SHORT).show()
                onBackClick()
            }
        },
        onRegenerateClick = {
            viewModel.regenerate()
            onRegenerateClick()
        },
        onShareClick = {
            val shareText = """
                $productName
                
                [COPYWRITING]
                ${viewModel.copywriting}
                
                [DESCRIPTION]
                ${viewModel.productDescription}
                
                [TAGS & HASHTAGS]
                ${viewModel.tagsAndHashtags}
            """.trimIndent()

            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, "Bagikan Hasil")
            context.startActivity(shareIntent)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateResultContent(
    imageUri: Uri?,
    productName: String,
    copywriting: String,
    productDescription: String,
    tagsAndHashtags: String,
    isSaving: Boolean,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    onRegenerateClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hasil Generasi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = Color.White)
                    }
                },
                actions = {
                    Button(
                        onClick = onSaveClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFFFFF)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save", fontSize = 14.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black, titleContentColor = Color.White)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Image Preview
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = productName.uppercase(),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(text = "Model: AI Optimized", color = SecondaryText, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(24.dp))

            // Result Sections
            ResultCard("COPYWRITING PRODUCT", copywriting)
            Spacer(modifier = Modifier.height(16.dp))
            ResultCard("PRODUCT DESCRIPTION", productDescription)
            Spacer(modifier = Modifier.height(16.dp))
            ResultCard("TAGS & HASHTAGS", tagsAndHashtags)

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onShareClick,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)
                ) {
                    Icon(Icons.Default.Share, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share Result", color = Color.White)
                }

                Button(
                    onClick = onRegenerateClick,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF065FF6))
                ) {
                    Icon(Icons.Default.Refresh, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Regenerate")
                }
            }
        }
    }
}

@Composable
fun ResultCard(title: String, content: String) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(12.dp))
            .border(0.5.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, color = Color(0xFF8A2BE2), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            IconButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText(title, content)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "$title disalin!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = "Salin $title",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(content, color = Color.White, fontSize = 14.sp, lineHeight = 20.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun GenerateResultPreview() {
    CapTagGeneratorTheme {
        GenerateResultContent(
            imageUri = null,
            productName = "Sample Product",
            copywriting = "This is a sample copywriting for the product. It should be catchy and engaging.",
            productDescription = "This is a sample product description. It describes the features and benefits of the product.",
            tagsAndHashtags = "#sample #product #ai #generator",
            isSaving = false,
            onBackClick = {},
            onSaveClick = {},
            onRegenerateClick = {},
            onShareClick = {}
        )
    }
}
