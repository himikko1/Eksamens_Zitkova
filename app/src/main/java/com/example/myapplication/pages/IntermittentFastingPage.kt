package com.example.myapplication.pages

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.FastingService // Import the service
import kotlinx.coroutines.flow.StateFlow // Import StateFlow
import java.io.Serializable // Import Serializable for FastingMode


enum class FastingMode(val eatHours: Int, val fastHours: Int, val displayName: String) : Serializable {
    MODE_20_4(4, 20, "20/4"),
    MODE_16_8(8, 16, "16/8"),
    MODE_18_6(6, 18, "16/8") // Corrected display name if needed
}

enum class TimerState {
    IDLE,
    RUNNING,
    PAUSED
}

enum class FastingState {
    EATING,
    FASTING
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntermittentFastingPage(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    // Observe state directly from the service's companion object flows
    val selectedMode by FastingService.currentMode.collectAsState()
    val timerState by FastingService.timerState.collectAsState()
    val remainingTime by FastingService.remainingTimeSeconds.collectAsState()
    val fastingState by FastingService.fastingState.collectAsState()

    val context = LocalContext.current

    // Effect to set the mode in the service when selected in UI
    // This handles cases where the app is started and a mode is already set in the service
    // or when the user selects a new mode.
    LaunchedEffect(selectedMode) {
        if (selectedMode != null && FastingService.currentMode.value != selectedMode) {
            val serviceIntent = Intent(context, FastingService::class.java).apply {
                action = FastingService.ACTION_SET_MODE
                putExtra(FastingService.EXTRA_FASTING_MODE, selectedMode)
            }
            context.startService(serviceIntent)
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Intermittent Fasting") },
                navigationIcon = {
                    IconButton(onClick = {
                        // Decide how 'Back' should behave.
                        // If timer is running, maybe just navigate up without stopping service?
                        // For now, just navigate up.
                        navController.navigateUp()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (selectedMode == null) {
                ModeSelectionScreen(onModeSelected = { mode ->
                    // Send intent to service to set the mode
                    val serviceIntent = Intent(context, FastingService::class.java).apply {
                        action = FastingService.ACTION_SET_MODE
                        putExtra(FastingService.EXTRA_FASTING_MODE, mode)
                    }
                    context.startService(serviceIntent)
                    // No longer need to update local selectedMode state here,
                    // as the UI observes the service's state.
                })
            } else {
                // Pass observed states and callbacks to send intents
                FastingTimerScreen(
                    fastingMode = selectedMode!!, // selectedMode will not be null here
                    timerState = timerState,
                    fastingState = fastingState,
                    remainingTimeSeconds = remainingTime,
                    onStartTimer = {
                        val serviceIntent = Intent(context, FastingService::class.java).apply {
                            action = FastingService.ACTION_START
                        }
                        context.startService(serviceIntent)
                    },
                    onPauseTimer = {
                        val serviceIntent = Intent(context, FastingService::class.java).apply {
                            action = FastingService.ACTION_PAUSE
                        }
                        context.startService(serviceIntent)
                    },
                    onResetTimer = {
                        val serviceIntent = Intent(context, FastingService::class.java).apply {
                            action = FastingService.ACTION_RESET
                        }
                        context.startService(serviceIntent)
                        // Navigating back to selection is handled by the reset action in service
                        // and the UI observing the null currentMode state.
                    },
                    onBackToSelection = {
                        // If the user wants to change mode while timer is running,
                        // maybe reset the service first, then the UI will show selection.
                        val serviceIntent = Intent(context, FastingService::class.java).apply {
                            action = FastingService.ACTION_RESET // Reset also stops the service
                        }
                        context.startService(serviceIntent)
                        // The UI observes currentMode becoming null and switches screen
                    }
                )
            }
        }
    }
}

@Composable
fun ModeSelectionScreen(
    onModeSelected: (FastingMode) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Select Fasting Mode",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        FastingMode.values().forEach { mode ->
            Button(
                onClick = { onModeSelected(mode) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = mode.displayName,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "${mode.fastHours}h fast / ${mode.eatHours}h eat",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun FastingTimerScreen(
    fastingMode: FastingMode, // Passed for display purposes
    timerState: TimerState, // Observed state
    fastingState: FastingState, // Observed state
    remainingTimeSeconds: Int, // Observed state
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onBackToSelection: () -> Unit // Callback to change mode
) {
    // Format time as HH:MM:SS
    val hours = remainingTimeSeconds / 3600
    val minutes = (remainingTimeSeconds % 3600) / 60
    val seconds = remainingTimeSeconds % 60
    val timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    // Background color based on fasting state
    val backgroundColor = when (fastingState) {
        FastingState.EATING -> Color(0xFF4CAF50) // Green
        FastingState.FASTING -> Color(0xFFE57373) // Red
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = fastingMode.displayName,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = if (fastingState == FastingState.EATING)
                    "You can eat now! (${fastingMode.eatHours}h)"
                else
                    "Fasting period (${fastingMode.fastHours}h)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(250.dp)
                .background(backgroundColor.copy(alpha = 0.2f), shape = RoundedCornerShape(125.dp))
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (fastingState == FastingState.EATING) "EATING" else "FASTING",
                    style = MaterialTheme.typography.titleLarge,
                    color = backgroundColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = timeFormatted,
                    fontSize = 36.sp,
                    textAlign = TextAlign.Center
                )
                Text( // Display timer state
                    text = timerState.name,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onStartTimer,
                    enabled = timerState != TimerState.RUNNING && fastingMode != null, // Enable only if not running and mode is set
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Start")
                }

                Button(
                    onClick = onPauseTimer,
                    enabled = timerState == TimerState.RUNNING, // Enable only if running
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Pause")
                }
            }

            Button(
                onClick = onResetTimer,
                modifier = Modifier.fillMaxWidth(),
                enabled = timerState != TimerState.IDLE // Enable if timer has been started or paused
            ) {
                Text("Reset")
            }

            OutlinedButton(
                onClick = onBackToSelection,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Change Mode")
            }
        }
    }
}

// Helper to observe StateFlows in Composable
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun <T> StateFlow<T>.collectAsState(): State<T> {
    return collectAsState(initial = value)
}