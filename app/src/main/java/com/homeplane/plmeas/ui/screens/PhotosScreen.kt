package com.homeplane.plmeas.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.homeplane.plmeas.data.entity.PhotoItem
import com.homeplane.plmeas.ui.components.*
import com.homeplane.plmeas.ui.theme.*
import com.homeplane.plmeas.viewmodel.AppViewModel

private val photoCategories = listOf("All", "Before", "Design Ideas", "After")

@Composable
fun PhotosScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val photos by viewModel.photos.collectAsStateWithLifecycle()
    var selectedCategory by remember { mutableStateOf("All") }
    var showPickerFor by remember { mutableStateOf<String?>(null) }
    var deleteTarget by remember { mutableStateOf<PhotoItem?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.addPhoto(it.toString(), "", showPickerFor ?: "Design Ideas")
        }
        showPickerFor = null
    }

    val filtered = if (selectedCategory == "All") photos else photos.filter { it.category == selectedCategory }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenTopBar(
            title = "Photos",
            onBack = onBack,
            actions = {
                IconButton(onClick = { showPickerFor = selectedCategory.takeIf { it != "All" } ?: "Design Ideas" }) {
                    Icon(Icons.Filled.AddPhotoAlternate, contentDescription = "Add Photo")
                }
            }
        )

        ChipRow(
            items = photoCategories,
            selected = selectedCategory,
            onSelect = { selectedCategory = it },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        if (photos.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    EmptyStateView(
                        icon = Icons.Filled.PhotoLibrary,
                        title = "No photos yet",
                        subtitle = "Capture before/after shots and design inspiration"
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { showPickerFor = "Design Ideas" },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Add Photo")
                    }
                }
            }
        } else if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No photos in \"$selectedCategory\"", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = { showPickerFor = selectedCategory }) { Text("Add to $selectedCategory") }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered) { photo ->
                    PhotoCard(photo = photo, onDelete = { deleteTarget = photo })
                }
            }
        }
    }

    LaunchedEffect(showPickerFor) {
        if (showPickerFor != null) {
            galleryLauncher.launch("image/*")
        }
    }

    deleteTarget?.let { photo ->
        ConfirmDeleteDialog(
            title = "Delete Photo",
            message = "Remove this photo?",
            onConfirm = { viewModel.deletePhoto(photo); deleteTarget = null },
            onDismiss = { deleteTarget = null }
        )
    }
}

@Composable
private fun PhotoCard(photo: PhotoItem, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = photo.uri,
                contentDescription = photo.description,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Category badge
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color.Black.copy(alpha = 0.5f)
            ) {
                Text(
                    photo.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete", tint = Color.White)
            }
            // Description
            if (photo.description.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    color = Color.Black.copy(alpha = 0.5f)
                ) {
                    Text(
                        photo.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        modifier = Modifier.padding(8.dp),
                        maxLines = 2
                    )
                }
            }
        }
    }
}
