package com.example.myapplication.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class CalorieData(
    val userId: String = "",
    val weight: Double = 0.0,
    val height: Double = 0.0,
    val age: Int = 0,
    val gender: String = "",
    val activityLevel: String = "",
    val dailyCalories: Double = 0.0,
    val lastUpdated: Long = System.currentTimeMillis()
)

class CalorieCalculatorViewModel : ViewModel() {
    private val db by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    private val _calorieData = MutableLiveData<CalorieData?>()
    val calorieData: LiveData<CalorieData?> = _calorieData

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    init {
        loadUserCalorieData()
    }

    fun loadUserCalorieData() {
        val userId = auth.currentUser?.uid ?: return
        _isLoading.value = true

        db.collection("calorieData")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                _isLoading.value = false
                if (document.exists()) {
                    _calorieData.value = document.toObject(CalorieData::class.java)
                } else {
                    _calorieData.value = CalorieData(userId = userId)
                }
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = "Kļūda ielādējot datus: ${e.message}"
            }
    }



    fun calculateAndSaveCalories(
        weight: Double,
        height: Double,
        age: Int,
        gender: String,
        activityLevel: String
    ) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _error.value = "Lietotājs nav ielogojies"
            return
        }

        try {
            _isLoading.value = true


            if (weight <= 0 || height <= 0 || age <= 0) {
                _error.value = "Lūdzu, ievadiet derīgas vērtības"
                _isLoading.value = false
                return
            }


            val bmr = when (gender) {
                "male" -> (10 * weight) + (6.25 * height) - (5 * age) + 5
                "female" -> (10 * weight) + (6.25 * height) - (5 * age) - 161
                else -> 0.0
            }


            val activityMultiplier = when (activityLevel) {
                "sedentary" -> 1.2
                "light" -> 1.375
                "moderate" -> 1.55
                "active" -> 1.725
                "very_active" -> 1.9
                else -> 1.2
            }

            val dailyCalories = bmr * activityMultiplier

            val calorieData = CalorieData(
                userId = userId,
                weight = weight,
                height = height,
                age = age,
                gender = gender,
                activityLevel = activityLevel,
                dailyCalories = dailyCalories,
                lastUpdated = System.currentTimeMillis()
            )

            db.collection("calorieData")
                .document(userId)
                .set(calorieData)
                .addOnSuccessListener {
                    _isLoading.value = false
                    _calorieData.value = calorieData
                    _error.value = null
                }
                .addOnFailureListener { e ->
                    _isLoading.value = false
                    _error.value = "Kļūda saglabājot datus: ${e.message}"
                }
        } catch (e: Exception) {
            _isLoading.value = false
            _error.value = "Neizdevās aprēķināt kalorijas: ${e.message}"
        }
    }

    fun clearError() {
        _error.value = null
    }
}