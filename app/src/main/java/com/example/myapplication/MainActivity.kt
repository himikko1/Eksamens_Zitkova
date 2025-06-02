// MainActivity.kt
package com.example.myapplication

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.myapplication.models.AuthViewModel
import com.example.myapplication.models.BmiViewModel
import com.example.myapplication.models.CalorieCalculatorViewModel
import com.example.myapplication.models.NotificationUtils
import com.example.myapplication.models.ThemeViewModel
//import com.example.myapplication.models.TodoViewModel // Make sure this import is UNCOMMENTED
// Removed explicit imports for WaterViewModel, MenstrualCycleViewModel here as they are created in MyAppNavigation
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Locale

class MainActivity : ComponentActivity() { // Keep ComponentActivity

    // Reverting to simpler by viewModels() for ViewModels that don't need application context
    private val authViewModel: AuthViewModel by viewModels()
    private val todoViewModel: TodoViewModel by viewModels() // If TodoViewModel needs Application, adjust its constructor or pass it differently.
    private val bmiViewModel: BmiViewModel by viewModels()
    private val calorieCalculatorViewModel: CalorieCalculatorViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()

    // WaterViewModel and MenstrualCalendarViewModel will be initialized in MyAppNavigation using viewModel()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("MainActivity", "POST_NOTIFICATIONS permission granted")
            } else {
                Log.w("MainActivity", "POST_NOTIFICATIONS permission denied")
            }
        }

    override fun attachBaseContext(newBase: Context?) {
        val app = newBase?.applicationContext as? MyApplication
        val languagePreferences = app?.getLanguagePreferences()

        val savedLanguageCode = languagePreferences?.let {
            runBlocking {
                it.getLanguage.first()
            }
        } ?: Locale.getDefault().language

        val context = LocaleContextWrapper.wrap(newBase!!, savedLanguageCode.toString())
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationPermission()

        java.util.Locale.setDefault(
            java.util.Locale.forLanguageTag("lv")
        )

        // Create all notification channels when the app starts
        NotificationUtils.createWaterReminderChannel(applicationContext) // For water reminder
        NotificationUtils.createMenstrualReminderChannel(applicationContext) // For menstrual reminder

        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error initializing Firebase: ${e.message}")
        }

        setContent {
            val isDarkThemeActive by themeViewModel.isDarkTheme.collectAsState(initial = false)

            MyApplicationTheme(darkTheme = isDarkThemeActive) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MyAppNavigation(
                        modifier = Modifier.padding(innerPadding),
                        authViewModel = authViewModel,
                        todoViewModel = todoViewModel,
                        bmiViewModel = bmiViewModel,
                        calorieCalculatorViewModel = calorieCalculatorViewModel,
                        themeViewModel = themeViewModel
                        // WaterViewModel and MenstrualCycleViewModel are created within MyAppNavigation
                    )
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d("MainActivity", "POST_NOTIFICATIONS permission already granted")
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    Log.d("MainActivity", "Showing rationale for POST_NOTIFICATIONS permission")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    Log.d("MainActivity", "Requesting POST_NOTIFICATIONS permission")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}