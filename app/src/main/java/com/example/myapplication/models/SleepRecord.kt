package com.example.myapplication.models

data class SleepRecord(
    val id: String = "",
    val userId: String = "",
    val sleepTime: Long = 0,
    val wakeTime: Long = 0,
    val date: Long = 0,
    val quality: Int = 0,
    val notes: String = ""
) {
    val durationInHours: Float
        get() = (wakeTime - sleepTime) / (1000f * 60f * 60f)
}
