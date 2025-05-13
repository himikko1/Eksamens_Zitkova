package com.example.myapplication // Make sure this matches your app's package

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.pages.FastingMode // Import your FastingMode enum
import com.example.myapplication.pages.FastingState // Import your FastingState enum
import com.example.myapplication.pages.TimerState // Import your TimerState enum
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.Serializable // Required if FastingMode is Serializable

// Assuming your FastingMode enum is defined like this in pages/FastingMode.kt
// enum class FastingMode(val eatHours: Int, val fastHours: Int, val displayName: String) : Serializable { ... }


class FastingService : LifecycleService() {

    private var timerJob: Job? = null
    private var eatTimeSeconds: Int = 0
    private var fastTimeSeconds: Int = 0

    // Companion object to hold the StateFlows and static methods/constants
    companion object {
        // Internal MutableStateFlows - Moved into companion object
        private val _remainingTimeSeconds = MutableStateFlow(0)
        private val _timerState = MutableStateFlow(TimerState.IDLE)
        private val _fastingState = MutableStateFlow(FastingState.EATING)
        private val _currentMode = MutableStateFlow<FastingMode?>(null) // Represents the selected mode

        // Actions for Intents
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESET = "ACTION_RESET"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_SET_MODE = "ACTION_SET_MODE" // Action to set the fasting mode
        const val EXTRA_FASTING_MODE = "EXTRA_FASTING_MODE" // Extra key for the fasting mode

        // Notification Channel constants
        private const val NOTIFICATION_CHANNEL_ID = "fasting_timer_channel"
        private const val NOTIFICATION_CHANNEL_NAME = "Fasting Timer"
        private const val NOTIFICATION_ID = 1 // Must be a unique non-zero integer

        // Public read-only StateFlows for UI to observe
        val remainingTimeSeconds: StateFlow<Int> get() = _remainingTimeSeconds.asStateFlow()
        val timerState: StateFlow<TimerState> get() = _timerState.asStateFlow()
        val fastingState: StateFlow<FastingState> get() = _fastingState.asStateFlow()
        val currentMode: StateFlow<FastingMode?> get() = _currentMode.asStateFlow()

        // Helper function to format time from seconds to HH:MM:SS string - Moved to companion
        fun formatTime(totalSeconds: Int): String {
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val secs = totalSeconds % 60
            return String.format("%02d:%02d:%02d", hours, minutes, secs)
        }

        // Helper function to start the service with a specific action
        // Uses startForegroundService if the action implies going into a foreground state
        fun startService(context: Context, action: String, mode: FastingMode? = null) {
            val intent = Intent(context, FastingService::class.java).apply {
                this.action = action
                mode?.let { putExtra(EXTRA_FASTING_MODE, it) }
            }

            // Determine if we need to use startForegroundService
            val requiresForeground = when(action) {
                ACTION_START -> true
                ACTION_SET_MODE -> _timerState.value == TimerState.RUNNING // If timer is already running when mode is set
                // Add other actions here that might require bringing the service to the foreground
                else -> false
            }

            if (requiresForeground) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent) // For older Android versions
                }
            } else {
                // For actions that don't require immediate foreground (e.g., pause when already foreground)
                context.startService(intent)
            }
        }

        // Helper to stop the service
        fun stopService(context: Context) {
            val intent = Intent(context, FastingService::class.java).apply {
                action = ACTION_STOP
            }
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        // Ensure the notification channel is created as soon as the service instance is created
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let { action ->
            when (action) {
                ACTION_START -> startTimer()
                ACTION_PAUSE -> pauseTimer()
                ACTION_RESET -> resetTimer()
                ACTION_STOP -> stopServiceAndForeground() // Call a specific stop method
                ACTION_SET_MODE -> {
                    // Use getSerializableExtra with Class for API 33+
                    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getSerializableExtra(EXTRA_FASTING_MODE, FastingMode::class.java)
                    } else {
                        @Suppress("DEPRECATION") // For backward compatibility
                        intent.getSerializableExtra(EXTRA_FASTING_MODE) as? FastingMode
                    }
                    mode?.let { setMode(it) }
                }
                // Add the else branch to make the when exhaustive for String
                else -> {
                    // Handle unknown actions, e.g., log a warning or do nothing
                    // Log.w("FastingService", "Received unknown action: $action")
                }
            }
        }
        // Call the super implementation and return its result.
        return super.onStartCommand(intent, flags, startId)
    }

    // Sets the fasting mode and initializes timer values and state
    private fun setMode(mode: FastingMode) {
        _currentMode.value = mode // Update StateFlow in companion object
        eatTimeSeconds = mode.eatHours * 3600
        fastTimeSeconds = mode.fastHours * 3600
        _fastingState.value = FastingState.EATING // Default to Eating phase when mode is set
        _remainingTimeSeconds.value = eatTimeSeconds // Initialize time to eating window
        _timerState.value = TimerState.IDLE // Timer is idle initially after setting mode
        stopTimerJob() // Ensure any running timer is stopped
        updateNotification() // Update notification to reflect the new mode and state (IDLE)
    }

    // Starts the timer countdown
    private fun startTimer() {
        if (_timerState.value == TimerState.RUNNING) return // Already running
        if (_currentMode.value == null) return // Cannot start if no mode is set

        _timerState.value = TimerState.RUNNING // Update StateFlow in companion object

        timerJob?.cancel() // Cancel any previous job
        timerJob = lifecycleScope.launch {
            while (_remainingTimeSeconds.value > 0) {
                delay(1000)
                _remainingTimeSeconds.value-- // Update StateFlow in companion object
                updateNotification() // Update notification every second
            }
            // Timer finished, switch state
            switchFastingState()
            // Start the next phase immediately
            startTimer()
        }

        // Start the service as a foreground service
        // This call is now redundant if startService() helper is used correctly with requiresForeground
        // but harmless to keep here as a fallback if startService() was bypassed.
        startForegroundService()
    }

    // Switches between the EATING and FASTING phases
    private fun switchFastingState() {
        if (_fastingState.value == FastingState.EATING) {
            _fastingState.value = FastingState.FASTING
            _remainingTimeSeconds.value = fastTimeSeconds
        } else {
            _fastingState.value = FastingState.EATING
            _remainingTimeSeconds.value = eatTimeSeconds
        }
        updateNotification() // Update notification after switching state
    }

    // Pauses the timer
    private fun pauseTimer() {
        if (_timerState.value != TimerState.RUNNING) return // Not running
        _timerState.value = TimerState.PAUSED // Update StateFlow in companion object
        stopTimerJob() // Stop the coroutine job
        updateNotification() // Update notification to show paused state
    }

    // Resets the timer to the beginning of the EATING phase for the current mode
    private fun resetTimer() {
        _fastingState.value = FastingState.EATING // Update StateFlow in companion object
        // Ensure eatTimeSeconds is correct based on currentMode
        val mode = _currentMode.value
        if (mode != null) {
            eatTimeSeconds = mode.eatHours * 3600
        } else {
            eatTimeSeconds = 0 // Should not happen if mode is set before reset is possible
        }
        _remainingTimeSeconds.value = eatTimeSeconds // Update StateFlow in companion object
        _timerState.value = TimerState.IDLE // Update StateFlow in companion object
        stopTimerJob() // Stop the coroutine job
        stopServiceAndForeground() // Stop the service and remove notification
        // UI observing _currentMode becoming null will navigate back to selection.
    }

    // Cancels the coroutine job that runs the timer
    private fun stopTimerJob() {
        timerJob?.cancel()
        timerJob = null
    }

    // Starts the service in the foreground
    private fun startForegroundService() {
        // createNotificationChannel() is now called from onCreate()
        val notification = createNotification()
        // Start foreground with a unique ID and the notification
        startForeground(NOTIFICATION_ID, notification)
    }

    // Updates the existing foreground notification
    private fun updateNotification() {
        val notification = createNotification()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Use the same ID to update the existing notification
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    // Creates the Notification object for the foreground service
    private fun createNotification(): Notification {
        // Intent to open the app when the notification is tapped
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntentFlags = PendingIntent.FLAG_IMMUTABLE // Recommended for security

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            pendingIntentFlags
        )

        // Format the remaining time using the companion object's helper
        val timeFormatted = Companion.formatTime(_remainingTimeSeconds.value)
        // Get the current mode display name from the companion object's StateFlow
        val modeName = _currentMode.value?.displayName ?: "No mode selected"
        // Determine the text based on the current fasting state from StateFlow
        val stateText = if (_fastingState.value == FastingState.EATING) "Eating" else "Fasting"
        // Determine the content text based on the timer state from StateFlow
        val contentText = when(_timerState.value) {
            TimerState.RUNNING -> "$stateText: $timeFormatted left"
            TimerState.PAUSED -> "$stateText: $timeFormatted (Paused)"
            TimerState.IDLE -> "$modeName - Timer Idle"
        }


        // Build the notification
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Intermittent Fasting Timer")
            .setContentText(contentText)
            // Set a small icon (replace with your app's icon resource)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Example icon
            .setContentIntent(pendingIntent) // Set the intent to open the app
            // Make the notification ongoing when the timer is running, so it can't be swiped away
            .setOngoing(_timerState.value == TimerState.RUNNING)
            // Optional: Add actions (e.g., Pause, Reset) directly to the notification
            // .addAction(...)
            .build()
    }

    // Creates the notification channel (required for Android O and above)
    // This is now called from onCreate()
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                // Use IMPORTANCE_LOW for ongoing background tasks that don't need urgent attention
                // Use IMPORTANCE_DEFAULT or HIGH if the timer completing is a critical alert
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            // Check if the channel already exists before creating (optional but safe)
            if (manager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {
                manager.createNotificationChannel(serviceChannel)
            }
        }
    }

    // Stops the foreground service and removes the notification, then stops the service itself
    private fun stopServiceAndForeground() {
        stopForeground(true) // 'true' removes the notification
        stopSelf() // Stops the service
        // Reset states here so the UI observing the companion object flows sees the change
        _currentMode.value = null // Indicate no mode is selected
        _timerState.value = TimerState.IDLE
        _fastingState.value = FastingState.EATING
        _remainingTimeSeconds.value = 0
    }


    /*// Not using binding in this example
    override fun onBind(intent: Intent?): IBinder? {
        super.onBind(intent) // Required for LifecycleService
        return null
    }*/

    // Called when the service is being destroyed
    override fun onDestroy() {
        super.onDestroy()
        stopTimerJob() // Ensure the timer coroutine is cancelled
        // Clean up resources if any
    }
}