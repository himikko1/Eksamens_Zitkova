package com.example.myapplication.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.models.CalorieCalculatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalorieCalculatorPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    calorieCalculatorViewModel: CalorieCalculatorViewModel
) {
    val calorieData by calorieCalculatorViewModel.calorieData.observeAsState()
    val isLoading by calorieCalculatorViewModel.isLoading.observeAsState(false)
    val error by calorieCalculatorViewModel.error.observeAsState()

    var weight by remember { mutableStateOf(calorieData?.weight?.toString() ?: "") }
    var height by remember { mutableStateOf(calorieData?.height?.toString() ?: "") }
    var age by remember { mutableStateOf(calorieData?.age?.toString() ?: "") }
    var gender by remember { mutableStateOf(calorieData?.gender ?: "male") }
    var activityLevel by remember { mutableStateOf(calorieData?.activityLevel ?: "moderate") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kaloriju kalkulators") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atpakaļ")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }

            if (error != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Ieejas dati
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Svars (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = height,
                onValueChange = { height = it },
                label = { Text("Augstums (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Vecums") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // Dzimums
            Text(
                text = "Dzimums:",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = gender == "male",
                    onClick = { gender = "male" }
                )
                Text("Vīrietis", modifier = Modifier.padding(start = 8.dp))

                Spacer(modifier = Modifier.weight(1f))

                RadioButton(
                    selected = gender == "female",
                    onClick = { gender = "female" }
                )
                Text("Sieviete", modifier = Modifier.padding(start = 8.dp))
            }

            // Aktivitātes līmenis
            Text(
                text = "Aktivitātes līmenis:",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp)
            )

            ActivityLevelSelector(
                selectedLevel = activityLevel,
                onLevelSelected = { activityLevel = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Aprēķināšanas poga
            Button(
                onClick = {
                    try {
                        val weightVal = weight.toDoubleOrNull()
                        val heightVal = height.toDoubleOrNull()
                        val ageVal = age.toIntOrNull()

                        if (weightVal == null || heightVal == null || ageVal == null) {
                            // You could show a temporary error here if needed
                            calorieCalculatorViewModel.clearError()
                            return@Button
                        }

                        if (weightVal > 0 && heightVal > 0 && ageVal > 0) {
                            calorieCalculatorViewModel.calculateAndSaveCalories(
                                weight = weightVal,
                                height = heightVal,
                                age = ageVal,
                                gender = gender,
                                activityLevel = activityLevel
                            )
                        } else {
                            calorieCalculatorViewModel.clearError()
                        }
                    } catch (e: Exception) {
                        // Handle any unexpected errors
                        calorieCalculatorViewModel.clearError()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Icon(
                    Icons.Default.Calculate,
                    contentDescription = "Aprēķināt",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Aprēķināt kalorijas")
            }

            // Rezultāts
            calorieData?.let { data ->
                if (data.dailyCalories > 0) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Jūsu ieteicamās dienas kalorijas:",
                                fontSize = 16.sp
                            )
                            Text(
                                text = "${data.dailyCalories.toInt()} kcal",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityLevelSelector(
    selectedLevel: String,
    onLevelSelected: (String) -> Unit
) {
    val levels = listOf(
        "sedentary" to "Mazkustīgs (nav fizisku aktivitāšu)",
        "light" to "Viegls (1-3 reizes nedēļā)",
        "moderate" to "Vidējs (3-5 reizes nedēļā)",
        "active" to "Aktīvs (6-7 reizes nedēļā)",
        "very_active" to "Ļoti aktīvs (intensīvas fiziskas aktivitātes)"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        levels.forEach { (level, description) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedLevel == level,
                    onClick = { onLevelSelected(level) }
                )
                Text(description, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}