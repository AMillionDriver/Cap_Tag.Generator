package com.axoloth.captaggenerator.logic

import com.axoloth.captaggenerator.room.UserDao
import com.axoloth.captaggenerator.room.UserEntity
import com.axoloth.captaggenerator.service.security.AesSecurity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepository(private val userDao: UserDao) {

    fun getUser(): Flow<User?> = userDao.getUser().map { entity ->
        entity?.let {
            User(
                userName = AesSecurity.decrypt(it.encryptedUserName),
                businessName = AesSecurity.decrypt(it.encryptedBusinessName),
                category = AesSecurity.decrypt(it.encryptedCategory),
                profileImageUri = it.profileImageUri
            )
        }
    }

    suspend fun saveUser(user: User) {
        val entity = UserEntity(
            encryptedUserName = AesSecurity.encrypt(user.userName),
            encryptedBusinessName = AesSecurity.encrypt(user.businessName),
            encryptedCategory = AesSecurity.encrypt(user.category),
            profileImageUri = user.profileImageUri
        )
        userDao.insertUser(entity)
    }
}

data class User(
    val userName: String,
    val businessName: String,
    val category: String,
    val profileImageUri: String?
)
