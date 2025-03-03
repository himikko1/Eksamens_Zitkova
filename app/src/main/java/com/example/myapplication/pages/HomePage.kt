package com.example.myapplication.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.AuthViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.myapplication.R // Ensure this import is correct
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val authState = authViewModel.authState.observeAsState()

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthViewModel.AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Hello, ", fontSize = 32.sp)

        ElevatedButton(onClick = {
            authViewModel.signOut()
        }) {
            Text(text = "Izlogoties")
        }

        // funkcijas uzsauksana , lai redzetu todo listu
        TodoList()
    }
}

//funkcija, kura pievieno
@Composable
fun TodoList() {
    val todoList = getFakeTodos()
    var inputText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(8.dp)
    ) {
        Row {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Pievienot jaunu uzdevumu") },
                modifier = Modifier.weight(1f)
            )
            Button(onClick = {
            }) {
                Text(text = "Pievienot")
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .padding(8.dp)
        ) {
            itemsIndexed(todoList) { index: Int, item: Todo ->
                TodoItem(item = item) // izvada visus item
            }
        }
    }
}

@Composable
fun TodoItem(item: Todo) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                SimpleDateFormat("HH:mm:aa, dd/MM", Locale.ENGLISH).format(item.createdAt),
                fontSize = 12.sp,
                color = Color.LightGray
            )
            Text(
                text = item.title,
                fontSize = 20.sp,
                color = Color.White
            )
        }
        IconButton(onClick = { /* Handle delete action */ }) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_delete_outline_24), // ikona
                contentDescription = "Delete",
                tint = Color.White
            )
        }
    }
}