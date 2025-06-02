// models/NotificationUtils.kt
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
import java.util.Locale // Import for SimpleDateFormat

// --- Water Reminder Constants ---
private const val WATER_REMINDER_CHANNEL_ID = "water_reminder_channel"
const val WATER_REMINDER_NOTIFICATION_ID = 1001
const val ACTION_DRINK = "com.example.myapplication.ACTION_DRINK"
private const val WATER_REMINDER_DRINK_REQUEST_CODE = 3001
private const val WATER_REMINDER_ALARM_REQUEST_CODE = 2001
private const val WATER_REMINDER_TEST_INTERVAL_MS = 10 * 1000L // 10 seconds for testing
// --- Menstrual Reminder Constants ---
private const val MENSTRUAL_REMINDER_CHANNEL_ID = "menstrual_reminder_channel"
const val MENSTRUAL_REMINDER_NOTIFICATION_ID = 1003 // Unique ID for menstrual notifications
// Removed ACTION_LOG_PERIOD and MENSTRUAL_REMINDER_LOG_REQUEST_CODE as no actions
private const val MENSTRUAL_REMINDER_ALARM_REQUEST_CODE = 2003 // Unique request code for menstrual alarm
private const val MENSTRUAL_REMINDER_TEST_INTERVAL_MS = 10 * 1000L // 10 seconds for testing

object NotificationUtils {

    // --- Channel Creation Methods ---
    fun createWaterReminderChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val waterName = "Water Reminder"
            val waterDescription = "Reminds you to drink water"
            val waterChannel = NotificationChannel(WATER_REMINDER_CHANNEL_ID, waterName, NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = waterDescription
            }
            val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(waterChannel)
        }
    }

    // Menstrual Reminder Channel
    fun createMenstrualReminderChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val menstrualName = "Menstrual Cycle Reminder"
            val menstrualDescription = "Reminds about your menstrual cycle"
            val menstrualImportance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(MENSTRUAL_REMINDER_CHANNEL_ID, menstrualName, menstrualImportance).apply {
                description = menstrualDescription
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // --- Alarm Scheduling/Canceling Methods ---

    fun scheduleWaterReminder(context: Context) {
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context.applicationContext, WaterReminderBroadcastReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(context.applicationContext, WATER_REMINDER_ALARM_REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
        val triggerTime = System.currentTimeMillis() + WATER_REMINDER_TEST_INTERVAL_MS
        alarmMgr.set(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            alarmIntent
        )
        Log.d("NotificationUtils", "Water reminder scheduled for ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(triggerTime)}")
    }

    fun cancelWaterReminder(context: Context) {
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context.applicationContext, WaterReminderBroadcastReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(context.applicationContext, WATER_REMINDER_ALARM_REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
        alarmMgr.cancel(alarmIntent)
        Log.d("NotificationUtils", "Water reminder cancelled")
    }

    fun scheduleMenstrualReminder(context: Context) {
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context.applicationContext, MenstrualReminderBroadcastReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(context.applicationContext, MENSTRUAL_REMINDER_ALARM_REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        val triggerTime = System.currentTimeMillis() + MENSTRUAL_REMINDER_TEST_INTERVAL_MS // Hardcoded 10 seconds for testing
        alarmMgr.set(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            alarmIntent
        )
        Log.d("NotificationUtils", "Menstrual reminder scheduled for ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(triggerTime)}")
    }

    fun cancelMenstrualReminder(context: Context) {
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context.applicationContext, MenstrualReminderBroadcastReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(context.applicationContext, MENSTRUAL_REMINDER_ALARM_REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
        alarmMgr.cancel(alarmIntent)
        Log.d("NotificationUtils", "Menstrual reminder cancelled")
    }

    // --- Notification Display Methods ---

    fun showWaterNotification(context: Context) {
        Log.d("WaterReminder", "showWaterNotification() called")
        val builder = NotificationCompat.Builder(context.applicationContext, WATER_REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.local_drink) // Replace with your water icon
            .setContentTitle("Time to Hydrate!")
            .setContentText("It's been a while, take a sip of water.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val drinkIntent = Intent(context.applicationContext, WaterNotificationActionReceiver::class.java).apply {
            action = ACTION_DRINK
        }
        val drinkPendingIntent: PendingIntent =
            PendingIntent.getBroadcast(context.applicationContext, WATER_REMINDER_DRINK_REQUEST_CODE, drinkIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        builder.addAction(R.drawable.local_drink, "Drink", drinkPendingIntent)

        with(NotificationManagerCompat.from(context.applicationContext)) {
            if (ActivityCompat.checkSelfPermission(
                    context.applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(WATER_REMINDER_NOTIFICATION_ID, builder.build())
            }
        }
    }

    fun showMenstrualReminderNotification(context: Context) {
        Log.d("MenstrualReminder", "showMenstrualReminderNotification() called")
        val builder = NotificationCompat.Builder(context.applicationContext, MENSTRUAL_REMINDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_my_calendar) // Replace with your menstrual cycle icon
            .setContentTitle("Cycle Reminder")
            .setContentText("Your menstrual period is expected soon.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // Notification dismisses when tapped

        // NO ACTION BUTTONS ARE ADDED HERE

        with(NotificationManagerCompat.from(context.applicationContext)) {
            if (ActivityCompat.checkSelfPermission(
                    context.applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(MENSTRUAL_REMINDER_NOTIFICATION_ID, builder.build())
            }
        }
    }
}