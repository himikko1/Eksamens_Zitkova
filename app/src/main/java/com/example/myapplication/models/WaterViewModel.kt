package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WaterViewModel : ViewModel() {
    private val _waterCount = MutableStateFlow(0)
    val waterCount = _waterCount.asStateFlow()

    private val db = FirebaseFirestore.getInstance()
    private val userId = "test_user" // заменишь на id из auth позже

    init {
        loadWaterCount()
    }

    fun increase() {
        _waterCount.value++
        saveWaterCount()
    }

    fun decrease() {
        if (_waterCount.value > 0) {
            _waterCount.value--
            saveWaterCount()
        }
    }

    private fun saveWaterCount() {
        val data = hashMapOf("waterCount" to _waterCount.value)
        db.collection("users").document(userId).collection("waterTracker").document("today")
            .set(data)
    }

    private fun loadWaterCount() {
        db.collection("users").document(userId).collection("waterTracker").document("today")
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.contains("waterCount")) {
                    _waterCount.value = (document.getLong("waterCount") ?: 0).toInt()
                }
            }
    }
}
