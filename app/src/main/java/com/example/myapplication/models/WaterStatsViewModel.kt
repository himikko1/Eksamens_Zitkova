//package com.example.myapplication.models
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.Query
//// Remove the import for com.google.type.TimeZone
//// import com.google.type.TimeZone // REMOVE THIS LINE
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.tasks.await
//import kotlinx.datetime.* // Import all necessary components from kotlinx.datetime
//// Remove the import for java.time.Instant
//// import java.time.Instant // REMOVE THIS LINE
//
//
//enum class WaterStatsPeriod {
//    WEEK, TEN_DAYS, MONTH
//}
//
//data class WaterStats(
//    val period: WaterStatsPeriod,
//    val totalGlasses: Int
//)
//
//class WaterStatsViewModel : ViewModel() {
//
//    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
//    private val firestore = FirebaseFirestore.getInstance()
//
//    private val _waterStats = MutableStateFlow<List<WaterStats>>(emptyList())
//    val waterStats: StateFlow<List<WaterStats>> = _waterStats
//
//    private val _isLoading = MutableStateFlow(false)
//    val isLoading: StateFlow<Boolean> = _isLoading
//
//    private val _errorMessage = MutableStateFlow<String?>(null)
//    val errorMessage: StateFlow<String?> = _errorMessage
//
//    init {
//        // Fetch initial stats when the ViewModel is created
//        fetchWaterStats()
//    }
//
//    fun fetchWaterStats() {
//        val userId = auth.currentUser?.uid
//        if (userId == null) {
//            _errorMessage.value = "User not authenticated."
//            return
//        }
//
//        _isLoading.value = true
//        _errorMessage.value = null
//
//        viewModelScope.launch {
//            try {
//                val now = Clock.System.now()
//                // Use kotlinx.datetime.TimeZone
//                val zone = kotlinx.datetime.TimeZone.currentSystemDefault()
//
//                // Calculate start times for each period
//                val startOfToday = now.toLocalDateTime(zone).date.atStartOfDayIn(zone)
//                val startOfWeek = startOfToday.minus(7, DateTimeUnit.DAY, zone)
//                val startOfTenDays = startOfToday.minus(10, DateTimeUnit.DAY, zone)
//                // This calculates the start of the current month
//                val startOfMonth = startOfToday.minus(1, DateTimeUnit.MONTH, zone)
//
//                // If you want the last 30 days, use:
//                // val startOfLast30Days = now.minus(30, DateTimeUnit.DAY, zone)
//
//
//                // Pass kotlinx.datetime.Instant objects to the function
//                val weekStats = getWaterStatsForPeriod(userId, startOfWeek, now)
//                val tenDaysStats = getWaterStatsForPeriod(userId, startOfTenDays, now)
//                val monthStats = getWaterStatsForPeriod(userId, startOfMonth, now) // Or startOfLast30Days
//
//                _waterStats.value = listOf(
//                    WaterStats(WaterStatsPeriod.WEEK, weekStats),
//                    WaterStats(WaterStatsPeriod.TEN_DAYS, tenDaysStats),
//                    WaterStats(WaterStatsPeriod.MONTH, monthStats)
//                )
//
//            } catch (e: Exception) {
//                _errorMessage.value = "Failed to fetch water statistics: ${e.message}"
//            } finally {
//                _isLoading.value = false
//            }
//        }
//    }
//
//    private suspend fun getWaterStatsForPeriod(
//        userId: String,
//        // Change the parameter types to kotlinx.datetime.Instant
//        startTime: kotlinx.datetime.Instant,
//        endTime: kotlinx.datetime.Instant
//    ): Int {
//        return try {
//            val querySnapshot = firestore.collection("waterRecords")
//                .whereEqualTo("userId", userId)
//                // Assuming timestamp is stored as milliseconds, this comparison is correct
//                .whereGreaterThanOrEqualTo("timestamp", startTime.toEpochMilliseconds())
//                .whereLessThanOrEqualTo("timestamp", endTime.toEpochMilliseconds())
//                .get()
//                .await()
//
//            var totalGlasses = 0
//            for (document in querySnapshot.documents) {
//                val amount = document.getLong("amount")?.toInt() ?: 0
//                totalGlasses += amount
//            }
//            totalGlasses
//        } catch (e: Exception) {
//            println("Error fetching water stats for period: ${e.message}")
//            0 // Return 0 in case of error for this period
//        }
//    }
//}