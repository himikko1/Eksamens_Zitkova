package com.example.myapplication.StepCounter

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.models.StepCounterViewModel

@Composable
fun StepCounterScreen(viewModel: StepCounterViewModel) {
    // Get the current LifecycleOwner's lifecycle
    val lifecycleOwner = LocalLifecycleOwner.current

    // Collect the StateFlow as a State with lifecycle awareness
    // StateFlow already has an initial value, so no need to provide initialValue here
    val steps by viewModel.stepCount.collectAsStateWithLifecycle(
        lifecycleOwner.lifecycle // Pass the lifecycle
        // Remove initialValue = 0
    )

    Surface {
        Text(
            text = "Шагов: $steps",
            style = MaterialTheme.typography.headlineLarge
        )
    }
}