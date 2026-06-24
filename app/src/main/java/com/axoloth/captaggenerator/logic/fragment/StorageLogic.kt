package com.axoloth.captaggenerator.logic.fragment

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axoloth.captaggenerator.room.UserDao
import com.axoloth.captaggenerator.service.security.ValidationStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StorageViewModel : ViewModel() {
    var isDeleteCacheChecked by mutableStateOf(false)
    var isDeleteHistoryChecked by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
        private set
    
    var isProcessing by mutableStateOf(false)
        private set

    fun performDeletion(
        context: Context,
        userDao: UserDao?,
        onComplete: (Boolean) -> Unit
    ) {
        if (!isDeleteCacheChecked && !isDeleteHistoryChecked) return
        if (isDeleteHistoryChecked && userDao == null) {
            errorMessage = "Database riwayat belum tersedia."
            onComplete(false)
            return
        }

        isProcessing = true
        errorMessage = null

        viewModelScope.launch {
            val success = runCatching {
                var operationSuccess = true

                if (isDeleteCacheChecked) {
                    operationSuccess = operationSuccess &&
                        ValidationStorage.secureClearCache(context.applicationContext)
                }

                if (isDeleteHistoryChecked) {
                    withContext(Dispatchers.IO) {
                        requireNotNull(userDao).deleteAllHistory()
                    }
                }

                operationSuccess
            }.onFailure { error ->
                errorMessage = error.message ?: "Gagal membersihkan penyimpanan."
            }.getOrDefault(false)

            isProcessing = false
            if (success) {
                isDeleteCacheChecked = false
                isDeleteHistoryChecked = false
            }
            onComplete(success)
        }
    }
}
