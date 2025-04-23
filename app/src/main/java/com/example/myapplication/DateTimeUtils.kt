//package com.example.myapplication.utils
//
//import kotlinx.datetime.*
//import java.time.ZoneId
//import java.time.LocalTime as JavaLocalTime
//import java.time.LocalDate as JavaLocalDate
//import java.time.Instant as JavaInstant
//
//
//// Обратная конвертация из java.time в kotlinx.datetime
//fun JavaLocalTime.toKotlinLocalTime(): kotlinx.datetime.LocalTime = kotlinx.datetime.LocalTime(hour, minute)
//fun JavaLocalDate.toKotlinLocalDate(): kotlinx.datetime.LocalDate = kotlinx.datetime.LocalDate(year, monthValue, dayOfMonth)
//fun JavaInstant.toKotlinInstant(): kotlinx.datetime.Instant = kotlinx.datetime.Instant.fromEpochSeconds(epochSecond, nano)