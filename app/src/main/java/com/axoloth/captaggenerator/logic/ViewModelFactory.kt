package com.axoloth.captaggenerator.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.axoloth.captaggenerator.room.UserDao

class AccountViewModelFactory(private val repository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccountViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AccountViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
