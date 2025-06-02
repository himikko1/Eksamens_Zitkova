package com.example.myapplication

import PhotoManager
import TrainingVideosPage
import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext // Keep this for context, but not for ViewModel factories
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.StepCounter.StepCounterScreen
import com.example.myapplication.models.AuthViewModel
import com.example.myapplication.models.BmiViewModel
import com.example.myapplication.models.CalorieCalculatorViewModel
import com.example.myapplication.models.MenstrualCalendarViewModel
import com.example.myapplication.models.StepCounterViewModel
import com.example.myapplication.models.ThemeViewModel
//import com.example.myapplication.models.TodoViewModel
import com.example.myapplication.models.WaterViewModel
import com.example.myapplication.pages.CalorieCalculatorPage
import com.example.myapplication.pages.CalorieHistoryPage
import com.example.myapplication.pages.HomePage
import com.example.myapplication.pages.IntermittentFastingPage
import com.example.myapplication.pages.LoginPage
import com.example.myapplication.pages.MenstrualCalendarPage
import com.example.myapplication.pages.SettingsPage
import com.example.myapplication.pages.SignupPage
import com.example.myapplication.pages.WaterTrackerPage


sealed class BottomNavItem(val route: String, val icon: @Composable () -> Unit, val label: String) {
    object Home : BottomNavItem(
        route = "home",
        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
        label = "Home"
    )

    object Profile : BottomNavItem(
        route = "profile",
        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
        label = "Profile"
    )

    object Settings : BottomNavItem(
        route = "settings",
        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
        label = "Settings"
    )
}

@Composable
fun MyAppNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    todoViewModel: TodoViewModel,
    bmiViewModel: BmiViewModel,
    calorieCalculatorViewModel: CalorieCalculatorViewModel,
    themeViewModel: ThemeViewModel
) {
    val navController = rememberNavController()
    // No longer need to manually cast LocalContext.current.applicationContext as Application
    // val context = LocalContext.current.applicationContext as Application

    // Observe authentication state
    val authState by authViewModel.authState.observeAsState(AuthViewModel.AuthState.Loading)
    val isAuthenticated = authState is AuthViewModel.AuthState.Authenticated

    // List of bottom navigation items
    val bottomNavItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Profile,
        BottomNavItem.Settings
    )

    Scaffold(
        bottomBar = {
            if (isAuthenticated) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { item.icon() },
                            label = { Text(item.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isAuthenticated) "home" else "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            // Auth screens
            composable("login") {
                LoginPage(modifier, navController, authViewModel)
            }
            composable("signup") {
                SignupPage(modifier, navController, authViewModel)
            }

            composable(BottomNavItem.Home.route) {
                HomePage(modifier, navController, authViewModel, todoViewModel, bmiViewModel)
            }

            composable("step_counter") {
                val stepCounterViewModel: StepCounterViewModel = viewModel()
                StepCounterScreen(viewModel = stepCounterViewModel)
            }

            composable(BottomNavItem.Profile.route) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser

                        val userName = when {
                            !user?.displayName.isNullOrEmpty() -> user?.displayName
                            !user?.email.isNullOrEmpty() -> user?.email?.substringBefore('@')
                            else -> "User"
                        }

                        Text("Profile Page", fontSize = 24.sp)
                        Text(
                            text = "Welcome, $userName!",
                            fontSize = 20.sp,
                            modifier = Modifier.padding(top = 16.dp)
                        )

                        Text(
                            text = "Email: ${user?.email ?: "Not available"}",
                            fontSize = 16.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        PhotoManager()

                        androidx.compose.material3.Button(
                            onClick = {
                                authViewModel.signOut()
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            modifier = Modifier.padding(top = 32.dp)
                        ) {
                            Text("Выйти")
                        }
                    }
                }
            }

            composable(BottomNavItem.Settings.route) {
                SettingsPage(
                    modifier = modifier,
                    navController = navController,
                    authViewModel = authViewModel,
                    themeViewModel = themeViewModel
                )
            }

            composable("calorie_calculator") {
                CalorieCalculatorPage(
                    modifier,
                    navController,
                    calorieCalculatorViewModel = calorieCalculatorViewModel
                )
            }

            composable("calorie_history") {
                CalorieHistoryPage(
                    modifier,
                    navController,
                    calorieCalculatorViewModel = calorieCalculatorViewModel
                )
            }

            composable("water_tracker") {
                // Initialize WaterViewModel without a factory
                val waterViewModel: WaterViewModel = viewModel()
                WaterTrackerPage(modifier, navController, waterViewModel)
            }

            composable("intermittent_fasting") {
                IntermittentFastingPage(modifier, navController)
            }

            composable("menstrual_calendar") {
                // Initialize MenstrualCalendarViewModel without a factory
                val menstrualCalendarViewModel: MenstrualCalendarViewModel = viewModel()
                MenstrualCalendarPage(
                    navController = navController,
                    authViewModel = authViewModel,
                    menstrualCalendarViewModel = menstrualCalendarViewModel
                )
            }

            composable("training_videos") {
                TrainingVideosPage(
                    navController = navController
                )
            }
        }
    }
}