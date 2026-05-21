package com.axoloth.captaggenerator.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productName: String,
    val copywriting: String,
    val productDescription: String,
    val tagsAndHashtags: String,
    val imageUri: String?,
    val timestamp: Long = System.currentTimeMillis()
)
