package com.example.myapplication

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

class LocaleContextWrapper(base: Context) : ContextWrapper(base) {

    companion object {
        fun wrap(context: Context, language: String): ContextWrapper {
            var newContext = context
            val config = newContext.resources.configuration
            val locale = Locale(language) // Use Locale(language_code)

            // Set default locale for older APIs or if app isn't explicitly locale-aware for all components
            Locale.setDefault(locale)

            // Update configuration based on Android version
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                config.setLocales(LocaleList(locale))
            } else {
                @Suppress("DEPRECATION") // For older API compatibility
                config.locale = locale
            }

            // Apply the new configuration to the context
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                newContext = newContext.createConfigurationContext(config)
            } else {
                @Suppress("DEPRECATION") // For older API compatibility
                newContext.resources.updateConfiguration(config, newContext.resources.displayMetrics)
            }
            return LocaleContextWrapper(newContext)
        }
    }
}