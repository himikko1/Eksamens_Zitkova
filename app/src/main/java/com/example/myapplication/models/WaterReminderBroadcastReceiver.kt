package com.example.myapplication.models

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class WaterReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            NotificationUtils.showWaterNotification(context)
        }
    }
}