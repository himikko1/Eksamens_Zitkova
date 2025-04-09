package com.example.myapplication.TimePicker

import java.text.SimpleDateFormat
import java.util.*


fun formatTime(timeInMillis: Long): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(Date(timeInMillis))
}

fun formatDate(timeInMillis: Long): String {
    val formatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    return formatter.format(Date(timeInMillis))
}