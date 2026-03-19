package com.homeplane.plmeas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.homeplane.plmeas.data.entity.InteriorRoom
import com.homeplane.plmeas.navigation.Routes
import com.homeplane.plmeas.ui.components.*
import com.homeplane.plmeas.ui.theme.*
import com.homeplane.plmeas.viewmodel.AppViewModel

private val roomTypeGradients = mapOf(
    "Living Room" to listOf(DustyBlue, Color(0xFF4A8FDE)),
    "Kitchen" to listOf(Sand, Color(0xFFD4A843)),
    "Bedroom" to listOf(Color(0xFFB39DDB), Color(0xFF7E57C2)),
    "Bathroom" to listOf(Color(0xFF80DEEA), Color(0xFF00ACC1)),
    "Hallway" to listOf(Olive, Color(0xFF6B7A5D)),
    "Office" to listOf(Terracotta, Color(0xFFE9A062)),
    "Other" to listOf(Color(0xFFBCAAA4), Color(0xFF8D6E63))
)

@Composable
fun RoomsScreen(viewModel: AppViewModel, navController: NavController) {
    val rooms by viewModel.rooms.collectAsStateWithLifecycle()
    val activeProject by viewModel.activeProject.collectAsStateWithLifecycle()
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val units = prefs.units
    var deleteTarget by remember { mutableStateOf<InteriorRoom?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenTopBar(
            title = "Rooms",
            actions = {
                IconButton(onClick = { navController.navigate(Routes.ADD_ROOM) }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Room")
                }
            }
        )

        if (activeProject == null) {
            Column(modifier = Modifier.fillMaxSize()) {
                NoProjectBanner(onCreateProject = { navController.navigate(Routes.PROJECTS) })
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    EmptyStateView(
                        icon = Icons.Filled.FolderOpen,
                        title = "No active project",
                        subtitle = "Create or select a project to start adding rooms"
                    )
                }
            }
        } else if (rooms.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyStateView(
                    icon = Icons.Filled.GridView,
                    title = "No rooms yet",
                    subtitle = "Tap + to add your first room"
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(rooms) { room ->
                    RoomCard(
                        room = room,
                        units = units,
                        onClick = { navController.navigate(Routes.roomDetail(room.id)) },
                        onDelete = { deleteTarget = room }
                    )
                }
            }
        }
    }

    deleteTarget?.let { room ->
        ConfirmDeleteDialog(
            title = "Delete Room",
            message = "Delete \"${room.name}\"?",
            onConfirm = { viewModel.deleteRoom(room); deleteTarget = null },
            onDismiss = { deleteTarget = null }
        )
    }
}

@Composable
fun AddRoomScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val units = prefs.units
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Living Room") }
    var style by remember { mutableStateOf("") }
    var width by remember { mutableStateOf("") }
    var length by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val roomTypes = listOf("Living Room", "Kitchen", "Bedroom", "Bathroom", "Hallway", "Office", "Other")

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenTopBar(title = "Add Room", onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Room Name *") },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )

            Text("Room Type", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            ChipRow(items = roomTypes, selected = type, onSelect = { type = it })

            OutlinedTextField(
                value = style, onValueChange = { style = it },
                label = { Text("Style") },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                placeholder = { Text("e.g. Scandinavian, Modern") }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = width, onValueChange = { width = it },
                    label = { Text("Width ($units)") },
                    modifier = Modifier.weight(1f), singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = length, onValueChange = { length = it },
                    label = { Text("Length ($units)") },
                    modifier = Modifier.weight(1f), singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                )
            }
            OutlinedTextField(
                value = notes, onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(), minLines = 3, maxLines = 4
            )
            Spacer(Modifier.weight(1f))
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        viewModel.addRoom(name, type, style, width.toFloatOrNull() ?: 0f, length.toFloatOrNull() ?: 0f, notes)
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) { Text("Save Room") }
        }
    }
}

@Composable
private fun RoomCard(room: InteriorRoom, units: String, onClick: () -> Unit, onDelete: () -> Unit) {
    val gradient = roomTypeGradients[room.type] ?: roomTypeGradients["Other"]!!

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(gradient)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.GridView, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                Box(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete", tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(room.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(room.type, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (room.area > 0) {
                Spacer(Modifier.height(4.dp))
                Text("${room.area.toInt()} ${units}²", style = MaterialTheme.typography.bodySmall, color = Terracotta, fontWeight = FontWeight.Medium)
            }
            if (room.style.isNotEmpty()) {
                Text(room.style, style = MaterialTheme.typography.bodySmall, color = Olive, maxLines = 1)
            }
        }
    }
}
