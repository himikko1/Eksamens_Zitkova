/*
package com.example.myapplication.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.ui.theme.LanguagePreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LanguageViewModel(application: Application) : AndroidViewModel(application) {

    private val languagePreferences = LanguagePreferences(application.applicationContext)

    val currentLanguage: StateFlow<String> = languagePreferences.getLanguage
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LanguagePreferences.DEFAULT_LANGUAGE_CODE
        )

    fun setLanguage(languageCode: String) {
        viewModelScope.launch {
            languagePreferences.setLanguage(languageCode)
        }
    }
}*/
