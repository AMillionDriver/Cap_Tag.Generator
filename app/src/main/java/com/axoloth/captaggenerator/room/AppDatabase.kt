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

@Database(entities = [UserEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao

    companion object {
        private const val DATABASE_NAME = "captag_secure.db"
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = buildDatabase(context)
                INSTANCE = instance
                instance
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            // SQLCipher 4.6.1+ initialization
            // loadLibs is no longer required in modern SQLCipher Android, 
            // but we keep it safe or use the new initialization pattern if needed.
            System.loadLibrary("sqlcipher")
            
            val prefs = context.getSharedPreferences("security_prefs", Context.MODE_PRIVATE)
            val dbKey = KeyStoreManager.getDatabaseKey(prefs)
            val factory = SupportOpenHelperFactory(dbKey)

            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
            .build()
        }
        
        // Robust opening to handle SQLiteNotADatabaseException/Corrupt database
        fun getSafeInstance(context: Context): AppDatabase? {
            return try {
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
    }
}
