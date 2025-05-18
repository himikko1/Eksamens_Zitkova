package com.example.myapplication.models

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

private const val WATER_REMINDER_REQUEST_CODE = 2001
private const val REMINDER_INTERVAL_MS = 10 * 1000L//90 * 60 * 1000L // 1.5 hours in milliseconds

class WaterViewModel(private val appContext: Context) : ViewModel() {
    private val _waterCount = MutableStateFlow(0)
    val waterCount = _waterCount.asStateFlow()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid

    private var alarmMgr: AlarmManager? = null
    private lateinit var alarmIntent: PendingIntent

    init {
        loadWaterCount()
        alarmMgr = appContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        alarmIntent = Intent(appContext, WaterReminderBroadcastReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(appContext, WATER_REMINDER_REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
    }

    fun increase() {
        _waterCount.value++
        saveWaterCount()
        if (_waterCount.value < 8) {
            scheduleWaterReminder()
        } else {
            cancelWaterReminder()
        }
    }

    fun decrease() {
        if (_waterCount.value > 0) {
            _waterCount.value--
            saveWaterCount()
            if (_waterCount.value < 8) {
                scheduleWaterReminder()
            } else {
                cancelWaterReminder()
            }
        }
    }


    //saglabā ūdeni skaiti
    private fun saveWaterCount() {
        val data = hashMapOf("waterCount" to _waterCount.value, "lastDrinkTime" to System.currentTimeMillis())
        userId?.let {
            db.collection("users").document(it).collection("waterTracker").document(getCurrentDate())
                .set(data)
        }
    }

    private fun loadWaterCount() {
        userId?.let {
            db.collection("users").document(it).collection("waterTracker").document(getCurrentDate())
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.contains("waterCount")) {
                        _waterCount.value = (document.getLong("waterCount") ?: 0).toInt()
                    }
                }
        }
    }

    //dod paziņojumu
    fun scheduleWaterReminder() {
        val triggerTime = System.currentTimeMillis() + REMINDER_INTERVAL_MS

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