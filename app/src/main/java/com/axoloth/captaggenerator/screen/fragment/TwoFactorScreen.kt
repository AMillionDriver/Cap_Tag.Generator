package com.axoloth.captaggenerator.screen.fragment

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.axoloth.captaggenerator.logic.TwoFactorViewModel
import com.axoloth.captaggenerator.logic.VerificationStatus
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoFactorScreen(
    onBackClick: () -> Unit = {},
    onSaveSuccess: () -> Unit = {},
    viewModel: TwoFactorViewModel = viewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }

    TwoFactorScreenContent(
        qrBitmap = viewModel.qrBitmap,
        rawSecret = viewModel.rawSecret,
        displaySecret = viewModel.displaySecret,
        code1 = viewModel.code1,
        status1 = viewModel.status1,
        code2 = viewModel.code2,
        status2 = viewModel.status2,
        code3 = viewModel.code3,
        status3 = viewModel.status3,
        onCode1Change = { viewModel.code1 = it },
        onCode2Change = { viewModel.code2 = it },
        onCode3Change = { viewModel.code3 = it },
        onVerify1 = { viewModel.verifyCode1(it) { /* handled in focus */ } },
        onVerify2 = { viewModel.verifyCode2(it) { /* handled in focus */ } },
        onVerify3 = { viewModel.verifyCode3(it) },
        onReset1 = { viewModel.resetCode1() },
        onReset2 = { viewModel.resetCode2() },
        onReset3 = { viewModel.resetCode3() },
        onBackClick = onBackClick,
        onSaveSuccess = onSaveSuccess,
        canSave = viewModel.canSave(),
        snackbarHostState = snackbarHostState,
        onVerify1WithNext = { input, onNext -> viewModel.verifyCode1(input, onNext) },
        onVerify2WithNext = { input, onNext -> viewModel.verifyCode2(input, onNext) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoFactorScreenContent(
    qrBitmap: Bitmap?,
    rawSecret: String,
    displaySecret: String,
    code1: String,
    status1: VerificationStatus,
    code2: String,
    status2: VerificationStatus,
    code3: String,
    status3: VerificationStatus,
    onCode1Change: (String) -> Unit,
    onCode2Change: (String) -> Unit,
    onCode3Change: (String) -> Unit,
    onVerify1: (String) -> Unit,
    onVerify2: (String) -> Unit,
    onVerify3: (String) -> Unit,
    onVerify1WithNext: (String, () -> Unit) -> Unit = { s, f -> },
    onVerify2WithNext: (String, () -> Unit) -> Unit = { s, f -> },
    onReset1: () -> Unit,
    onReset2: () -> Unit,
    onReset3: () -> Unit,
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit,
    canSave: Boolean,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    // Focus management
    val focus1 = remember { FocusRequester() }
    val focus2 = remember { FocusRequester() }
    val focus3 = remember { FocusRequester() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TWO-FACTOR AUTHENTICATION ACTIVATED.", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black, titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Black
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // QR Code
            qrBitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "QR Code",
                    modifier = Modifier.size(200.dp).background(Color.White).padding(8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Alternatively, if you cannot scan the QR code, please enter this code into your authenticator app:",
                color = Color.Gray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = displaySecret,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.clickable {
                        // Copy the RAW secret (without dashes) to prevent "illegal value" in Authenticator app
                        clipboardManager.setText(AnnotatedString(rawSecret))
                        scope.launch { snackbarHostState.showSnackbar("Secret copied to clipboard") }
                    }
                )
                IconButton(onClick = {
                    // Copy the RAW secret (without dashes)
                    clipboardManager.setText(AnnotatedString(rawSecret))
                    scope.launch { snackbarHostState.showSnackbar("Secret copied to clipboard") }
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "After registering in your authenticator app, please enter the codes below to verify:\nEnter the first code, wait for it to expire (30s), enter the second code, and repeat for the third.\nCorrect codes will turn green, incorrect codes red.",
                color = Color.Gray,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CodeInputField(
                    modifier = Modifier.weight(1f).focusRequester(focus1),
                    label = "FIRST CODE",
                    value = code1,
                    status = status1,
                    onValueChange = { if (it.length <= 6) onCode1Change(it) },
                    onVerify = { input ->
                        onVerify1WithNext(input) { focus2.requestFocus() }
                    },
                    onResetRequest = {
                        scope.launch {
                            val result = snackbarHostState.showSnackbar("Yakin ingin mengubah?", actionLabel = "Ya")
                            if (result == SnackbarResult.ActionPerformed) {
                                onReset1()
                            }
                        }
                    }
                )
                CodeInputField(
                    modifier = Modifier.weight(1f).focusRequester(focus2),
                    label = "SECOND CODE",
                    value = code2,
                    status = status2,
                    onValueChange = { if (it.length <= 6) onCode2Change(it) },
                    onVerify = { input ->
                        onVerify2WithNext(input) { focus3.requestFocus() }
                    },
                    onResetRequest = {
                        scope.launch {
                            val result = snackbarHostState.showSnackbar("Yakin ingin mengubah?", actionLabel = "Ya")
                            if (result == SnackbarResult.ActionPerformed) {
                                onReset2()
                            }
                        }
                    }
                )
                CodeInputField(
                    modifier = Modifier.weight(1f).focusRequester(focus3),
                    label = "THIRD CODE",
                    value = code3,
                    status = status3,
                    onValueChange = { if (it.length <= 6) onCode3Change(it) },
                    onVerify = { input ->
                        onVerify3(input)
                    },
                    onResetRequest = {
                        scope.launch {
                            val result = snackbarHostState.showSnackbar("Yakin ingin mengubah?", actionLabel = "Ya")
                            if (result == SnackbarResult.ActionPerformed) {
                                onReset3()
                            }
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = onSaveSuccess,
                enabled = canSave,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8A2BE2))
            ) {
                Text("SAVE CONFIGURATION", fontWeight = FontWeight.Bold)
            }
            Text("Ensure your settings are saved securely.", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
fun CodeInputField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    status: VerificationStatus,
    onValueChange: (String) -> Unit,
    onVerify: (String) -> Unit,
    onResetRequest: () -> Unit = {}
) {
    val borderColor = when (status) {
        VerificationStatus.IDLE -> Color.DarkGray
        VerificationStatus.SUCCESS -> Color.Green
        VerificationStatus.ERROR -> Color.Red
    }
    
    LaunchedEffect(value) {
        if (value.length == 6 && status == VerificationStatus.IDLE) {
            onVerify(value)
        }
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = status == VerificationStatus.IDLE,
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(onDoubleTap = { if (status == VerificationStatus.SUCCESS) onResetRequest() })
                },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = borderColor,
                unfocusedBorderColor = borderColor,
                disabledBorderColor = borderColor,
                cursorColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                disabledTextColor = Color.White,
                focusedContainerColor = Color(0xFF1A1A1A),
                unfocusedContainerColor = Color(0xFF1A1A1A),
                disabledContainerColor = Color(0xFF1A1A1A)
            )
        )
    }
}

@Preview
@Composable
fun TwoFactorPreview() {
    TwoFactorScreenContent(
        qrBitmap = null,
        rawSecret = "ABCDEFGHIJKLMNO",
        displaySecret = "ABCD-EFGH-IJKL-MNOP",
        code1 = "",
        status1 = VerificationStatus.IDLE,
        code2 = "",
        status2 = VerificationStatus.IDLE,
        code3 = "",
        status3 = VerificationStatus.IDLE,
        onCode1Change = {},
        onCode2Change = {},
        onCode3Change = {},
        onVerify1 = {},
        onVerify2 = {},
        onVerify3 = {},
        onReset1 = {},
        onReset2 = {},
        onReset3 = {},
        onBackClick = {},
        onSaveSuccess = {},
        canSave = false
    )
}
