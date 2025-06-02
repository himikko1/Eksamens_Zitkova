//package com.example.myapplication.models
//
//import android.app.NotificationManager
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import androidx.core.app.NotificationCompat
//import java.time.LocalDate
//import java.time.format.DateTimeFormatter
//
//class MenstrualNotificationReceiver : BroadcastReceiver() {
//
//    override fun onReceive(context: Context, intent: Intent) {
//        // This is a simplified example. In a real app, you might fetch the predicted date
//        // from SharedPreferences or pass it via the intent.
//        val predictedDate = intent.getStringExtra("PREDICTED_DATE")
//        val formattedDate = predictedDate?.let {
//            LocalDate.parse(it, DateTimeFormatter.ISO_DATE).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
//        } ?: "an unknown date"
//
//
//        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        val notification = NotificationCompat.Builder(context, "menstrual_cycle_notification_channel") // Use your CHANNEL_ID
//            .setSmallIcon(android.R.drawable.ic_dialog_info) // Your app icon
//            .setContentTitle("Menstrual Cycle Reminder")
//            .setContentText("Your next menstrual cycle is predicted to start on $formattedDate.")
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .setAutoCancel(true)
//            .build()
//
//        notificationManager.notify(101, notification) // Use your NOTIFICATION_ID
//
//        // Re-schedule notification after boot if needed
//        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
//
//        }
//    }
//}