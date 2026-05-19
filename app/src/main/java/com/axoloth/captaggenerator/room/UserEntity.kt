package com.axoloth.captaggenerator.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val encryptedUserName: String,
    val encryptedBusinessName: String,
    val encryptedCategory: String,
    val profileImageUri: String?
)
