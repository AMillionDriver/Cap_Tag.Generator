package com.axoloth.captaggenerator.logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class CategoryItem(val name: String)

class CategoryViewModel : ViewModel() {
    var searchQuery by mutableStateOf("")
    
    private val _defaultCategories = listOf(
        "Agrikultur", "Aksesoris", "Alat Tulis",
        "Bahan Bangunan", "Busana", "Barang Elektronik",
        "Cinderamata", "Daging", "Edukasi", "Farmasi",
        "Gawai", "Hotel", "Industri", "Jasa", "Kuliner",
        "Logistik", "Manufaktur", "Niaga", "Otomotif",
        "Pariwisata", "Retail", "Seni", "Tekstil",
        "Usaha Kreatif", "Video", "Wisata"
    )

    private val _userCategories = mutableStateListOf<String>()

    val categoriesGrouped: Map<Char, List<String>>
        get() {
            val all = (_defaultCategories + _userCategories)
                .filter { it.contains(searchQuery, ignoreCase = true) }
                .sortedBy { it }
            
            return all.groupBy { it.first().uppercaseChar() }
        }

    fun addCategory(name: String) {
        if (name.isNotBlank() && !_defaultCategories.contains(name) && !_userCategories.contains(name)) {
            _userCategories.add(name)
        }
    }
}
