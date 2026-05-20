package com.axoloth.captaggenerator.screen.fragment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.axoloth.captaggenerator.logic.fragment.StorageViewModel

private val PopupBg = Color(0xFF2C2C2E)
private val DeleteRed = Color(0xFFFF453A)
private val SecondaryText = Color(0xFF8E8E93)

@Composable
fun StoragePopUp(
    onDismiss: () -> Unit,
    viewModel: StorageViewModel = viewModel()
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = PopupBg,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = DeleteRed,
                    modifier = Modifier.size(40.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Bersihkan Cache & Riwayat",
                    color = DeleteRed,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Checkbox: Hapus Cache
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = viewModel.isDeleteCacheChecked,
                        onCheckedChange = { viewModel.isDeleteCacheChecked = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = DeleteRed,
                            uncheckedColor = Color.Gray
                        )
                    )
                    Text("Hapus Cache", color = Color.White, fontSize = 16.sp)
                }
                
                // Checkbox: Hapus Riwayat
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = viewModel.isDeleteHistoryChecked,
                        onCheckedChange = { viewModel.isDeleteHistoryChecked = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = DeleteRed,
                            uncheckedColor = Color.Gray
                        )
                    )
                    Text("Hapus Riwayat", color = Color.White, fontSize = 16.sp)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal", color = SecondaryText, fontSize = 16.sp)
                    }
                    
                    Button(
                        onClick = {
                            viewModel.performDeletion(context) { success ->
                                if (success) onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeleteRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(48.dp).weight(1f).padding(start = 16.dp),
                        enabled = !viewModel.isProcessing && (viewModel.isDeleteCacheChecked || viewModel.isDeleteHistoryChecked)
                    ) {
                        if (viewModel.isProcessing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Hapus", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
