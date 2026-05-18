package com.axoloth.captaggenerator.logic

import androidx.lifecycle.ViewModel
import com.axoloth.captaggenerator.logic.fragment.HistoryItem
import com.axoloth.captaggenerator.logic.fragment.HistoryManager

class HistoryViewModel : ViewModel() {
    val historyItems: List<HistoryItem> get() = HistoryManager.historyItems

    fun clearAllHistory() {
        HistoryManager.clearAll()
    }
}
