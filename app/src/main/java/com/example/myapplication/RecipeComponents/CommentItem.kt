package com.example.myapplication.RecipeComponents

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.myapplication.RecipeData.Comment

@Composable
fun CommentItem(comment: Comment) {
    Text("${comment.userId}: ${comment.text}")
}
