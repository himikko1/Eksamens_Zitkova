package com.example.myapplication.TimePicker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.models.SleepViewModel
import com.example.sleeptracker.ui.components.PeriodTab
import com.example.sleeptracker.ui.components.SleepRecordItem
import com.example.sleeptracker.viewmodel.SleepViewModel
import java.util.*

// statistikas ekrāns
@Composable
fun SleepStatsScreen(
    viewModel: SleepViewModel,
    navController: NavController
) {
    val sleepRecords by viewModel.sleepRecords.collectAsState()
    var selectedPeriod by remember { mutableStateOf("week") }

    LaunchedEffect(selectedPeriod) {
        val cal = Calendar.getInstance()
        val endTime = cal.timeInMillis

        when (selectedPeriod) {
            "week" -> {
                cal.add(Calendar.DAY_OF_YEAR, -7)
                viewModel.fetchSleepRecordsForPeriod(cal.timeInMillis, endTime)
            }
            "month" -> {
                cal.add(Calendar.MONTH, -1)
                viewModel.fetchSleepRecordsForPeriod(cal.timeInMillis, endTime)
            }
            "year" -> {
                cal.add(Calendar.YEAR, -1)
                viewModel.fetchSleepRecordsForPeriod(cal.timeInMillis, endTime)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Moegas statistika",
            style = MaterialTheme.typography.headlineMedium
        )

        // selektors perioda
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            PeriodTab("Nedēļa", selectedPeriod == "week") { selectedPeriod = "week" }
            PeriodTab("Mēnesis", selectedPeriod == "month") { selectedPeriod = "month" }
            PeriodTab("Gads", selectedPeriod == "year") { selectedPeriod = "year" }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // statistika
        if (sleepRecords.isNotEmpty()) {
            val avgDuration = sleepRecords.map { it.durationInHours }.average()

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Vidējais miega ilgums",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = String.format("%.1f stundas", avgDuration),
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Ierakstu skaits: ${sleepRecords.size}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Par izvēlēto periodu nav miega datu",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Miega ierakstu saraksts
        LazyColumn {
            items(sleepRecords) { record ->
                SleepRecordItem(record)
            }
        }

        // Pievienot jaunu ierakstu poga
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = { navController.navigate("add_sleep_record") },
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Pievienot ierakstu")
            }
        }
    }
}