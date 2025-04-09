
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoManager(viewModel: ProfileViewModel = viewModel()) {
    val imageUris by viewModel.imageUris.collectAsState()
    var showAddButtons by remember { mutableStateOf(true) }

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
        Text("Profile Page", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        // Image Carousel
        if (imageUris.isNotEmpty()) {
            ImageCarousel(imageUris = imageUris)
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
fun ImageCarousel(imageUris: List<Uri>) {
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { imageUris.size }
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(
            //pageCount = imageUris.size, // Ensure pageCount is correctly set
            state = pagerState,
            modifier = Modifier
                .height(200.dp) // Adjust height as needed
                .fillMaxWidth()
        ) { page ->
            Image(
                painter = rememberAsyncImagePainter(model = imageUris[page]),
                contentDescription = "Carousel Image",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )
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
    }
}