package com.example.myapplication.models

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Serializable
data class MenstrualCycleData(
    val userId: String = "",
    val startDate: String? = null,
    val endDate: String? = null,
    val markedDates: MutableList<String> = mutableListOf()
)


class MenstrualCalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Состояние данных менструального цикла
    private val _cycleData = MutableStateFlow(MenstrualCycleData())
    val cycleData: StateFlow<MenstrualCycleData> = _cycleData.asStateFlow()

    val selectedStartDate = mutableStateOf<LocalDate?>(null)
    val selectedEndDate = mutableStateOf<LocalDate?>(null)

    fun loadUserData(userId: String) {
        viewModelScope.launch {
            try {
                firestore.collection("users")
                    .document(userId)
                    .collection("menstrualData")
                    .document("cycleData")
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            val data = document.data?.let {
                                MenstrualCycleData(
                                    userId = userId,
                                    startDate = it["startDate"] as? String,
                                    endDate = it["endDate"] as? String,
                                    markedDates = (it["markedDates"] as? List<String>)?.toMutableList() ?: mutableListOf()
                                )
                            } ?: MenstrualCycleData(userId = userId)

                            _cycleData.value = data

                            // Установка выбранных дат если они есть
                            data.startDate?.let {
                                selectedStartDate.value = LocalDate.parse(it, DateTimeFormatter.ISO_DATE)
                            }
                            data.endDate?.let {
                                selectedEndDate.value = LocalDate.parse(it, DateTimeFormatter.ISO_DATE)
                            }
                        } else {
                            // Если данных нет, создаем новые
                            _cycleData.value = MenstrualCycleData(userId = userId)
                        }
                    }
                    .addOnFailureListener { e ->
                        println("Ошибка при загрузке данных менструального цикла: ${e.message}")
                        _cycleData.value = MenstrualCycleData(userId = userId)
                    }
            } catch (e: Exception) {
                // Обработка ошибок
                println("Ошибка при загрузке данных менструального цикла: ${e.message}")
                _cycleData.value = MenstrualCycleData(userId = userId)
            }
        }
    }

    // Сохранить данные цикла
    private fun saveData() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch

                val cycleDataMap = hashMapOf(
                    "userId" to userId,
                    "startDate" to _cycleData.value.startDate,
                    "endDate" to _cycleData.value.endDate,
                    "markedDates" to _cycleData.value.markedDates
                )

                firestore.collection("users")
                    .document(userId)
                    .collection("menstrualData")
                    .document("cycleData")
                    .set(cycleDataMap)
                    .addOnSuccessListener {
                        println("Данные менструального цикла успешно сохранены")
                    }
                    .addOnFailureListener { e ->
                        println("Ошибка при сохранении данных: ${e.message}")
                    }
            } catch (e: Exception) {
                println("Ошибка при сохранении данных: ${e.message}")
            }
        }
    }

    // Установить дату начала цикла
    fun setStartDate(date: LocalDate) { //
        selectedStartDate.value = date
        val dateStr = date.format(DateTimeFormatter.ISO_DATE)
        _cycleData.value = _cycleData.value.copy(startDate = dateStr)
        // Помечаем начальную дату
        toggleDateMark(date)
        // Если есть конечная дата, помечаем даты между началом и концом
        selectedEndDate.value?.let { endDate ->
            markPeriodDates(date, endDate)
        }
        saveData()
    }
    private fun markPeriodDates(startDate: LocalDate, endDate: LocalDate) {
        val newMarkedDates = _cycleData.value.markedDates.toMutableList()
        var currentDate = startDate

        while (!currentDate.isAfter(endDate)) {
            val dateStr = currentDate.format(DateTimeFormatter.ISO_DATE)
            if (dateStr !in newMarkedDates) {
                newMarkedDates.add(dateStr)
            }
            currentDate = currentDate.plusDays(1)
        }

        _cycleData.value = _cycleData.value.copy(markedDates = newMarkedDates)
    }

    fun setEndDate(date: LocalDate) {
        selectedEndDate.value = date
        val dateStr = date.format(DateTimeFormatter.ISO_DATE)
        _cycleData.value = _cycleData.value.copy(endDate = dateStr)
        toggleDateMark(date)
        selectedStartDate.value?.let { startDate ->
            markPeriodDates(startDate, date)
        }
        saveData()
    }

    fun toggleDateMark(date: LocalDate) {
        val dateStr = date.format(DateTimeFormatter.ISO_DATE)
        val currentMarkedDates = _cycleData.value.markedDates.toMutableList()

        if (dateStr in currentMarkedDates) {
            currentMarkedDates.remove(dateStr)
        } else {
            currentMarkedDates.add(dateStr)
        }

        _cycleData.value = _cycleData.value.copy(markedDates = currentMarkedDates)
        saveData()
    }

    // Проверить, отмечена ли дата
    fun isDateMarked(date: LocalDate): Boolean {
        val dateStr = date.format(DateTimeFormatter.ISO_DATE)
        return dateStr in _cycleData.value.markedDates
    }

    fun isInPeriod(date: LocalDate): Boolean {
        val start = selectedStartDate.value
        val end = selectedEndDate.value

        if (start == null || end == null) return false

        return !date.isBefore(start) && !date.isAfter(end)
    }

    fun isStartDate(date: LocalDate): Boolean {
        val startDate = selectedStartDate.value ?: return false
        return date.isEqual(startDate)
    }

    fun isEndDate(date: LocalDate): Boolean {
        val endDate = selectedEndDate.value ?: return false
        return date.isEqual(endDate)
    }
}