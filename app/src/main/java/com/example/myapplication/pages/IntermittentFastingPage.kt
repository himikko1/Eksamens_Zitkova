package com.example.myapplication.pages

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

enum class FastingMode(val eatHours: Int, val fastHours: Int, val displayName: String) {
    MODE_20_4(4, 20, "20/4"),
    MODE_16_8(8, 16, "16/8"),
    MODE_18_6(6, 18, "18/6")
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
    var selectedMode by remember { mutableStateOf<FastingMode?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Intermittent Fasting") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
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
                    selectedMode = mode
                })
            } else {
                FastingTimerScreen(
                    fastingMode = selectedMode!!,
                    onBackToSelection = { selectedMode = null }
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
    fastingMode: FastingMode,
    onBackToSelection: () -> Unit
) {
    var timerState by remember { mutableStateOf(TimerState.IDLE) }
    var fastingState by remember { mutableStateOf(FastingState.EATING) }

    // Timer values in seconds
    val eatTimeSeconds = fastingMode.eatHours * 3600
    val fastTimeSeconds = fastingMode.fastHours * 3600

    var remainingTimeSeconds by remember { mutableStateOf(
        if (fastingState == FastingState.EATING) eatTimeSeconds else fastTimeSeconds
    ) }

    // Timer effect
    LaunchedEffect(timerState, fastingState) {
        if (timerState == TimerState.RUNNING) {
            while (remainingTimeSeconds > 0) {
                delay(1000)
                remainingTimeSeconds--
            }

            // Switch states when timer completes
            if (remainingTimeSeconds <= 0) {
                if (fastingState == FastingState.EATING) {
                    fastingState = FastingState.FASTING
                    remainingTimeSeconds = fastTimeSeconds
                } else {
                    fastingState = FastingState.EATING
                    remainingTimeSeconds = eatTimeSeconds
                }
            }
        }
    }

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
                    onClick = {
                        timerState = TimerState.RUNNING
                    },
                    enabled = timerState != TimerState.RUNNING,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Start")
                }

                Button(
                    onClick = {
                        timerState = TimerState.PAUSED
                    },
                    enabled = timerState == TimerState.RUNNING,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Pause")
                }
            }

            Button(
                onClick = {
                    fastingState = FastingState.EATING
                    timerState = TimerState.IDLE
                    remainingTimeSeconds = eatTimeSeconds
                },
                modifier = Modifier.fillMaxWidth()
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