package com.axoloth.captaggenerator.logic.fragment

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.axoloth.captaggenerator.service.security.ValidationStorage

class StorageViewModel : ViewModel() {
    var isDeleteCacheChecked by mutableStateOf(false)
    var isDeleteHistoryChecked by mutableStateOf(false)
    
    var isProcessing by mutableStateOf(false)
        private set

    fun performDeletion(context: Context, onComplete: (Boolean) -> Unit) {
        if (!isDeleteCacheChecked && !isDeleteHistoryChecked) return
        
        isProcessing = true
        
        // Menjalankan validasi dan eksekusi keamanan
        var success = true
        
        if (isDeleteCacheChecked) {
            success = success && ValidationStorage.secureClearCache(context)
        }
        
        if (isDeleteHistoryChecked) {
            success = success && ValidationStorage.secureClearHistory(context)
        }
        
        isProcessing = false
        onComplete(success)
    }
}
