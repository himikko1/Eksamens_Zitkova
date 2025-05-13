package com.example.myapplication.pages

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

data class TrainingVideo(val title: String, val youtubeUrl: String)

@Composable
fun TrainingVideosPage(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var videoList by remember { mutableStateOf<List<TrainingVideo>>(emptyList()) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        db.collection("training_videos").get()
            .addOnSuccessListener { result ->
                val videos = result.documents.mapNotNull { doc ->
                    val title = doc.getString("title")
                    val url = doc.getString("youtubeUrl")
                    println("Fetched video: title=$title, url=$url")
                    if (title != null && url != null) TrainingVideo(title, url) else null
                }
                videoList = videos
            }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text(
            text = "Treniņš",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        for (video in videoList) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(video.youtubeUrl))
                        context.startActivity(intent)
                    },
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = video.title, style = MaterialTheme.typography.bodyLarge)
                    Text(text = video.youtubeUrl, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
