package com.example.myapplication.models

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.myapplication.R // Replace with your app's package name


private const val WATER_REMINDER_CHANNEL_ID = "water_reminder_channel"
const val WATER_REMINDER_NOTIFICATION_ID = 1001
private const val WATER_REMINDER_REQUEST_CODE = 2001
const val ACTION_DRINK = "com.example.myapplication.ACTION_DRINK"
private const val WATER_REMINDER_DRINK_REQUEST_CODE = 3001

object NotificationUtils {
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Water Reminder"
            val descriptionText = "Reminds you to drink water"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(WATER_REMINDER_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleReminder(context: Context) {
        // Get AlarmManager
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Create the intent for the broadcast receiver
        val alarmIntent = Intent(context, WaterReminderBroadcastReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(context, WATER_REMINDER_REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        val triggerTime = System.currentTimeMillis() + 10 * 1000L // Use REMINDER_INTERVAL_MS or pass it as an argument
        alarmMgr.set(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            alarmIntent
        )
        Log.d("NotificationUtils", "Water reminder scheduled for ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(triggerTime)}")
    }

    fun cancelReminder(context: Context) {
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, WaterReminderBroadcastReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(context, WATER_REMINDER_REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
        alarmMgr.cancel(alarmIntent)
        Log.d("NotificationUtils", "Water reminder cancelled")
    }

    fun showNotification(context: Context) {
        Log.d("WaterReminder", "showNotification() called")
        val builder = NotificationCompat.Builder(context, WATER_REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.local_drink) // Replace with your notification icon
            .setContentTitle("Time to Hydrate!")
            .setContentText("It's been a while, take a sip of water.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val drinkIntent = Intent(context, WaterNotificationActionReceiver::class.java).apply {
            action = ACTION_DRINK
        }
        val drinkPendingIntent: PendingIntent =
            PendingIntent.getBroadcast(context, WATER_REMINDER_DRINK_REQUEST_CODE, drinkIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        builder.addAction(R.drawable.local_drink, "Drink", drinkPendingIntent) // Add action

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(WATER_REMINDER_NOTIFICATION_ID, builder.build())
            }
        }
    }
}