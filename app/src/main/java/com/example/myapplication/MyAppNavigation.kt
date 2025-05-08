package com.example.myapplication

import PhotoManager
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.models.AuthViewModel
import com.example.myapplication.models.BmiViewModel
import com.example.myapplication.models.CalorieCalculatorViewModel
//import com.example.myapplication.models.SleepViewModel
import com.example.myapplication.pages.CalorieCalculatorPage
import com.example.myapplication.pages.CalorieHistoryPage
import com.example.myapplication.pages.HomePage
import com.example.myapplication.pages.IntermittentFastingPage
import com.example.myapplication.pages.LoginPage
import com.example.myapplication.pages.SignupPage
//import com.example.myapplication.pages.SleepTrackerPage
import com.example.myapplication.ui.theme.SettingsPage
import com.example.myapplication.pages.WaterTrackerPage
//import com.example.myapplication.viewmodel.WaterViewModel


// Definē navigation items uz bottom navigation
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
//    waterViewModel: WaterViewModel,
    calorieCalculatorViewModel: CalorieCalculatorViewModel,
//    sleepViewModel: SleepViewModel,
//    fragmentManager: FragmentManager,
) {
    val navController = rememberNavController()

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
            // parāda bottom nav tikai tad , kad auth
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
                                    // Pop up to the sākuma distanciju  grafā
                                    // izvarās no lielā stack distancijām
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // Izvairās no vienā un tā pašā vairākām kopijām
                                    launchSingleTop = true
                                    // Restore state when reselecting a previously selected item
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

            //galvēnie ekrāni (ar bottom navigation)
            composable(BottomNavItem.Home.route) {
                HomePage(modifier, navController, authViewModel, todoViewModel,  bmiViewModel/*, waterViewModel*/ )
            }

            // tiek izmantota *pagaidu* implementacija , līdz , kad ProfilePage tiek pareizi izvedoita
            composable(BottomNavItem.Profile.route) {
                // Temporary profile page implementation
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // dabuj user from firebase
                        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser

                        // izraksta lietotāja vārdu
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
                            Text("Iziet")
                        }
                    }
                }
            }

            // izmantoju jaunu iestatijumu lapu , nevis pagaidu lapu
            composable(BottomNavItem.Settings.route) {
                SettingsPage(modifier, navController, authViewModel)
            }


            composable("calorie_calculator") {
               CalorieCalculatorPage(modifier, navController,
                   calorieCalculatorViewModel = CalorieCalculatorViewModel()
               )
           }

            composable("calorie_history") {
                CalorieHistoryPage(modifier, navController,
                    calorieCalculatorViewModel = CalorieCalculatorViewModel()
                )
            }

            composable("water_tracker") {
                WaterTrackerPage(modifier, navController)
            }

            composable("intermittent_fasting") {
                IntermittentFastingPage(modifier, navController)
            }



//            composable("sleep_tracker") {
//                SleepTrackerPage(
//                    navController = navController,
//                    fragmentManager = fragmentManager, // Передаем FragmentManager
//                    sleepViewModel = sleepViewModel   // Передаем SleepViewModel
//                )
//            }
        }
    }
}