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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.models.CalorieCalculatorViewModel
import com.example.myapplication.models.WeightEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalorieHistoryPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    calorieCalculatorViewModel: CalorieCalculatorViewModel
) {
    val calorieData by calorieCalculatorViewModel.calorieData.observeAsState()
    val weightHistory by calorieCalculatorViewModel.weightHistory.observeAsState(emptyList())
    val isLoading by calorieCalculatorViewModel.isLoading.observeAsState(false)

    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    val dateFormatShort = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) // Also declare short format here

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kaloriju vēsture") },
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
            calorieData?.let { data ->
                if (data.dailyCalories > 0) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Pēdējie aprēķini",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            DataRow("Dienas kalorijas", "${data.dailyCalories.toInt()} kcal")
                            DataRow("Svars", "${data.weight} kg")
                            DataRow("Augstums", "${data.height} cm")
                            DataRow("Vecums", "${data.age} gadi")
                            DataRow("Dzimums", if (data.gender == "male") "Vīrietis" else "Sieviete")
                            DataRow("Aktivitātes līmenis", activityLevelText(data.activityLevel))

                            val date = Date(data.lastUpdated)
                            DataRow("Pēdējā atjaunošana", dateFormat.format(date))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                //svara statiskika
                    if (weightHistory.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Svara vēsture un statistika",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                val sortedHistory = weightHistory.sortedBy { it.timestamp }

                                if (sortedHistory.size > 1) {
                                    val oldestWeight = sortedHistory.first().weight
                                    val oldestDate = Date(sortedHistory.first().timestamp)
                                    val newestWeight = sortedHistory.last().weight
                                    val newestDate = Date(sortedHistory.last().timestamp)

                                    Text(
                                        text = "Svars (vecākais): ${oldestWeight} kg (${dateFormatShort.format(oldestDate)})",
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Svars (jaunākais): ${newestWeight} kg (${dateFormatShort.format(newestDate)})",
                                        fontSize = 14.sp
                                    )

                                    val weightChange = newestWeight - oldestWeight
                                    val changeText = String.format("%.1f", abs(weightChange))
                                    val changeColor = when {
                                        weightChange > 0 -> Color.Red
                                        weightChange < 0 -> Color.Green
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Izmaiņas kopš pirmā ieraksta: ${if (weightChange >= 0) "+" else "-"}${changeText} kg",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = changeColor
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Pēdējie svara ieraksti:",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    weightHistory.take(5).forEach { entry ->
                                        val entryDate = Date(entry.timestamp)
                                        DataRow(dateFormat.format(entryDate), "${entry.weight} kg")
                                    }
                                } else {
                                    Text(
                                        text = "Pirmais svara ieraksts: ${sortedHistory.first().weight} kg",
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Nepieciešami vairāki ieraksti, lai parādītu izmaiņas.",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    } else if (!isLoading) {
                        Text(
                            text = "Nav svara vēstures datu. Katru reizi, kad aprēķināsiet kalorijas, tiks saglabāts jūsu svars.",
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }


                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Ieteikumi",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "Balstoties uz jūsu profilā norādītajiem datiem, jūsu ieteicamais dienās kaloriju daudzums ir ${data.dailyCalories.toInt()} kcal.",
                                fontSize = 14.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "Lai sasniegtu svara samazināšanas mērķus, mēģiniet uzņemt par 300-500 kcal mazāk nekā jūsu ieteicamais daudzums.",
                                fontSize = 14.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "Lai palielinātu svaru, mēģiniet uzņemt par 300-500 kcal vairāk nekā jūsu ieteicamais daudzums.",
                                fontSize = 14.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "Atcerieties, ka šīs ir tikai aptuvenas vērtības. Individuālie rezultāti var atšķirties atkarībā no jūsu ķermeņa sastāva, veselības stāvokļa un citiem faktoriem.",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Uztura ieteikumi",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "Iekļaujiet savā uzturā šādas uzturvielas:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                "• Olbaltumvielas: 10-35% no kopējā kaloriju daudzuma",
                                fontSize = 14.sp
                            )
                            Text(
                                "• Ogļhidrāti: 45-65% no kopējā kaloriju daudzuma",
                                fontSize = 14.sp
                            )
                            Text(
                                "• Tauki: 20-35% no kopējā kaloriju daudzuma",
                                fontSize = 14.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "Dzert pietiekami daudz ūdens ir svarīgi labai veselībai. Centieties dzert vismaz 8 glāzes (aptuveni 2 litrus) ūdens dienā.",
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    Text(
                        "Nav pieejamu kaloriju aprēķinu datu. Lūdzu, aprēķiniet savas kalorijas Kaloriju kalkulatora sadaļā.",
                        modifier = Modifier.padding(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

fun activityLevelText(level: String): String {
    return when (level) {
        "sedentary" -> "Mazkustīgs"
        "light" -> "Viegls (1-3 reizes nedēļā)"
        "moderate" -> "Vidējs (3-5 reizes nedēļā)"
        "active" -> "Aktīvs (6-7 reizes nedēļā)"
        "very_active" -> "Ļoti aktīvs (intensīvas fiziskas aktivitātes)"
        else -> "Nav norādīts"
    }
}
