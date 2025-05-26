@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.myapplication.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.models.AuthViewModel
import com.example.myapplication.models.BmiViewModel
import com.example.myapplication.Todo
import com.example.myapplication.TodoViewModel
//import com.example.myapplication.viewmodel.WaterViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    todoViewModel: TodoViewModel,
    bmiViewModel: BmiViewModel
    //waterViewModel: WaterViewModel
) {
    val authState = authViewModel.authState.observeAsState()
    val error = todoViewModel.error.observeAsState()

    // Rādīt kļūdu snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthViewModel.AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    // Rādīt kļūdu snackbar, ja tāda ir
    LaunchedEffect(error.value) {
        error.value?.let {
            snackbarHostState.showSnackbar(
                message = it,
                actionLabel = "Dismiss",
                duration = SnackbarDuration.Short
            )
            todoViewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Izlogoties") },
                actions = {
                    IconButton(onClick = { authViewModel.signOut() }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Logout"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {

            BmiCalculator(
                bmiViewModel = bmiViewModel,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            Button(
                onClick = { navController.navigate("water_tracker") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Ūdens skaitītājs 💧")
            }
            Button(
                onClick = { navController.navigate("step_counter") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Soļu skaitītājs")
            }

            TodoListOriginal(
                viewModel = todoViewModel,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
fun BmiCalculator(
    bmiViewModel: BmiViewModel,
    modifier: Modifier = Modifier
) {
    val height by bmiViewModel.height.observeAsState(170f)
    val weight by bmiViewModel.weight.observeAsState(70f)
    val bmiResult by bmiViewModel.bmiResult.observeAsState(0f)
    val bmiCategory by bmiViewModel.bmiCategory.observeAsState("")

    Card(
        modifier = modifier
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Kalkulators KMI",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // izvada augumu
            Text(
                text = "Augums: ${height.toInt()} см",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // slider prieks augumu
            Slider(
                value = height,
                onValueChange = { bmiViewModel.updateHeight(it) },
                valueRange = 100f..220f,
                steps = 120,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // izvada svaru
            Text(
                text = "Svars: ${weight.toInt()} kg",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // slaider prieks svaru
            Slider(
                value = weight,
                onValueChange = { bmiViewModel.updateWeight(it) },
                valueRange = 30f..150f,
                steps = 120,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // rezultats
            BmiResultDisplay(bmiResult, bmiCategory)
        }
    }
}

@Composable
fun BmiResultDisplay(bmiResult: Float, bmiCategory: String) {
    val backgroundColor = when {
        bmiResult < 18.5 -> Color(0xFFF5F5DC)
        bmiResult < 25 -> Color(0xFFDEF1D8)
        bmiResult < 30 -> Color(0xFFFFE5B4)
        else -> Color(0xFFFFCCCB)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Jūsu ĶMI",
                color = Color.Black,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = String.format("%.1f", bmiResult),
                color = Color.Black,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = bmiCategory,
                color = Color.Black,
                style = MaterialTheme.typography.titleMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}


@Composable
fun TodoListOriginal(viewModel: TodoViewModel, modifier: Modifier = Modifier) {
    val todoList by viewModel.todoList.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    var inputText by remember { mutableStateOf("") }
    var editingTodo by remember { mutableStateOf<Todo?>(null) }

    Column(modifier = modifier) {
        // Input sekcijā
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (editingTodo == null) "Pievienot jaunu uzdevumu" else "Rediģēt uzdevumu",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text("Uzdevuma nosaukums") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (editingTodo != null) {
                        OutlinedButton(
                            onClick = {
                                editingTodo = null
                                inputText = ""
                            },
                            modifier = Modifier.padding(end = 8.dp),
                            enabled = !isLoading
                        ) {
                            Text("Atcelt")
                        }
                    }

                    Button(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                if (editingTodo != null) {
                                    // Update existing todo
                                    viewModel.updateTodo(editingTodo!!.copy(title = inputText))
                                    editingTodo = null
                                } else {
                                    // Add new todo
                                    viewModel.addTodo(inputText)
                                }
                                inputText = ""
                            }
                        },
                        enabled = !isLoading && inputText.isNotBlank()
                    ) {
                        Text(if (editingTodo == null) "Pievienot" else "Saglabāt")
                    }
                }
            }
        }

        // loading indikators
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // Todo list
        if (!isLoading && todoList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nav uzdevumu. Pievienojiet savu pirmo uzdevumu!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else if (!isLoading) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                userScrollEnabled = true
            ) {
                itemsIndexed(todoList) { _, item ->
                    TodoItem(
                        item = item,
                        onEdit = {
                            editingTodo = it
                            inputText = it.title
                        },
                        onDelete = { viewModel.deleteTodo(it.id) },
                        onToggleComplete = { viewModel.toggleTodoCompletion(it) },
                        isEnabled = !isLoading
                    )
                }
            }
        }
    }
}

@Composable
fun TodoItem(
    item: Todo,
    onEdit: (Todo) -> Unit,
    onDelete: (Todo) -> Unit,
    onToggleComplete: (Todo) -> Unit,
    isEnabled: Boolean = true
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox ja pabeidza
            Checkbox(
                checked = item.completed,
                onCheckedChange = { onToggleComplete(item) },
                modifier = Modifier.padding(end = 8.dp),
                enabled = isEnabled
            )


            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (item.completed) TextDecoration.LineThrough else TextDecoration.None
                )

                Text(
                    text = SimpleDateFormat("HH:mm, dd.MM.yyyy", Locale.getDefault()).format(item.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // Action buttons
            Row {
                IconButton(
                    onClick = { onEdit(item) },
                    enabled = isEnabled
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }

                IconButton(
                    onClick = { onDelete(item) },
                    enabled = isEnabled
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = if (isEnabled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}