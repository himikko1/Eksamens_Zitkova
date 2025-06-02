// models/WaterViewModel.kt
package com.example.myapplication.models

import android.app.Application // Import Application
import androidx.lifecycle.AndroidViewModel // Change to AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

private const val WATER_TARGET_COUNT = 8

data class DailyWater(
    val date: String,
    val count: Int
)

// Change to AndroidViewModel and use 'application' directly
class WaterViewModel(application: Application) : AndroidViewModel(application) {
    // Now you can use 'application' directly where you used 'appContext'
    // For NotificationUtils, you'll pass 'getApplication()'
    private val _waterCount = MutableStateFlow(0)
    val waterCount = _waterCount.asStateFlow()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid

    private val _weeklyWaterStats = MutableStateFlow<List<DailyWater>>(emptyList())
    val weeklyWaterStats = _weeklyWaterStats.asStateFlow()

    private val _isLoadingStats = MutableStateFlow(false)
    val isLoadingStats = _isLoadingStats.asStateFlow()

    private val _statsErrorMessage = MutableStateFlow<String?>(null)
    val statsErrorMessage = _statsErrorMessage.asStateFlow()

    init {
        loadWaterCount()
        fetchWeeklyWaterStats()
    }

    fun increase() {
        _waterCount.value++
        saveWaterCount()
        fetchWeeklyWaterStats()
        if (_waterCount.value < WATER_TARGET_COUNT) {
            scheduleWaterReminder()
        } else {
            cancelWaterReminder()
        }
    }

    fun decrease() {
        if (_waterCount.value > 0) {
            _waterCount.value--
            saveWaterCount()
            fetchWeeklyWaterStats()
            if (_waterCount.value < WATER_TARGET_COUNT) {
                scheduleWaterReminder()
            } else {
                cancelWaterReminder()
            }
        }
    }

    private fun saveWaterCount() {
        val data = hashMapOf("waterCount" to _waterCount.value.toLong(), "lastDrinkTime" to System.currentTimeMillis())
        userId?.let { uid ->
            db.collection("users").document(uid).collection("waterTracker").document(getCurrentDate())
                .set(data)
                .addOnFailureListener { e ->
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
                        _waterCount.value = 0
                    }
                }
                .addOnFailureListener { e ->
                    println("Error loading water count: ${e.message}")
                    _waterCount.value = 0
                }
        }
    }

    fun fetchWeeklyWaterStats() {
        if (userId == null) {
            _statsErrorMessage.value = "User not authenticated."
            return
        }

        _isLoadingStats.value = true
        _statsErrorMessage.value = null

        viewModelScope.launch {
            val dailyCounts = mutableListOf<DailyWater>()
            val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")

            for (i in 0 until 7) {
                val date = LocalDate.now().minusDays(i.toLong())
                val formattedDate = date.format(dateFormat)

                val documentRef = db.collection("users").document(userId).collection("waterTracker").document(formattedDate)

                try {
                    val snapshot = documentRef.get().await()
                    val waterCount = snapshot.getLong("waterCount")?.toInt() ?: 0
                    dailyCounts.add(DailyWater(formattedDate, waterCount))
                } catch (e: Exception) {
                    _statsErrorMessage.value = "Failed to fetch data for $formattedDate: ${e.message}"
                    dailyCounts.add(DailyWater(formattedDate, 0))
                }
            }

            dailyCounts.sortBy { it.date }

            _weeklyWaterStats.value = dailyCounts
            _isLoadingStats.value = false
        }
    }

    fun scheduleWaterReminder() {
        // Use getApplication() here, as it's provided by AndroidViewModel
        NotificationUtils.scheduleWaterReminder(getApplication())
    }

    fun cancelWaterReminder() {
        // Use getApplication() here, as it's provided by AndroidViewModel
        NotificationUtils.cancelWaterReminder(getApplication())
    }

    private fun getCurrentDate(): String {
        return LocalDate.now().toString()
    }
}