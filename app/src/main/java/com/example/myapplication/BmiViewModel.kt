
package com.example.myapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class BmiViewModel : ViewModel() {
    // saglabā ievadītus datus
    private val _height = MutableLiveData<Float>(170f)
    val height: LiveData<Float> = _height

    private val _weight = MutableLiveData<Float>(70f)
    val weight: LiveData<Float> = _weight

    // rezultats
    private val _bmiResult = MutableLiveData<Float>()
    val bmiResult: LiveData<Float> = _bmiResult

    // kategorija
    private val _bmiCategory = MutableLiveData<String>()
    val bmiCategory: LiveData<String> = _bmiCategory

    init {
        // skaitām ar inicializāciju
        calculateBmi()
    }

    fun updateHeight(height: Float) {
        _height.value = height
        calculateBmi()
    }

    fun updateWeight(weight: Float) {
        _weight.value = weight
        calculateBmi()
    }

    private fun calculateBmi() {
        val currentHeight = _height.value ?: 170f
        val currentWeight = _weight.value ?: 70f

        //  formula kmi:
        val heightInMeters = currentHeight / 100
        val bmi = currentWeight / (heightInMeters * heightInMeters)
        _bmiResult.value = bmi

        // kategorija
        _bmiCategory.value = when {
            bmi < 16 -> "Smags masas deficīts"
            bmi < 18.5 -> "Nepietiekams svars"
            bmi < 25 -> "Normāls svars"
            bmi < 30 -> "Liekais svars"
            bmi < 35 -> "1.pakāpes aptaukošanās"
            bmi < 40 -> "2.pakāpes aptaukošanās"
            else -> "3.pakāpes aptaukošanās"
        }
    }
}