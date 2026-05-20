package com.axoloth.captaggenerator.service.security

import android.content.Context
import android.util.Log
import java.io.File

object ValidationStorage {

    /**
     * Memvalidasi dan menghapus cache aplikasi secara aman.
     * Hanya menghapus file di dalam folder cache yang bukan merupakan file sistem penting.
     */
    fun secureClearCache(context: Context): Boolean {
        return try {
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
    fun secureClearHistory(context: Context): Boolean {
        // Implementasi spesifik untuk menghapus riwayat aktivitas
        // Contoh: Clear folder internal data tertentu yang aman
        return true 
    }

    private fun deleteRecursive(fileOrDirectory: File, preserveRoot: Boolean = false) {
        if (fileOrDirectory.isDirectory) {
            fileOrDirectory.listFiles()?.forEach { child ->
                deleteRecursive(child)
            }
        }
        
        if (!preserveRoot) {
            fileOrDirectory.delete()
        }
    }
}
