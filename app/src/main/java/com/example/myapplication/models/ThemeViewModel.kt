package com.example.myapplication.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.ui.theme.ThemePreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val themePreferences = ThemePreferences(application.applicationContext)

    // Expose the theme state as a StateFlow for Compose to observe
    val isDarkTheme: StateFlow<Boolean> = themePreferences.isDarkTheme
        .map { it } // No extra transformation needed here, just map to ensure it's a StateFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Keep the flow active as long as there's a collector
            initialValue = false // Initial value while DataStore is loading
        )

    fun setDarkTheme(isDarkTheme: Boolean) {
        viewModelScope.launch {
            themePreferences.setDarkTheme(isDarkTheme)
        }
    }
}