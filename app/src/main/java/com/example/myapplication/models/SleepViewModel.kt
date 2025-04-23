//package com.example.myapplication.models
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.Query
//import com.google.firebase.firestore.ktx.firestore
//import com.google.firebase.ktx.Firebase
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.tasks.await
//import kotlinx.datetime.* // Используем kotlinx-datetime
//import java.time.Clock
//import java.time.Instant
//import java.time.LocalDate
//import java.time.LocalDateTime
//import java.time.LocalTime
//import java.time.format.DateTimeFormatter // Для форматирования
//import java.util.TimeZone
//
//// Модель данных для UI
//data class SleepRecord(
//    val id: String = "", // ID документа из Firestore
//    val startTime: Instant,
//    val endTime: Instant,
//    val durationMinutes: Long
//)
//
//// Периоды для статистики
//enum class StatsPeriod { WEEK, MONTH, YEAR }
//
//class SleepViewModel : ViewModel() {
//
//    private val db: FirebaseFirestore = Firebase.firestore
//    private val auth: FirebaseAuth = FirebaseAuth.getInstance() // Если используешь аутентификацию
//
//    // Состояние для ввода времени
//    private val _selectedDate = MutableStateFlow(Clock.System.todayIn(TimeZone.currentSystemDefault()))
//    val selectedDate: StateFlow<LocalDate> = _selectedDate
//
//    // Используем LocalTime из kotlinx-datetime
//    private val _startTime = MutableStateFlow<LocalTime?>(null)
//    val startTime: StateFlow<LocalTime?> = _startTime
//
//    private val _endTime = MutableStateFlow<LocalTime?>(null)
//    val endTime: StateFlow<LocalTime?> = _endTime
//
//    // Рассчитанная длительность
//    private val _durationText = MutableStateFlow<String?>(null)
//    val durationText: StateFlow<String?> = _durationText
//
//    // Состояние для статистики
//    private val _sleepStats = MutableStateFlow<List<SleepRecord>>(emptyList())
//    val sleepStats: StateFlow<List<SleepRecord>> = _sleepStats
//
//    private val _isLoading = MutableStateFlow(false)
//    val isLoading: StateFlow<Boolean> = _isLoading
//
//    private val _errorMessage = MutableStateFlow<String?>(null)
//    val errorMessage: StateFlow<String?> = _errorMessage
//
//    fun onDateChange(date: LocalDate) {
//        _selectedDate.value = date
//        calculateDuration() // Пересчитать длительность при смене даты
//    }
//
//    fun onStartTimeChange(time: LocalTime) {
//        _startTime.value = time
//        calculateDuration()
//    }
//
//    fun onEndTimeChange(time: LocalTime) {
//        _endTime.value = time
//        calculateDuration()
//    }
//
//    private fun calculateDuration() {
//        val start = _startTime.value
//        val end = _endTime.value
//        val date = _selectedDate.value
//
//        if (start != null && end != null) {
//            try {
//                // Собираем дату и время начала и конца
//                var startDateTime = LocalDateTime(date, start)
//                var endDateTime = LocalDateTime(date, end)
//
//                // Если время конца раньше времени начала, считаем, что это следующий день
//                if (end < start) {
//                    endDateTime = endDateTime.plus(1, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
//                }
//
//                // Используем TimeZone.currentSystemDefault() для корректного расчета
//                val startInstant = startDateTime.toInstant(TimeZone.currentSystemDefault())
//                val endInstant = endDateTime.toInstant(TimeZone.currentSystemDefault())
//
//                val duration = endInstant - startInstant
//                val durationMinutes = duration.inWholeMinutes
//
//                if (durationMinutes > 0) {
//                    val hours = duration.inWholeHours
//                    val minutes = durationMinutes % 60
//                    _durationText.value = "Длительность: $hours ч $minutes мин"
//                } else {
//                    _durationText.value = "Некорректное время"
//                }
//            } catch (e: Exception) {
//                _durationText.value = "Ошибка расчета"
//                // Логирование ошибки
//                println("Duration calculation error: ${e.message}")
//            }
//        } else {
//            _durationText.value = null // Сброс, если не все данные введены
//        }
//    }
//
//
//    fun saveSleepRecord() {
//        val userId = auth.currentUser?.uid
//        if (userId == null) {
//            _errorMessage.value = "Пользователь не авторизован"
//            return
//        }
//
//        val start = _startTime.value
//        val end = _endTime.value
//        val date = _selectedDate.value
//
//        if (start == null || end == null) {
//            _errorMessage.value = "Введите время начала и конца сна"
//            return
//        }
//
//        _isLoading.value = true
//        viewModelScope.launch {
//            try {
//                var startDateTime = LocalDateTime(date, start)
//                var endDateTime = LocalDateTime(date, end)
//
//                if (end < start) {
//                    endDateTime = endDateTime.plus(1, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
//                }
//
//                val startInstant = startDateTime.toInstant(TimeZone.currentSystemDefault())
//                val endInstant = endDateTime.toInstant(TimeZone.currentSystemDefault())
//                val duration = endInstant - startInstant
//                val durationMinutes = duration.inWholeMinutes
//
//                if (durationMinutes <= 0) {
//                    _errorMessage.value = "Некорректная длительность сна"
//                    _isLoading.value = false
//                    return@launch
//                }
//
//                // Конвертируем Instant в Firebase Timestamp
//                val startTimestamp = com.google.firebase.Timestamp(startInstant.epochSeconds, startInstant.nanosecondsOfSecond)
//                val endTimestamp = com.google.firebase.Timestamp(endInstant.epochSeconds, endInstant.nanosecondsOfSecond)
//
//
//                val sleepData = hashMapOf(
//                    "userId" to userId,
//                    "startTime" to startTimestamp,
//                    "endTime" to endTimestamp,
//                    "durationMinutes" to durationMinutes,
//                    "createdAt" to com.google.firebase.Timestamp.now() // Текущее время сервера
//                )
//
//                db.collection("users").document(userId)
//                    .collection("sleep_records")
//                    .add(sleepData)
//                    .await() // Ожидаем завершения
//
//                // Очистка полей после сохранения (опционально)
//                // _startTime.value = null
//                // _endTime.value = null
//                // _durationText.value = null
//                _errorMessage.value = null // Очистить сообщение об ошибке
//                fetchSleepData(StatsPeriod.WEEK) // Обновить статистику после сохранения
//
//            } catch (e: Exception) {
//                _errorMessage.value = "Ошибка сохранения: ${e.message}"
//                println("Error saving sleep record: ${e.message}") // Логирование
//            } finally {
//                _isLoading.value = false
//            }
//        }
//    }
//
//    fun fetchSleepData(period: StatsPeriod) {
//        val userId = auth.currentUser?.uid ?: return // Нужен ID пользователя
//
//        _isLoading.value = true
//        viewModelScope.launch {
//            try {
//                val now = Clock.System.now()
//                val zone = TimeZone.currentSystemDefault()
//                val today = now.toLocalDateTime(zone).date
//
//                val (startDate, endDate) = when (period) {
//                    StatsPeriod.WEEK -> {
//                        val startOfWeek = today.minus(today.dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY)
//                        val endOfWeek = startOfWeek.plus(1, DateTimeUnit.WEEK)
//                        Pair(startOfWeek.atStartOfDayIn(zone), endOfWeek.atStartOfDayIn(zone))
//                    }
//                    StatsPeriod.MONTH -> {
//                        val startOfMonth = LocalDate(today.year, today.monthNumber, 1)
//                        val endOfMonth = startOfMonth.plus(1, DateTimeUnit.MONTH)
//                        Pair(startOfMonth.atStartOfDayIn(zone), endOfMonth.atStartOfDayIn(zone))
//                    }
//                    StatsPeriod.YEAR -> {
//                        val startOfYear = LocalDate(today.year, 1, 1)
//                        val endOfYear = startOfYear.plus(1, DateTimeUnit.YEAR)
//                        Pair(startOfYear.atStartOfDayIn(zone), endOfYear.atStartOfDayIn(zone))
//                    }
//                }
//
//                // Конвертируем Instant в Firebase Timestamp для запроса
//                val startTimestamp = com.google.firebase.Timestamp(startDate.epochSeconds, startDate.nanosecondsOfSecond)
//                val endTimestamp = com.google.firebase.Timestamp(endDate.epochSeconds, endDate.nanosecondsOfSecond)
//
//
//                val querySnapshot = db.collection("users").document(userId)
//                    .collection("sleep_records")
//                    .whereGreaterThanOrEqualTo("startTime", startTimestamp)
//                    .whereLessThan("startTime", endTimestamp) // Записи, начавшиеся в этом периоде
//                    .orderBy("startTime", Query.Direction.DESCENDING) // Сначала новые
//                    .get()
//                    .await()
//
//                val records = querySnapshot.documents.mapNotNull { doc ->
//                    val data = doc.data
//                    val startTs = data?.get("startTime") as? com.google.firebase.Timestamp
//                    val endTs = data?.get("endTime") as? com.google.firebase.Timestamp
//                    val duration = data?.get("durationMinutes") as? Long
//
//                    if (startTs != null && endTs != null && duration != null) {
//                        // Конвертируем Firebase Timestamp обратно в Instant
//                        val startInstant = Instant.fromEpochSeconds(startTs.seconds, startTs.nanoseconds)
//                        val endInstant = Instant.fromEpochSeconds(endTs.seconds, endTs.nanoseconds)
//
//                        SleepRecord(
//                            id = doc.id,
//                            startTime = startInstant,
//                            endTime = endInstant,
//                            durationMinutes = duration
//                        )
//                    } else {
//                        null // Пропустить некорректные записи
//                    }
//                }
//                _sleepStats.value = records
//                _errorMessage.value = null
//
//            } catch (e: Exception) {
//                _errorMessage.value = "Ошибка загрузки статистики: ${e.message}"
//                println("Error fetching sleep data: ${e.message}") // Логирование
//                _sleepStats.value = emptyList() // Очистить при ошибке
//            } finally {
//                _isLoading.value = false
//            }
//        }
//    }
//}