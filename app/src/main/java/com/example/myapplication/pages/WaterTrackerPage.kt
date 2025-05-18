package com.example.myapplication.pages

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterTrackerPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: WaterViewModel = viewModel(
        factory = WaterViewModelFactory(LocalContext.current)
    )
) {
    val waterCount by viewModel.waterCount.collectAsState()

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
            verticalArrangement = Arrangement.Center
        ) {
            // Replace the single Image with the WaterTrackerVisual composable
            WaterTrackerVisual(waterCount = waterCount)

            Spacer(modifier = Modifier.height(20.dp))

            Text("Glāzes dzērumā: $waterCount")

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
}

class WaterViewModelFactory(private val context: Context) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WaterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WaterViewModel(appContext = context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun WaterTrackerVisual(waterCount: Int) {
    val displayedGlasses = minOf(waterCount, 9)

    val columns = when (displayedGlasses) {
        1 -> 1
        2 -> 2
        in 3..3 -> displayedGlasses
        in 4..4 -> 2
        in 5..6 -> 3
        else -> 3
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier
            .width(200.dp) // Fixed width for the grid
            .height(200.dp), // Fixed height for the grid
        verticalArrangement = Arrangement.Center,
        horizontalArrangement = Arrangement.Center
    ) {
        items(displayedGlasses) { index ->
            val imageModifier = when (displayedGlasses) {
                1 -> Modifier.size(100.dp)
                2 -> Modifier.fillMaxWidth().aspectRatio(1f)
                3 -> Modifier.fillMaxSize().aspectRatio(1f)
                4 -> Modifier.fillMaxWidth().aspectRatio(1f)
                else -> Modifier.size(50.dp).padding(4.dp)
            }

            Image(
                painter = painterResource(id = R.drawable.local_drink),
                contentDescription = "Glass ${index + 1}",
                modifier = imageModifier,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
            )
        }
    }
}