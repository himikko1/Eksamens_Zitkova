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
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.models.AuthViewModel
import com.example.myapplication.models.BmiViewModel
import com.example.myapplication.Todo
import com.example.myapplication.TodoViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    todoViewModel: TodoViewModel,
    bmiViewModel: BmiViewModel
) {
    val authState = authViewModel.authState.observeAsState()
    val error = todoViewModel.error.observeAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthViewModel.AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

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
            CenterAlignedTopAppBar( // Changed to CenterAlignedTopAppBar for better aesthetics
                title = { Text("Mājas Lapa", style = MaterialTheme.typography.titleLarge) }, // Stronger title
                actions = {
                    IconButton(onClick = { authViewModel.signOut() }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer // Use theme color
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
                .padding(horizontal = 16.dp) // Increased horizontal padding for better breathing room
                .verticalScroll(rememberScrollState()), // Allows the whole screen to scroll
            horizontalAlignment = Alignment.CenterHorizontally // Center content horizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp)) // Space before first card

            // BMI Calculator Section
            BmiCalculator(
                bmiViewModel = bmiViewModel,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Activity Trackers Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface) // Use surface color for card background
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Aktivitātes",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround, // Space out buttons
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Water Tracker Button
                        ElevatedButton( // Elevated button for a more distinct look
                            onClick = { navController.navigate("water_tracker") },
                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Default.LocalDrink, contentDescription = "Water", modifier = Modifier.size(24.dp))
                                Spacer(Modifier.height(4.dp))
                                Text("Ūdens", style = MaterialTheme.typography.bodyLarge)
                            }
                        }

                        // Step Counter Button
                        ElevatedButton( // Elevated button for a more distinct look
                            onClick = { navController.navigate("step_counter") },
                            modifier = Modifier.weight(1f).padding(start = 8.dp),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Default.DirectionsRun, contentDescription = "Steps", modifier = Modifier.size(24.dp))
                                Spacer(Modifier.height(4.dp))
                                Text("Soļi", style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }


            // Todo List Section
            TodoList( // Renamed TodoListOriginal for consistency
                viewModel = todoViewModel,
                modifier = Modifier
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp)) // Space at the bottom
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface) // Use surface color for card background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Ķermeņa masas indekss (ĶMI)", // More descriptive title
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), // Bolder title
                modifier = Modifier.padding(bottom = 16.dp),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Height input
            Text(
                text = "Augums: ${height.toInt()} cm",
                style = MaterialTheme.typography.bodyLarge, // Larger text for values
                modifier = Modifier.padding(bottom = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = height,
                onValueChange = { bmiViewModel.updateHeight(it) },
                valueRange = 100f..220f,
                steps = 120,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Weight input
            Text(
                text = "Svars: ${weight.toInt()} kg",
                style = MaterialTheme.typography.bodyLarge, // Larger text for values
                modifier = Modifier.padding(bottom = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = weight,
                onValueChange = { bmiViewModel.updateWeight(it) },
                valueRange = 30f..150f,
                steps = 120,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // BMI Result Display
            BmiResultDisplay(bmiResult, bmiCategory)
        }
    }
}

@Composable
fun BmiResultDisplay(bmiResult: Float, bmiCategory: String) {
    // Defined a more Material Design friendly color palette for BMI categories
    val backgroundColor = when {
        bmiResult < 18.5 -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f) // Underweight
        bmiResult < 25 -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f) // Normal weight
        bmiResult < 30 -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f) // Overweight
        else -> MaterialTheme.colorScheme.error.copy(alpha = 0.6f) // Obesity
    }
    val contentColor = when {
        bmiResult < 18.5 -> MaterialTheme.colorScheme.onSecondaryContainer
        bmiResult < 25 -> MaterialTheme.colorScheme.onTertiaryContainer
        bmiResult < 30 -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onError
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Jūsu ĶMI",
                color = contentColor,
                style = MaterialTheme.typography.titleSmall, // Slightly smaller title
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = String.format("%.1f", bmiResult),
                color = contentColor,
                style = MaterialTheme.typography.headlineLarge, // Larger and more prominent BMI value
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = bmiCategory,
                color = contentColor,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
fun TodoList(viewModel: TodoViewModel, modifier: Modifier = Modifier) { // Renamed TodoListOriginal
    val todoList by viewModel.todoList.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    var inputText by remember { mutableStateOf("") }
    var editingTodo by remember { mutableStateOf<Todo?>(null) }

    Column(modifier = modifier) {
        // Input section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (editingTodo == null) "Pievienot jaunu uzdevumu" else "Rediģēt uzdevumu",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text("Uzdevuma nosaukums") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp), // Increased top padding for buttons
                    horizontalArrangement = Arrangement.End
                ) {
                    if (editingTodo != null) {
                        TextButton( // TextButton for cancel
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
                                    viewModel.updateTodo(editingTodo!!.copy(title = inputText))
                                    editingTodo = null
                                } else {
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

        // Loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp), // More padding for loader
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary // Use theme primary color
                )
            }
        }

        // Todo list or empty state
        if (!isLoading && todoList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nav uzdevumu.\nPievienojiet savu pirmo uzdevumu!", // Multiline for better readability
                    style = MaterialTheme.typography.titleMedium, // Stronger text for empty state
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center
                )
            }
        } else if (!isLoading) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp), // Use heightIn for better responsiveness
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant) // Slightly different color for list items
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.completed,
                onCheckedChange = { onToggleComplete(item) },
                modifier = Modifier.size(24.dp), // Fixed size for checkbox
                enabled = isEnabled,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.width(16.dp)) // Increased space

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall, // Stronger text for title
                    textDecoration = if (item.completed) TextDecoration.LineThrough else TextDecoration.None,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = SimpleDateFormat("HH:mm, dd.MM.yyyy", Locale.getDefault()).format(item.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) // Slightly faded timestamp
                )
            }

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