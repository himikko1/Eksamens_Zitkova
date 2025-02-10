package com.example.myapplication.pages

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.AuthViewModel
import java.lang.reflect.Modifier

@Composable
fun SignupPage(
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    // Your composable content here
    var email by remember {
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
    }

    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(authState.value){
        when(authState.value){
            is AuthViewModel.AuthState.Authenticated -> navController.navigate("home")
            is AuthViewModel.AuthState.Error -> Toast.makeText(context, ((authState.value as AuthViewModel.AuthState.Error).message), Toast.LENGTH_SHORT).show()
            else -> Unit
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Signup Page", fontSize = 32.sp)

        Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
            },
            label = {
                Text(text = "Email")
            }
        )

        Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
            },
            label = {
                Text(text = "Password")
            }
        )

        Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
        ElevatedButton(onClick = {
                authViewModel.signup(email, password)
        },
            enabled = authState.value != AuthViewModel.AuthState.Loading){
            Text(text = "Create account")
        }

        Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
        TextButton(onClick = {
            navController.navigate("login")
        }){
            Text(text = "Already have an account, Login")
        }
    }
}