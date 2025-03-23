//package com.example.myapplication.RecipeScreens
//
//import androidx.compose.foundation.layout.Column
//import androidx.compose.material3.Button
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextField
//import androidx.compose.runtime.*
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.example.myapplication.models.RecipeViewModel
//
//@Composable
//fun CommentsScreen(recipeId: String, viewModel: RecipeViewModel = viewModel()) {
//    var commentText by remember { mutableStateOf("") }
//
//    Column {
//        CommentList(recipeId, viewModel)
//        TextField(value = commentText, onValueChange = { commentText = it }, label = { Text("Komentārs:") })
//        Button(onClick = {
//            viewModel.addComment(recipeId, commentText)
//            commentText = ""
//        }) {
//            Text("Pievienot komentāru:")
//        }
//    }
//}
//
//@Composable
//fun CommentList(recipeId: String, viewModel: RecipeViewModel = viewModel()) {
//    val comments by viewModel.getComments(recipeId).observeAsState(emptyList())
//
//    Column {
//        comments.forEach { comment ->
//            Text("${comment.userId}: ${comment.text}")
//        }
//    }
//}
