package com.example.myapplication.RecipeData

data class Recipe(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val ingredients: List<String> = listOf(),
    val steps: List<String> = listOf(),
    val prepTime: Int = 0,  // in minutes
    val calories: Int = 0,
    val likes: Int = 0,
    val authorId: String = "",
    val authorName: String = "",
    val timestamp: Long = System.currentTimeMillis()
)