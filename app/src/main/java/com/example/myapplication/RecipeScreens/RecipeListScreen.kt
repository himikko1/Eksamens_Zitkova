package com.example.myapplication.RecipeScreens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.RecipeComponents.RecipeItem
import com.example.myapplication.models.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    navController: NavController,
    viewModel: RecipeViewModel = viewModel()
) {
    val recipes by viewModel.recipes.observeAsState(listOf())
    val isLoading by viewModel.isLoading.observeAsState(false)

    // Refresh recipes when screen opens
    LaunchedEffect(key1 = Unit) {
        viewModel.loadRecipes()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Receptes") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("addRecipe") }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Pievienot recepti")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (recipes.isEmpty()) {
                Text(
                    text = "Nav pievienotu recepšu. Pievienojiet pirmo, nospiežot + pogu.",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(recipes) { recipe ->
                        RecipeItem(recipe) {
                            navController.navigate("recipeDetail/${recipe.id}")
                        }
                    }
                }
            }
        }
    }
}