package com.axoloth.captaggenerator.room

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.axoloth.captaggenerator.service.security.KeyStoreManager
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

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
            // Initialize SQLCipher library
            SQLiteDatabase.loadLibs(context)
            
            val prefs = context.getSharedPreferences("security_prefs", Context.MODE_PRIVATE)
            val dbKey = KeyStoreManager.getDatabaseKey(prefs)
            val factory = SupportFactory(dbKey)

            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration() // Handle schema changes by wiping data if needed
            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE) // SQLCipher works best with TRUNCATE
            .addCallback(object : RoomDatabase.Callback() {
                // Optional callbacks
            })
            .build()
        }
        
        // Robust opening to handle SQLiteNotADatabaseException/Corrupt database
        fun getSafeInstance(context: Context): AppDatabase? {
            return try {
                val db = getInstance(context)
                // Trigger an actual open by running a simple query if needed
                db.query("SELECT 1", null)
                db
            } catch (e: Exception) {
                Log.e("AppDatabase", "Error opening database, possibly invalid key or corrupt file: ${e.message}")
                // Delete database file if corrupt to prevent crash loop
                context.deleteDatabase(DATABASE_NAME)
                // Attempt to rebuild from scratch
                try {
                    getInstance(context)
                } catch (retryException: Exception) {
                    Log.e("AppDatabase", "Fatal error after retry: ${retryException.message}")
                    null
                }
            }
        }
    }
}
