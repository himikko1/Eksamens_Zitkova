package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope // Make sure this is imported if you use it elsewhere
import com.example.myapplication.models.AuthViewModel
import com.example.myapplication.models.BmiViewModel
import com.example.myapplication.models.CalorieCalculatorViewModel
import com.example.myapplication.models.NotificationUtils // Assuming this is still needed for water reminders
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.ThemePreferences
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.launch

// Assuming TodoViewModel and MyAppNavigation exist in your project
// import com.example.myapplication.models.TodoViewModel
// import com.example.myapplication.MyAppNavigation


class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val todoViewModel: TodoViewModel by viewModels() // Make sure this is imported
    private val bmiViewModel: BmiViewModel by viewModels()
    private val calorieCalculatorViewModel: CalorieCalculatorViewModel by viewModels()

    // Declare the permission request launcher as a member of the Activity
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. You can now post notifications.
                Log.d("MainActivity", "POST_NOTIFICATIONS permission granted")
                // If you have a ViewModel that manages notification state,
                // you might update it here.
            } else {
                // Permission is denied.
                Log.w("MainActivity", "POST_NOTIFICATIONS permission denied")
                // Optionally, show a message to the user explaining why notifications are needed
                // and how to enable them in settings.
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- Start: Notification Permission Request ---
        // Request the notification permission when the activity is created
        requestNotificationPermission()
        // --- End: Notification Permission Request ---

        // Ensure the notification channel for Water Reminders is created (if still needed)
        NotificationUtils.createNotificationChannel(applicationContext)

        // Initialization Firebase
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Ошибка инициализации Firebase: ${e.message}")
        }

        val themePreferences = ThemePreferences(this)

        //enableEdgeToEdge()
        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                themePreferences.isDarkTheme.collect { theme ->
                    isDarkTheme = theme
                }
            }

            MyApplicationTheme(darkTheme = isDarkTheme) { // Replace with your theme
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MyAppNavigation( // Make sure MyAppNavigation is imported and available
                        modifier = Modifier.padding(innerPadding),
                        authViewModel = authViewModel,
                        todoViewModel = todoViewModel,
                        bmiViewModel = bmiViewModel,
                        calorieCalculatorViewModel = calorieCalculatorViewModel,
                        isDarkTheme = isDarkTheme,
                        onThemeToggle = { newTheme ->
                            lifecycleScope.launch {
                                themePreferences.setDarkTheme(newTheme)
                            }
                        }
                    )
                }
            }
        }
    }

    // Function to check and request the POST_NOTIFICATIONS permission
    private fun requestNotificationPermission() {
        // Check if the Android version is Tiramisu (API 33) or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                // Check if permission is already granted
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission is already granted, proceed normally
                    Log.d("MainActivity", "POST_NOTIFICATIONS permission already granted")
                }
                // Check if we should show a rationale for why permission is needed
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // This case is for when the user has previously denied the permission.
                    // You should show an educational UI (like a dialog) here to explain
                    // why the permission is needed before requesting it again.
                    // For this example, we'll just launch the request directly, but
                    // showing a rationale is better UX.
                    Log.d("MainActivity", "Showing rationale for POST_NOTIFICATIONS permission")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                // Directly request the permission (first time asking or after "Don't ask again")
                else -> {
                    Log.d("MainActivity", "Requesting POST_NOTIFICATIONS permission")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
        // For versions below API 33, the permission is granted via manifest declaration
        // and no runtime request is needed.
    }

    // Make sure your other methods like onResume, onPause, etc. are here if you had them
}