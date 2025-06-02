package com.example.myapplication.pages

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext // Still needed for other uses, but not for ViewModel factory
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.models.WaterViewModel
import com.example.myapplication.R
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.myapplication.models.DailyWater


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterTrackerPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    // REMOVE THE FACTORY HERE!
    // The WaterViewModel now extends AndroidViewModel, so it handles its own application context.
    viewModel: WaterViewModel = viewModel()
) {
    val waterCount by viewModel.waterCount.collectAsState()
    val weeklyWaterStats by viewModel.weeklyWaterStats.collectAsState()
    val isLoadingStats by viewModel.isLoadingStats.collectAsState()
    val statsErrorMessage by viewModel.statsErrorMessage.collectAsState()


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ūdens skaitītājs") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("home") }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back to Home"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WaterTrackerVisual(waterCount = waterCount)

            Text(
                "Glāzes dzērumā: $waterCount",
                style = MaterialTheme.typography.headlineSmall
            )

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { viewModel.decrease() },
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                ) {
                    Text("-", fontSize = 24.sp)
                }
                Button(
                    onClick = { viewModel.increase() },
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                ) {
                    Text("+", fontSize = 24.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Pagājušās 7 dienas:",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            if (isLoadingStats) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (!statsErrorMessage.isNullOrBlank()) {
                Text(
                    text = "Kļūda ielādējot statistiku: ${statsErrorMessage}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    var totalWeeklyWater = 0
                    weeklyWaterStats.forEach { dailyWater ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = dailyWater.date, style = MaterialTheme.typography.bodyLarge)
                            Text(text = "${dailyWater.count} glāzes", style = MaterialTheme.typography.bodyLarge)
                        }
                        totalWeeklyWater += dailyWater.count
                    }
                    if (weeklyWaterStats.isNotEmpty()) {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Kopā: ",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$totalWeeklyWater glāzes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text(text = "Nav datu par pēdējām 7 dienām.", modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}

// DELETE THIS CLASS! It's no longer needed if WaterViewModel extends AndroidViewModel
// class WaterViewModelFactory(private val context: Context) : androidx.lifecycle.ViewModelProvider.Factory {
//     override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
//         if (modelClass.isAssignableFrom(WaterViewModel::class.java)) {
//             @Suppress("UNCHECKED_CAST")
//             return WaterViewModel() as T
//         }
//         throw IllegalArgumentException("Unknown ViewModel class")
//     }
// }

@Composable
fun WaterTrackerVisual(waterCount: Int) {
    val displayedGlasses = minOf(waterCount, 9)

    val columns = when {
        displayedGlasses <= 0 -> 1
        displayedGlasses <= 2 -> displayedGlasses
        displayedGlasses == 3 -> 3
        displayedGlasses <= 6 -> 3
        else -> 3
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier
            .widthIn(min = 150.dp, max = 300.dp)
            .heightIn(min = 150.dp, max = 300.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        userScrollEnabled = false
    ) {
        if (displayedGlasses > 0) {
            items(displayedGlasses) { index ->
                val imageSizeModifier = when (displayedGlasses) {
                    1 -> Modifier.size(120.dp)
                    2 -> Modifier.size(100.dp)
                    3 -> Modifier.size(80.dp)
                    in 4..6 -> Modifier.size(70.dp)
                    else -> Modifier.size(60.dp)
                }

                Image(
                    painter = painterResource(id = R.drawable.local_drink),
                    contentDescription = "Glass ${index + 1}",
                    modifier = imageSizeModifier,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                )
            }
        } else {
            item {
                Text(
                    text = "Vēl neesi dzēris šodien!",
                    modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}