package com.axoloth.captaggenerator.logic

import androidx.lifecycle.ViewModel
import com.axoloth.captaggenerator.logic.fragment.HistoryItem
import com.axoloth.captaggenerator.logic.fragment.HistoryManager

import androidx.lifecycle.viewModelScope
import com.axoloth.captaggenerator.room.HistoryEntity
import com.axoloth.captaggenerator.room.UserDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(private val userDao: UserDao) : ViewModel() {
    val historyItems: StateFlow<List<HistoryEntity>> = userDao.getAllHistory()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun clearAllHistory() {
        viewModelScope.launch {
            userDao.deleteAllHistory()
        }
    }
}
