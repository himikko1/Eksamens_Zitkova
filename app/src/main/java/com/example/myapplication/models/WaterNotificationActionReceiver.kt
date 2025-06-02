package com.example.myapplication.models

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.myapplication.models.ACTION_DRINK
import com.example.myapplication.models.NotificationUtils
import com.example.myapplication.models.WATER_REMINDER_NOTIFICATION_ID
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate

class WaterNotificationActionReceiver : BroadcastReceiver() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent?.action == ACTION_DRINK) {
            Log.d("DrinkActionReceiver", "Drink action received!")

            userId?.let { uid ->
                val waterTrackerRef = db.collection("users").document(uid)
                    .collection("waterTracker").document(getCurrentDate())

                db.runTransaction { transaction ->
                    val snapshot = transaction.get(waterTrackerRef)
                    var currentCount = snapshot.getLong("waterCount") ?: 0
                    currentCount++
                    transaction.update(waterTrackerRef, "waterCount", currentCount)
                    currentCount
                }.addOnSuccessListener { newCount ->
                    Log.d("DrinkActionReceiver", "Water count updated in DB to: $newCount")

                    // Schedule the next reminder if the count is less than 8
                    if (newCount < 8) {
                        // You'll need a way to access your AlarmManager and PendingIntent here.
                        // Consider passing the context to a function in NotificationUtils or
                        // having a separate AlarmScheduler utility class.
                        NotificationUtils.scheduleWaterReminder(context.applicationContext)
                        Log.d("DrinkActionReceiver", "Next reminder scheduled.")
                    } else {
                        NotificationUtils.cancelWaterReminder(context.applicationContext)
                        Log.d("DrinkActionReceiver", "Reminder cancelled (goal reached).")
                    }

                    // Dismiss the notification
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                    notificationManager.cancel(WATER_REMINDER_NOTIFICATION_ID)
                    Log.d("DrinkActionReceiver", "Notification dismissed.")

                }.addOnFailureListener { e ->
                    Log.e("DrinkActionReceiver", "Error updating water count in DB", e)
                }
            }
        }
    }

    private fun getCurrentDate(): String {
        return LocalDate.now().toString()
    }
}