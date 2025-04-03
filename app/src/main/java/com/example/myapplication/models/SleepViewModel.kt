package com.example.myapplication.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sleeptracker.model.SleepRecord
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*


class SleepViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val sleepCollection = firestore.collection("sleep_records")
    private val auth = FirebaseAuth.getInstance()

    private val _sleepRecords = MutableStateFlow<List<SleepRecord>>(emptyList())
    val sleepRecords: StateFlow<List<SleepRecord>> = _sleepRecords

    init {
        if (auth.currentUser == null) {
            auth.signInAnonymously()
        }

        fetchSleepRecords()
    }

    fun addSleepRecord(sleepTime: Long, wakeTime: Long, quality: Int = 0, notes: String = "") {
        val currentUser = auth.currentUser ?: return

        val record = SleepRecord(
            id = UUID.randomUUID().toString(),
            userId = currentUser.uid,
            sleepTime = sleepTime,
            wakeTime = wakeTime,
            date = Calendar.getInstance().timeInMillis,
            quality = quality,
            notes = notes
        )

        sleepCollection.document(record.id).set(record)
            .addOnSuccessListener {
                fetchSleepRecords()
            }
            .addOnFailureListener { e ->
                println("Error adding document: $e")
            }
    }

    fun fetchSleepRecords() {
        val currentUser = auth.currentUser ?: return

        sleepCollection
            .whereEqualTo("userId", currentUser.uid)
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val records = documents.toObjects(SleepRecord::class.java)
                _sleepRecords.value = records
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }
    }

    fun fetchSleepRecordsForPeriod(startTime: Long, endTime: Long) {
        val currentUser = auth.currentUser ?: return

        sleepCollection
            .whereEqualTo("userId", currentUser.uid)
            .whereGreaterThanOrEqualTo("date", startTime)
            .whereLessThanOrEqualTo("date", endTime)
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val records = documents.toObjects(SleepRecord::class.java)
                _sleepRecords.value = records
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }
    }
}