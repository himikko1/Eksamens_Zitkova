// MyApplication.kt
package com.example.myapplication

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import com.example.myapplication.ui.theme.LanguagePreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Locale

class MyApplication : Application() {

    private lateinit var languagePreferences: LanguagePreferences
    // Store the current app locale, accessible by other components
    private var currentLocale: Locale? = null

    override fun attachBaseContext(base: Context) {
        languagePreferences = LanguagePreferences(base)

        val savedLanguageCode = runBlocking {
            languagePreferences.getLanguage.first()
        }

        currentLocale = Locale(savedLanguageCode.toString())
        // Set default for non-Android framework calls that don't take a Context
        Locale.setDefault(currentLocale!!) // This sets the JVM-level default locale

        // !!! IMPORTANT: Call super.attachBaseContext with the ORIGINAL 'base' context.
        // This is crucial for preventing ClassCastException during system component (like BroadcastReceiver) instantiation.
        super.attachBaseContext(base)

        // *** REMOVE THE PREVIOUS applyOverrideConfiguration OVERRIDE ***
        // It's not a direct overrideable method for Application, and handling is slightly different.
        // The Application's resources *might* update themselves if the system configuration changes
        // or if you explicitly call updateConfiguration on its resources.
        // However, for most modern Android apps, especially with Compose, the locale is primarily
        // managed at the Activity level where resources are loaded.
        // The fact that this line caused "overrides nothing" confirms it's not the right place.
    }

    override fun onCreate() {
        super.onCreate()
        // Your other app-level initializations (Firebase, etc.)
    }

    // Expose language preferences for Activities to get the saved language
    fun getLanguagePreferences(): LanguagePreferences {
        return languagePreferences
    }

    // Optional: If you need to expose the currently set locale from the Application,
    // though Activities will get their own locale from their wrapped context.
    fun getCurrentAppLocale(): Locale? {
        return currentLocale
    }
}