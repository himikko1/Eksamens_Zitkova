package com.example.myapplication.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
// import androidx.datastore.preferences.preferencesDataStoreFile // <-- REMOVE THIS IMPORT
import androidx.datastore.preferences.core.emptyPreferences

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.io.File // <-- ADD THIS IMPORT

// Define the name for your preferences DataStore
private val DATA_STORE_NAME = "language_preferences"
// Define the exact filename DataStore uses for preferences
private val DATA_STORE_FILE_NAME = DATA_STORE_NAME + ".preferences_pb"

// A private lateinit var to hold the single instance of DataStore.
private lateinit var languageDataStoreInstance: DataStore<Preferences>

/**
 * Provides a singleton instance of the Language DataStore.
 * This function uses the direct factory method, suitable for early initialization in Application.attachBaseContext().
 *
 * @param context The Context used to create the DataStore.
 * @return The singleton DataStore<Preferences> instance for language preferences.
 */
fun provideLanguageDataStore(context: Context): DataStore<Preferences> {
    // Check if the instance is already initialized.
    if (!::languageDataStoreInstance.isInitialized) {
        // Synchronize block for thread-safe initialization.
        synchronized(LanguagePreferences::class.java) {
            // Double-check inside the synchronized block.
            if (!::languageDataStoreInstance.isInitialized) {
                // Use the direct factory method: PreferenceDataStoreFactory.create()
                languageDataStoreInstance = PreferenceDataStoreFactory.create(
                    corruptionHandler = ReplaceFileCorruptionHandler(
                        produceNewData = { emptyPreferences() }
                    ),
                    migrations = listOf(),
                    scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
                    // FIX APPLIED HERE: Manually construct the File object for DataStore
                    // This bypasses the potentially problematic call to Context.getFilesDir()
                    // through the extension function during early app startup.
                    produceFile = { File(context.filesDir, "datastore/" + DATA_STORE_FILE_NAME) } // <--- CORRECTED LINE!
                )
            }
        }
    }
    return languageDataStoreInstance
}

class LanguagePreferences(context: Context) {
    // Get the DataStore instance using the controlled factory function provided above.
    private val dataStore: DataStore<Preferences> = provideLanguageDataStore(context)

    companion object {
        val DEFAULT_LANGUAGE_CODE = "en"
        private val LANGUAGE_KEY = stringPreferencesKey("app_language_code")
    }

    suspend fun setLanguage(languageCode: String) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = languageCode
        }
    }

    val getLanguage: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[LANGUAGE_KEY] ?: DEFAULT_LANGUAGE_CODE
        }
}