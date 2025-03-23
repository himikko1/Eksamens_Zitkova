//package com.example.myapplication.RecipeScreens
//
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.Favorite
//import androidx.compose.material.icons.filled.Send
//import androidx.compose.material.icons.filled.Timer
//import androidx.compose.material3.Button
//import androidx.compose.material3.Card
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.Divider
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.material3.TopAppBar
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.livedata.observeAsState
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavController
//import com.example.myapplication.RecipeComponents.CommentItem
//import com.example.myapplication.RecipeData.Comment
//import com.example.myapplication.models.RecipeViewModel
//import java.text.SimpleDateFormat
//import java.util.Date
//import java.util.Locale
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun RecipeDetailScreen(
//    recipeId: String,
//    navController: NavController,
//    viewModel: RecipeViewModel = viewModel()
//) {
//    val recipe by viewModel.currentRecipe.observeAsState()
//    val isLoading by viewModel.isLoading.observeAsState(false)
//    val error by viewModel.error.observeAsState()
//
//    var commentText by remember { mutableStateOf("") }
//
//    // Load recipe on screen open
//    LaunchedEffect(key1 = recipeId) {
//        viewModel.getRecipeById(recipeId)
//    }
//
//    // Load comments for this recipe
//    val comments by viewModel.getCommentsForRecipe(recipeId).observeAsState(emptyList())
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text(recipe?.title ?: "Receptes detaļas") },
//                navigationIcon = {
//                    IconButton(onClick = { navController.popBackStack() }) {
//                        Icon(Icons.Default.ArrowBack, contentDescription = "Atpakaļ")
//                    }
//                }
//            )
//        }
//    ) { paddingValues ->
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//        ) {
//            if (isLoading) {
//                CircularProgressIndicator(
//                    modifier = Modifier.align(Alignment.Center)
//                )
//            } else if (error != null) {
//                Text(
//                    text = error ?: "",
//                    color = MaterialTheme.colorScheme.error,
//                    modifier = Modifier
//                        .align(Alignment.Center)
//                        .padding(16.dp)
//                )
//            } else if (recipe == null) {
//                Text(
//                    text = "Recepte nav atrasta",
//                    modifier = Modifier
//                        .align(Alignment.Center)
//                        .padding(16.dp)
//                )
//            } else {
//                Column(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(16.dp)
//                        .verticalScroll(rememberScrollState())
//                ) {
//                    // Recipe details
//                    Text(
//                        text = recipe!!.title,
//                        fontSize = 24.sp,
//                        fontWeight = FontWeight.Bold
//                    )
//
//                    Spacer(modifier = Modifier.height(8.dp))
//
//                    Text(
//                        text = "Autors: ${recipe!!.authorName}",
//                        fontSize = 14.sp,
//                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
//                    )
//
//                    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
//                    val date = Date(recipe!!.timestamp)
//
//                    Text(
//                        text = "Pievienots: ${dateFormat.format(date)}",
//                        fontSize = 14.sp,
//                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
//                    )
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    // Recipe metadata
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceBetween
//                    ) {
//                        Row(verticalAlignment = Alignment.CenterVertically) {
//                            Icon(
//                                Icons.Default.Timer,
//                                contentDescription = "Laiks",
//                                tint = MaterialTheme.colorScheme.primary
//                            )
//                            Text(
//                                text = " ${recipe!!.prepTime} min",
//                                fontSize = 16.sp,
//                                modifier = Modifier.padding(start = 4.dp)
//                            )
//                        }
//
//                        Text(
//                            text = "${recipe!!.calories} kcal",
//                            fontSize = 16.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = MaterialTheme.colorScheme.tertiary
//                        )
//
//                        Row(verticalAlignment = Alignment.CenterVertically) {
//                            Text(
//                                text = "${recipe!!.likes}",
//                                fontSize = 16.sp,
//                                modifier = Modifier.padding(end = 4.dp)
//                            )
//                            Icon(
//                                Icons.Default.Favorite,
//                                contentDescription = "Patīk",
//                                tint = MaterialTheme.colorScheme.error
//                            )
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    // Description
//                    Text(
//                        text = recipe!!.description,
//                        fontSize = 16.sp,
//                        lineHeight = 24.sp
//                    )
//
//                    Spacer(modifier = Modifier.height(24.dp))
//
//                    // Ingredients
//                    Text(
//                        text = "Sastāvdaļas",
//                        fontSize = 18.sp,
//                        fontWeight = FontWeight.Bold
//                    )
//
//                    Spacer(modifier = Modifier.height(8.dp))
//
//                    recipe!!.ingredients.forEachIndexed { index, ingredient ->
//                        Text(
//                            text = "• $ingredient",
//                            fontSize = 16.sp,
//                            modifier = Modifier.padding(vertical = 4.dp)
//                        )
//                    }
//
//                    Spacer(modifier = Modifier.height(24.dp))
//
//                    // Steps
//                    Text(
//                        text = "Pagatavošana",
//                        fontSize = 18.sp,
//                        fontWeight = FontWeight.Bold
//                    )
//
//                    Spacer(modifier = Modifier.height(8.dp))
//
//                    recipe!!.steps.forEachIndexed { index, step ->
//                        Text(
//                            text = "${index + 1}. $step",
//                            fontSize = 16.sp,
//                            modifier = Modifier.padding(vertical = 8.dp)
//                        )
//                    }
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    // Like button
//                    Button(
//                        onClick = { viewModel.likeRecipe(recipeId) },
//                        modifier = Modifier.fillMaxWidth()
//                    ) {
//                        Icon(
//                            Icons.Default.Favorite,
//                            contentDescription = "Patīk",
//                            modifier = Modifier.padding(end = 8.dp)
//                        )
//                        Text("Patīk šī recepte (${recipe!!.likes})")
//                    }
//
//                    Spacer(modifier = Modifier.height(24.dp))
//
//                    // Comments section
//                    Card(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(vertical = 8.dp)
//                    ) {
//                        Column(modifier = Modifier.padding(16.dp)) {
//                            Text(
//                                text = "Komentāri (${comments.size})",
//                                fontSize = 18.sp,
//                                fontWeight = FontWeight.Bold
//                            )
//
//                            Spacer(modifier = Modifier.height(8.dp))
//
//                            if (comments.isEmpty()) {
//                                Text(
//                                    text = "Nav komentāru. Pievienojiet pirmo!",
//                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
//                                )
//                            } else {
//                                comments.forEach { comment ->
//                                    Column(
//                                        modifier = Modifier
//                                            .fillMaxWidth()
//                                            .padding(vertical = 8.dp)
//                                    ) {
//                                        Row(