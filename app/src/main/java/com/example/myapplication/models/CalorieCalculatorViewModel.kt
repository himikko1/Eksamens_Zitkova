package com.example.myapplication.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Date

data class WeightEntry(
    val weight: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)

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

    private val _weightHistory = MutableLiveData<List<WeightEntry>>()
    val weightHistory: LiveData<List<WeightEntry>> = _weightHistory

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    init {
        loadUserCalorieData()
        loadWeightHistory()
    }

    fun loadUserCalorieData() {
        val userId = auth.currentUser?.uid ?: return
        _isLoading.value = true

        db.collection("CalorieCalculator")
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

    fun loadWeightHistory() {
        val userId = auth.currentUser?.uid ?: return
        _isLoading.value = true

        db.collection("CalorieCalculator")
            .document(userId)
            .collection("weight_history")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                _isLoading.value = false
                val history = querySnapshot.documents.mapNotNull { it.toObject(WeightEntry::class.java) }
                _weightHistory.value = history
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = "Kļūda ielādējot svara vēsturi: ${e.message}"
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
            val currentTime = System.currentTimeMillis()

            val calorieData = CalorieData(
                userId = userId,
                weight = weight,
                height = height,
                age = age,
                gender = gender,
                activityLevel = activityLevel,
                dailyCalories = dailyCalories,
                lastUpdated = currentTime
            )

            db.collection("CalorieCalculator")
                .document(userId)
                .set(calorieData)
                .addOnSuccessListener {
                    _calorieData.value = calorieData
                    _error.value = null

                    val weightEntry = WeightEntry(
                        weight = weight,
                        timestamp = currentTime
                    )
                    db.collection("CalorieCalculator")
                        .document(userId)
                        .collection("weight_history")
                        .add(weightEntry)
                        .addOnSuccessListener {
                            _isLoading.value = false
                            loadWeightHistory()
                        }
                        .addOnFailureListener { e ->
                            _isLoading.value = false
                            _error.value = "Kļūda saglabājot svara vēsturi: ${e.message}"
                        }
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
