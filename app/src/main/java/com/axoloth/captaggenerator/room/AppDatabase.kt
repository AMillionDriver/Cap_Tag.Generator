package com.axoloth.captaggenerator.room

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.axoloth.captaggenerator.service.security.KeyStoreManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Database(entities = [UserEntity::class, HistoryEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao

    companion object {
        private const val DATABASE_NAME = "captag_secure.db"
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private val instanceMutex = Mutex()
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `history` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `productName` TEXT NOT NULL,
                        `copywriting` TEXT NOT NULL,
                        `productDescription` TEXT NOT NULL,
                        `tagsAndHashtags` TEXT NOT NULL,
                        `imageUri` TEXT,
                        `timestamp` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        suspend fun getInstance(context: Context): AppDatabase = withContext(Dispatchers.IO) {
            INSTANCE ?: instanceMutex.withLock {
                INSTANCE ?: buildDatabase(context.applicationContext).also { instance ->
                    INSTANCE = instance
                }
            }
        }

        private suspend fun buildDatabase(context: Context): AppDatabase {
            return try {
                // SQLCipher 4.6.1+ initialization
                // CRITICAL: We must ensure native library is loaded before any SQLite operation
                System.loadLibrary("sqlcipher")

                val prefs = context.getSharedPreferences("security_prefs", Context.MODE_PRIVATE)
                val dbKey = KeyStoreManager.getDatabaseKey(prefs)
                val factory = SupportOpenHelperFactory(dbKey)

                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .openHelperFactory(factory)
                .addMigrations(MIGRATION_1_2)
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
                .build()
            }
        }
        
        suspend fun getSafeInstance(context: Context): AppDatabase = withContext(Dispatchers.IO) {
            var database: AppDatabase? = null
            try {
                val openedDatabase = getInstance(context)
                database = openedDatabase
                validateDatabase(openedDatabase)
                openedDatabase
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (e: Exception) {
                Log.e("AppDatabase", "Error opening database: ${e.message}")
                clearFailedInstance(database)
                throw DatabaseOpenException(databaseErrorMessage(e), e)
            }
        }

        private fun validateDatabase(database: AppDatabase) {
            database.query("SELECT 1", null).use { cursor ->
                cursor.moveToFirst()
            }
        }

        private suspend fun clearFailedInstance(failedDatabase: AppDatabase?) {
            instanceMutex.withLock {
                if (INSTANCE === failedDatabase) {
                    INSTANCE?.close()
                    INSTANCE = null
                } else {
                    failedDatabase?.close()
                }
            }
        }

        private fun databaseErrorMessage(error: Exception): String {
            val errorMessage = error.message.orEmpty()
            return when {
                error is android.database.sqlite.SQLiteDiskIOException ||
                    errorMessage.contains("disk I/O error", ignoreCase = true) ->
                    "Penyimpanan perangkat sedang bermasalah. Data tidak dihapus; silakan coba lagi."

                errorMessage.contains("file is not a database", ignoreCase = true) ||
                    errorMessage.contains("file is encrypted", ignoreCase = true) ->
                    "Database terenkripsi tidak dapat dibuka dengan kunci perangkat ini. Data tidak dihapus."

                errorMessage.contains("migration", ignoreCase = true) ->
                    "Versi database belum memiliki jalur migrasi yang aman. Data tidak dihapus."

                else ->
                    "Database tidak dapat dibuka. Data tidak dihapus; silakan coba lagi."
            }
        }

        private class DatabaseOpenException(
            message: String,
            cause: Throwable
        ) : Exception(message, cause)
    }
}
