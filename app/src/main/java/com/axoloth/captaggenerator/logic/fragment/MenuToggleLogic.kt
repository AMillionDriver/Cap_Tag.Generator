package com.axoloth.captaggenerator.logic.fragment

import com.axoloth.captaggenerator.logic.MainScreenViewModel
import com.axoloth.captaggenerator.logic.Screen

object MenuToggleLogic {
    private const val TENTANG_URL = "https://axolothdev.blogspot.com/2026/05/lapak-ai-captaggenerator-aplikasi.html"

    fun handleTentangLapakAI(viewModel: MainScreenViewModel) {
        viewModel.navigateTo(Screen.WebView(TENTANG_URL))
    }
}
