// models/MenstrualReminderBroadcastReceiver.kt
package com.example.myapplication.models

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class MenstrualReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("MenstrualReminder", "Menstrual Cycle reminder alarm received!")
        if (context != null) {
            NotificationUtils.showMenstrualReminderNotification(context)
            // For a real app, you'd calculate the next period date and reschedule the alarm here.
        }
    }
}