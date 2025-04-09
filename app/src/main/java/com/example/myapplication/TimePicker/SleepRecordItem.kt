package com.example.myapplication.TimePicker

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.models.SleepRecord


@Composable
fun SleepRecordItem(record: SleepRecord) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = formatDate(record.date),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Miegs: ${formatTime(record.sleepTime)}")
                Text("Atmoda: ${formatTime(record.wakeTime)}")
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text("Ilgums: ${String.format("%.1f stundas", record.durationInHours)}")

            if (record.quality > 0) {
                Row {
                    for (i in 1..5) {
                        Icon(
                            imageVector = if (i <= record.quality) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = null,
                            tint = if (i <= record.quality) Color.Yellow else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (record.notes.isNotEmpty()) {
                Text(
                    text = record.notes,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}