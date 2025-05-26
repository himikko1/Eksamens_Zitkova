import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore

data class TrainingVideo(val title: String, val youtubeUrl: String) {
    fun getYouTubeVideoId(): String? {
        val pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embedCE%2F|youtu.be%2F|%2Fv%2F)[^#&?\\n]*"
        val compiledPattern = Regex(pattern)
        val matcher = compiledPattern.find(youtubeUrl)
        return matcher?.value
    }

    fun getThumbnailUrl(): String? {
        val videoId = getYouTubeVideoId()
        return videoId?.let { "https://img.youtube.com/vi/$it/hqdefault.jpg" }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingVideosPage(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var videoList by remember { mutableStateOf<List<TrainingVideo>>(emptyList()) }
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        db.collection("training_videos").get()
            .addOnSuccessListener { result ->
                val videos = result.documents.mapNotNull { doc ->
                    val title = doc.getString("title")
                    val url = doc.getString("youtubeUrl")
                    if (title != null && url != null) TrainingVideo(title, url) else null
                }
                videoList = videos
                isLoading = false
            }
            .addOnFailureListener { e ->
                errorMessage = "Failed to load videos: ${e.message}"
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Treniņš") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            } else if (videoList.isEmpty()) {Text(
                text = "No training videos found.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = TextAlign.Center
            )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(videoList) { video ->
                        VideoCard(video = video, onVideoClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(video.youtubeUrl))
                            context.startActivity(intent)
                        })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoCard(video: TrainingVideo, onVideoClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onVideoClick),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                val thumbnailUrl = video.getThumbnailUrl()
                if (thumbnailUrl != null) {
                    Image(
                        painter = rememberAsyncImagePainter(model = thumbnailUrl),
                        contentDescription = "Video Thumbnail for ${video.title}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "No Thumbnail Available",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(48.dp)
                    )
                }

                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Play Video",
                    tint = Color.White,
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.Black.copy(alpha = 0.5f), shape = MaterialTheme.shapes.extraLarge)
                        .padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}
