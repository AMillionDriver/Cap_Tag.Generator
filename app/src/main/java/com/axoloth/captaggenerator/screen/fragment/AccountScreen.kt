package com.axoloth.captaggenerator.screen.fragment

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.axoloth.captaggenerator.logic.AccountHistoryItem
import com.axoloth.captaggenerator.logic.AccountViewModel
import com.axoloth.captaggenerator.service.storage.PersistableUriPermission
import com.axoloth.captaggenerator.ui.theme.CapTagGeneratorTheme

@Composable
fun AccountScreen(
    onBackClick: () -> Unit,
    viewModel: AccountViewModel = viewModel(),
) {
    var showEditPopup by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { PersistableUriPermission.takeRead(context, it) }
        viewModel.updateProfileImage(uri)
    }

    CapTagGeneratorTheme(darkTheme = true) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D0D17))
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Top Bar
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Profile Card Section
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C26))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(top = 70.dp, bottom = 32.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = viewModel.userName,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { showEditPopup = true }
                        )
                        Text(
                            text = viewModel.businessName,
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                        Text(
                            text = viewModel.category,
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }

                // Profile Image overlapping
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .border(4.dp, Color(0xFF0D0D17), CircleShape)
                        .clip(CircleShape)
                        .background(Color.DarkGray)
                        .clickable { photoPickerLauncher.launch(arrayOf("image/*")) },
                    contentAlignment = Alignment.Center
                ) {
                    if (viewModel.profileImageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(viewModel.profileImageUri),
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Riwayat Penggunaan Aplikasi",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // History Timeline
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(viewModel.accountHistory) { index, item ->
                    HistoryTimelineRow(
                        item = item,
                        isLast = (index == viewModel.accountHistory.size - 1)
                    )
                }
            }
        }

        if (showEditPopup) {
            PopUpNickName(
                initialName = viewModel.userName,
                onDismiss = { showEditPopup = false },
                onSave = { newName ->
                    viewModel.updateUserName(newName)
                    showEditPopup = false
                }
            )
        }
    }
}

@Composable
fun HistoryTimelineRow(item: AccountHistoryItem, isLast: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxHeight()
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(item.iconBackgroundColor, CircleShape)
                    .border(2.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(Color(0xFF3430DC).copy(alpha = 0.5f)) // Connecting line
                )
            }
        }

        Spacer(modifier = Modifier.width(20.dp))

        Column(
            modifier = Modifier
                .padding(bottom = 32.dp)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = item.title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = item.date,
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}
