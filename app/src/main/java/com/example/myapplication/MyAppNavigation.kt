package com.example.myapplication

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
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.pages.HomePage
import com.example.myapplication.pages.LoginPage
import com.example.myapplication.pages.SignupPage
import com.example.myapplication.ui.theme.SettingsPage

// Define navigation items for bottom navigation
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
fun MyAppNavigation(modifier: Modifier = Modifier, authViewModel: AuthViewModel) {
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
            // Show bottom navigation only when authenticated
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
                                    // Pop up to the start destination of the graph to
                                    // avoid building up a large stack of destinations
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // Avoid multiple copies of the same destination
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

            // Main screens (with bottom navigation)
            composable(BottomNavItem.Home.route) {
                HomePage(modifier, navController, authViewModel)
            }

            // Profile page - using temporary implementation until ProfilePage is properly created
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
        }
    }
}