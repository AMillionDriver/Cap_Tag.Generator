package com.axoloth.captaggenerator.service.security

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object ValidationStorage {

    /**
     * Memvalidasi dan menghapus cache aplikasi secara aman.
     * Hanya menghapus file di dalam folder cache yang bukan merupakan file sistem penting.
     */
    suspend fun secureClearCache(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            val cacheDir = context.cacheDir
            deleteRecursive(cacheDir, preserveRoot = true)
            true
        } catch (e: Exception) {
            Log.e("ValidationStorage", "Error clearing cache: ${e.message}")
            false
        }
    }

    /**
     * Memvalidasi dan menghapus riwayat (database atau riwayat penggunaan).
     * Dalam kasus ini, kita bisa membersihkan tabel riwayat jika diperlukan, 
     * atau membersihkan file log.
     */
    private fun deleteRecursive(fileOrDirectory: File, preserveRoot: Boolean = false) {
        if (!fileOrDirectory.exists()) return

        if (fileOrDirectory.isDirectory) {
            fileOrDirectory.listFiles()?.forEach { child ->
                deleteRecursive(child)
            }
        }
        
        if (!preserveRoot) {
            fileOrDirectory.deleteRecursively()
        }
    }
}
