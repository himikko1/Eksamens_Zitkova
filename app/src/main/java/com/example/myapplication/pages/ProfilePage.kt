package com.example.myapplication.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.models.AuthViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfilePage(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel) {

    val user = FirebaseAuth.getInstance().currentUser


    val userName = when {
        !user?.displayName.isNullOrEmpty() -> user?.displayName
        !user?.email.isNullOrEmpty() -> user?.email?.substringBefore('@')
        else -> "User"
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Profile",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))


        Text(
            text = "Welcome, $userName!",
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Email: ${user?.email ?: "Not available"}",
            fontSize = 16.sp
        )

        //PhotoManager(profileViewModel)

        Spacer(modifier = Modifier.height(48.dp))



        Button(
            onClick = {
                authViewModel.signOut()
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            }
        ) {
            Text("Sign Out")
        }

        Button(
            onClick = { navController.navigate("calorie_calculator") },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Калькулятор калорий")
        }
    }
}

