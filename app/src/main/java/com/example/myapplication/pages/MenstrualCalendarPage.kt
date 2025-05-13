package com.example.myapplication.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.components.MenstrualCalendar
import com.example.myapplication.models.AuthViewModel
import com.example.myapplication.models.MenstrualCalendarViewModel

@Composable
fun MenstrualCalendarPage(
    navController: NavController,
    authViewModel: AuthViewModel,
    menstrualCalendarViewModel: MenstrualCalendarViewModel,
    modifier: Modifier = Modifier
) {
    // Load user data when authenticated
    LaunchedEffect(authViewModel.authState.value) {
        val authState = authViewModel.authState.value
        if (authState is AuthViewModel.AuthState.Authenticated) {
            authViewModel.getCurrentUserId()?.let { userId ->
                menstrualCalendarViewModel.loadUserData(userId)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Atpakaļ"
                )
            }

            Text(
                text = "Kalendārs",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Calendar component
        MenstrualCalendar(
            viewModel = menstrualCalendarViewModel,
            modifier = Modifier.fillMaxWidth()
        )
    }
}