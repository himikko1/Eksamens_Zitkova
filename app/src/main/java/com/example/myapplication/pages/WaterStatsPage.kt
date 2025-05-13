//package com.example.myapplication.pages
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavController
//import com.example.myapplication.models.WaterStatsViewModel
//import com.example.myapplication.models.WaterStatsPeriod
//import com.example.myapplication.models.WaterStats
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun WaterStatsPage(
//    modifier: Modifier = Modifier,
//    navController: NavController,
//    viewModel: WaterStatsViewModel = viewModel()
//) {
//    val waterStats by viewModel.waterStats.collectAsState()
//    val isLoading by viewModel.isLoading.collectAsState()
//    val errorMessage by viewModel.errorMessage.collectAsState()
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Water Statistics") },
//                navigationIcon = {
//                    IconButton(onClick = { navController.navigateUp() }) { // Use navigateUp() to go back
//                        Icon(
//                            imageVector = Icons.Default.ArrowBack,
//                            contentDescription = "Back"
//                        )
//                    }
//                }
//            )
//        }
//    ) { paddingValues ->
//        Column(
//            modifier = modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//                .padding(16.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Top
//        ) {
//            Text("Your Water Intake Statistics", style = MaterialTheme.typography.headlineMedium)
//            Spacer(modifier = Modifier.height(24.dp))
//
//            if (isLoading) {
//                CircularProgressIndicator()
//            } else if (errorMessage != null) {
//                Text(
//                    text = "Error: $errorMessage",
//                    color = MaterialTheme.colorScheme.error,
//                    style = MaterialTheme.typography.bodyMedium
//                )
//            } else if (waterStats.isEmpty()) {
//                Text("No water data available.", style = MaterialTheme.typography.bodyMedium)
//            } else {
//                WaterStatsList(waterStats = waterStats)
//            }
//        }
//    }
//}
//
//@Composable
//fun WaterStatsList(waterStats: List<WaterStats>) {
//    Column(
//        modifier = Modifier.fillMaxWidth(),
//        verticalArrangement = Arrangement.spacedBy(8.dp)
//    ) {
//        waterStats.forEach { stats ->
//            WaterStatItem(stats = stats)
//        }
//    }
//}
//
//@Composable
//fun WaterStatItem(stats: WaterStats) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(
//                text = when (stats.period) {
//                    WaterStatsPeriod.WEEK -> "Last 7 Days"
//                    WaterStatsPeriod.TEN_DAYS -> "Last 10 Days"
//                    WaterStatsPeriod.MONTH -> "Last 30 Days" // Or "Last Month" depending on interpretation
//                },
//                style = MaterialTheme.typography.bodyLarge
//            )
//            Text(
//                text = "${stats.totalGlasses} glasses",
//                style = MaterialTheme.typography.headlineSmall,
//                color = MaterialTheme.colorScheme.primary
//            )
//        }
//    }
//}
