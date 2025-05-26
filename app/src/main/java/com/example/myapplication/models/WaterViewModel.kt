package com.example.myapplication.models

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope // For coroutines
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await // For suspending Firestore tasks
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.* // For Calendar

private const val WATER_REMINDER_REQUEST_CODE = 2001
private const val REMINDER_INTERVAL_MS = 10 * 1000L // 90 * 60 * 1000L // 1.5 hours in milliseconds

// Data class to hold daily water count for statistics
data class DailyWater(
    val date: String, // YYYY-MM-DD
    val count: Int
)

class WaterViewModel(private val appContext: Context) : ViewModel() {
    private val _waterCount = MutableStateFlow(0)
    val waterCount = _waterCount.asStateFlow()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid

    // LiveData to expose the weekly stats to the UI
    private val _weeklyWaterStats = MutableStateFlow<List<DailyWater>>(emptyList())
    val weeklyWaterStats = _weeklyWaterStats.asStateFlow()

    // MutableStateFlow for loading state (using Flow for consistency with waterCount)
    private val _isLoadingStats = MutableStateFlow(false)
    val isLoadingStats = _isLoadingStats.asStateFlow()

    // MutableStateFlow for error messages
    private val _statsErrorMessage = MutableStateFlow<String?>(null)
    val statsErrorMessage = _statsErrorMessage.asStateFlow()

    private var alarmMgr: AlarmManager? = null
    private lateinit var alarmIntent: PendingIntent

    init {
        loadWaterCount()
        alarmMgr = appContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        alarmIntent = Intent(appContext, WaterReminderBroadcastReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(appContext, WATER_REMINDER_REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
        fetchWeeklyWaterStats() // Fetch weekly stats on init
    }

    fun increase() {
        _waterCount.value++
        saveWaterCount()
        // Refresh weekly stats after saving to reflect current day's change
        fetchWeeklyWaterStats()
        if (_waterCount.value < 8) { // Assuming 8 is a target or threshold
            scheduleWaterReminder()
        } else {
            cancelWaterReminder()
        }
    }

    fun decrease() {
        if (_waterCount.value > 0) {
            _waterCount.value--
            saveWaterCount()
            // Refresh weekly stats after saving to reflect current day's change
            fetchWeeklyWaterStats()
            if (_waterCount.value < 8) { // Assuming 8 is a target or threshold
                scheduleWaterReminder()
            } else {
                cancelWaterReminder()
            }
        }
    }

    //saglabā ūdeni skaiti
    private fun saveWaterCount() {
        val data = hashMapOf("waterCount" to _waterCount.value.toLong(), "lastDrinkTime" to System.currentTimeMillis())
        userId?.let { uid ->
            db.collection("users").document(uid).collection("waterTracker").document(getCurrentDate())
                .set(data)
                .addOnFailureListener { e ->
                    // Handle save error, e.g., log it or show a Toast
                    println("Error saving water count: ${e.message}")
                }
        }
    }

    private fun loadWaterCount() {
        userId?.let { uid ->
            db.collection("users").document(uid).collection("waterTracker").document(getCurrentDate())
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists() && document.contains("waterCount")) {
                        _waterCount.value = (document.getLong("waterCount") ?: 0L).toInt()
                    } else {
                        _waterCount.value = 0 // Reset if no data for today
                    }
                }
                .addOnFailureListener { e ->
                    // Handle load error
                    println("Error loading water count: ${e.message}")
                    _waterCount.value = 0 // Default to 0 on error
                }
        }
    }

    // New function to fetch weekly water statistics
    fun fetchWeeklyWaterStats() {
        if (userId == null) {
            _statsErrorMessage.value = "User not authenticated."
            return
        }

        _isLoadingStats.value = true
        _statsErrorMessage.value = null // Clear previous errors

        viewModelScope.launch { // Coroutine on main thread (Flows handle background)
            val dailyCounts = mutableListOf<DailyWater>()
            val calendar = Calendar.getInstance()
            val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd") // Using java.time.format for LocalDate

            for (i in 0 until 7) {
                val date = LocalDate.now().minusDays(i.toLong()) // Get dates from today backwards
                val formattedDate = date.format(dateFormat)

                val path = "users/$userId/waterTracker/$formattedDate"
                val documentRef = db.collection("users").document(userId).collection("waterTracker").document(formattedDate)

                try {
                    val snapshot = documentRef.get().await() // Suspends until data is fetched
                    val waterCount = snapshot.getLong("waterCount")?.toInt() ?: 0
                    dailyCounts.add(DailyWater(formattedDate, waterCount))
                } catch (e: Exception) {
                    _statsErrorMessage.value = "Failed to fetch data for $formattedDate: ${e.message}"
                    dailyCounts.add(DailyWater(formattedDate, 0)) // Add 0 on error
                }
            }

            // Sort the list by date in ascending order (oldest to newest) for chart display
            dailyCounts.sortBy { it.date }

            _weeklyWaterStats.value = dailyCounts // Update the Flow
            _isLoadingStats.value = false
        }
    }

    //dod paziņojumu
    fun scheduleWaterReminder() {
        val triggerTime = System.currentTimeMillis() + REMINDER_INTERVAL_MS

        // Use setExactAndAllowWhileIdle for more precise alarms if needed, but be mindful of battery
        alarmMgr?.set(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            alarmIntent
        )
        println("Water reminder scheduled for ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(triggerTime)}")
    }

    fun cancelWaterReminder() {
        alarmMgr?.cancel(alarmIntent)
        println("Water reminder cancelled")
    }

    private fun getCurrentDate(): String {
        return LocalDate.now().toString()
    }
}