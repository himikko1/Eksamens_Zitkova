package com.example.myapplication.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.viewmodel.WaterViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.R

@Composable
fun WaterTrackerPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: WaterViewModel = viewModel()
) {
    val waterCount by viewModel.waterCount.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Water Tracker", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(20.dp))

        // Отображение уровня воды, например иконка кружки или шкала
        Image(
            painter = painterResource(R.drawable.local_drink), // добавь свою иконку кружки
            contentDescription = "Cup",
            modifier = Modifier
                .height((100 + waterCount * 10).dp)
                .width(100.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text("Стаканов выпито: $waterCount")

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = { viewModel.decrease() }) {
                Text("-")
            }
            Button(onClick = { viewModel.increase() }) {
                Text("+")
            }
        }
    }
}

