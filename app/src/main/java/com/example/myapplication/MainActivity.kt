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
import androidx.compose.ui.Modifier
import com.example.myapplication.models.AuthViewModel
import com.example.myapplication.models.BmiViewModel
import com.example.myapplication.models.CalorieCalculatorViewModel
import com.example.myapplication.models.NotificationUtils
//import com.example.myapplication.models.SleepViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val todoViewModel: TodoViewModel by viewModels()
    private val bmiViewModel: BmiViewModel by viewModels()
    // private val sleepViewModel: SleepViewModel by viewModels()
    private val calorieCalculatorViewModel: CalorieCalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NotificationUtils.createNotificationChannel(applicationContext)

        try {
            // Initialize Firebase
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to initialize Firebase: ${e.message}")
            // Consider showing a user-friendly error message
        }

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MyAppNavigation(
                        modifier = Modifier.padding(innerPadding),
                        authViewModel = authViewModel,
                        todoViewModel = todoViewModel,
                        bmiViewModel = bmiViewModel,
                        calorieCalculatorViewModel = calorieCalculatorViewModel
//                        waterViewModel = WaterViewModel(),
//                        sleepViewModel = sleepViewModel,
//                        fragmentManager = FragmentManager
                    )
                }
            }
        }
    }
}