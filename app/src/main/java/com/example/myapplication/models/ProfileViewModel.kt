import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var listenerRegistration: ListenerRegistration? = null

    private val _imageUris = MutableStateFlow<List<Uri>>(emptyList())
    val imageUris: StateFlow<List<Uri>> = _imageUris

    init {
        fetchImageUris()
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove() // Important to avoid memory leaks
    }

    private fun fetchImageUris() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            listenerRegistration = firestore.collection("users")
                .document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        println("Firestore listener error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val data = snapshot.data
                        val uris = (data?.get("imageCarousel") as? List<String>)?.mapNotNull { Uri.parse(it) }
                        _imageUris.value = uris ?: emptyList()
                    } else {
                        _imageUris.value = emptyList() // Handle case where document doesn't exist or has no data
                    }
                }
        }
    }

    fun addImageUri(uri: Uri) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            viewModelScope.launch {
                val currentUris = _imageUris.value.map { it.toString() }.toMutableList()
                currentUris.add(uri.toString())

                firestore.collection("users")
                    .document(userId)
                    .update("imageCarousel", currentUris)
                    .addOnSuccessListener {
                        println("Image URI added to Firestore")
                    }
                    .addOnFailureListener { e ->
                        println("Error adding image URI to Firestore: ${e.message}")
                    }
            }
        }
    }

    fun deleteImageUri(uriToDelete: Uri) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            viewModelScope.launch {
                val updatedUris = _imageUris.value.filter { it != uriToDelete }.map { it.toString() }

                firestore.collection("users")
                    .document(userId)
                    .update("imageCarousel", updatedUris)
                    .addOnSuccessListener {
                        println("Image URI deleted from Firestore")
                    }
                    .addOnFailureListener { e ->
                        println("Error deleting image URI from Firestore: ${e.message}")
                    }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoManager(viewModel: ProfileViewModel = viewModel()) {
    val imageUris by viewModel.imageUris.collectAsState()

    // Launcher for selecting an image
    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                viewModel.addImageUri(it)
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Before/After Photos", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        // Image Carousel
        if (imageUris.isNotEmpty()) {
            ImageCarouselWithLabels(imageUris = imageUris, onDelete = viewModel::deleteImageUri)
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            Text("No photos added yet.", style = MaterialTheme.typography.body1)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Add Photo Button (Allow up to 2 photos)
        if (imageUris.size < 2) {
            Button(onClick = {
                pickMediaLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Photo")
                Text("Add Photo")
            }
        } else {
            Text("You have added the maximum of 2 photos.", style = MaterialTheme.typography.caption)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageCarouselWithLabels(imageUris: List<Uri>, onDelete: (Uri) -> Unit) {
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { imageUris.size }
    )
    var showDialog by remember { mutableStateOf(false) }
    var currentUriToDelete by remember { mutableStateOf<Uri?>(null) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .height(500.dp) // Adjust height as needed
                .fillMaxWidth()
        ) { page ->
            val currentImageUri = imageUris[page]
            Box(contentAlignment = Alignment.BottomCenter) {
                Image(
                    painter = rememberAsyncImagePainter(model = currentImageUri),
                    contentDescription = "Carousel Image",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(MaterialTheme.shapes.medium)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    currentUriToDelete = currentImageUri
                                    showDialog = true
                                }
                            )
                        },
                    contentScale = ContentScale.Crop
                )
                if (imageUris.size == 2) {
                    val label = when (page) {
                        0 -> "Photo Before"
                        1 -> "Photo After"
                        else -> "" // Should not happen if size is 2
                    }
                    if (label.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = Color.White,
                                style = MaterialTheme.typography.caption
                            )
                        }
                    }
                } else if (imageUris.size == 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Photo",
                            color = Color.White,
                            style = MaterialTheme.typography.caption
                        )
                    }
                }
            }
        }

        // Page Indicator
        if (imageUris.size > 1) {
            Row(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(imageUris.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration) Color.Blue else Color.Gray
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(8.dp)
                    )
                }
            }
        }

        // Confirmation Dialog
        if (showDialog && currentUriToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Delete Photo?") },
                text = { Text("Are you sure you want to delete this photo?") },
                confirmButton = {
                    TextButton(onClick = {
                        currentUriToDelete?.let { onDelete(it) }
                        showDialog = false
                        currentUriToDelete = null
                    }) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false; currentUriToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}