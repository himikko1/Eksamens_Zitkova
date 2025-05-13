package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.models.AuthViewModel
import com.example.myapplication.models.BmiViewModel
import com.example.myapplication.models.CalorieCalculatorViewModel
import com.example.myapplication.models.NotificationUtils
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.ThemePreferences
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val todoViewModel: TodoViewModel by viewModels()
    private val bmiViewModel: BmiViewModel by viewModels()
    private val calorieCalculatorViewModel: CalorieCalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NotificationUtils.createNotificationChannel(applicationContext)

        // Инициализация Firebase
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Ошибка инициализации Firebase: ${e.message}")
        }

        val themePreferences = ThemePreferences(this)

        enableEdgeToEdge()
        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                themePreferences.isDarkTheme.collect { theme ->
                    isDarkTheme = theme
                }
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MyAppNavigation(
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
}
