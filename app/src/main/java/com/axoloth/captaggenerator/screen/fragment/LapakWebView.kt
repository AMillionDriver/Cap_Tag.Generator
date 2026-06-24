package com.axoloth.captaggenerator.screen.fragment

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LapakWebView(
    url: String,
    onBackClick: () -> Unit
) {
    val webViewHolder = remember { arrayOfNulls<WebView>(1) }

    DisposableEffect(Unit) {
        onDispose {
            webViewHolder[0]?.run {
                stopLoading()
                loadUrl("about:blank")
                clearHistory()
                removeAllViews()
                destroy()
            }
            webViewHolder[0] = null
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        TopAppBar(
            title = { Text("Tentang LapakAI", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.padding(8.dp).background(Color.White, CircleShape)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Black,
                titleContentColor = Color.White
            )
        )
        
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webViewHolder[0] = this
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                    loadUrl(url)
                }
            },
            update = { webView ->
                if (webView.url != url) {
                    webView.loadUrl(url)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
