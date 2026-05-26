package com.axoloth.captaggenerator.room

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.axoloth.captaggenerator.service.security.KeyStoreManager
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SQLiteDatabaseHook
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Database(entities = [UserEntity::class, HistoryEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao

    companion object {
        private const val DATABASE_NAME = "captag_secure.db"
        @Volatile
        private var INSTANCE: AppDatabase? = null

        suspend fun getInstance(context: Context): AppDatabase = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            INSTANCE ?: synchronized(this) {
                val instance = buildDatabase(context)
                INSTANCE = instance
                instance
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return try {
                // SQLCipher 4.6.1+ initialization
                // CRITICAL: We must ensure native library is loaded before any SQLite operation
                System.loadLibrary("sqlcipher")
                
                val prefs = context.getSharedPreferences("security_prefs", Context.MODE_PRIVATE)
                // Note: In real app, we should call KeyStoreManager.getDatabaseKey(prefs) 
                // in a suspend way. For Room builder, we might need a workaround or 
                // pre-fetch the key.
                val dbKey = kotlinx.coroutines.runBlocking { KeyStoreManager.getDatabaseKey(prefs) }
                val factory = SupportOpenHelperFactory(dbKey)

                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration()
                .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
                // Set query executor to IO to avoid blocking main thread on accidental access
                .setQueryExecutor(java.util.concurrent.Executors.newSingleThreadExecutor())
                .build()
            } catch (e: UnsatisfiedLinkError) {
                // Fallback for Compose Preview or environments without SQLCipher native libs
                Log.w("AppDatabase", "SQLCipher library not found, falling back to in-memory database: ${e.message}")
                Room.inMemoryDatabaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java
                )
                .fallbackToDestructiveMigration()
                .build()
            }
        }
        
        suspend fun getSafeInstance(context: Context): AppDatabase? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val db = getInstance(context)
                // Trigger an actual open by running a simple query
                val c = db.query("SELECT 1", null)
                c.close()
                db
            } catch (e: Exception) {
                Log.e("AppDatabase", "Error opening database: ${e.message}")
                
                // Jika terdeteksi Disk I/O Error atau File Corrupt, kita bersihkan
                if (e is android.database.sqlite.SQLiteDiskIOException || 
                    e.message?.contains("disk I/O error") == true ||
                    e.message?.contains("file is not a database") == true || 
                    e.message?.contains("file is encrypted") == true) {
                    
                    Log.w("AppDatabase", "Attempting to recover from Disk I/O or Corruption...")
                    context.deleteDatabase(DATABASE_NAME)
                    INSTANCE = null // Reset instance to force rebuild
                    try {
                        val newDb = getInstance(context)
                        val c = newDb.query("SELECT 1", null)
                        c.close()
                        newDb
                    } catch (retryException: Exception) {
                        Log.e("AppDatabase", "Failed to recover database: ${retryException.message}")
                        null
                    }
                } else {
                    null
                }
            }
        }

        // Bridge for non-suspend contexts where we are already on a background thread
        fun getSafeInstanceBlocking(context: Context): AppDatabase? {
            return kotlinx.coroutines.runBlocking { getSafeInstance(context) }
        }
    }
}
