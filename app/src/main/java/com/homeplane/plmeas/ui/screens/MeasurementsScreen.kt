package com.homeplane.plmeas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.homeplane.plmeas.data.entity.Measurement
import com.homeplane.plmeas.ui.components.*
import com.homeplane.plmeas.ui.theme.*
import com.homeplane.plmeas.viewmodel.AppViewModel

@Composable
fun MeasurementsScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val measurements by viewModel.measurements.collectAsStateWithLifecycle()
    val rooms by viewModel.rooms.collectAsStateWithLifecycle()
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<Measurement?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenTopBar(title = "Measurements", onBack = onBack, actions = {
            IconButton(onClick = { showAddDialog = true }) { Icon(Icons.Filled.Add, contentDescription = "Add") }
        })

        if (measurements.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyStateView(
                    icon = Icons.Filled.Straighten,
                    title = "No measurements",
                    subtitle = "Record room and furniture dimensions"
                )
            }
        } else {
            // Group by room
            val grouped = measurements.groupBy { it.roomId }
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                grouped.forEach { (roomId, items) ->
                    item {
                        val roomName = if (roomId == -1L) "General" else rooms.find { it.id == roomId }?.name ?: "Room"
                        SectionHeader(roomName)
                    }
                    items(items) { m ->
                        MeasurementCard(
                            measurement = m,
                            onDelete = { deleteTarget = m }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddMeasurementDialog(
            rooms = rooms.map { it.name },
            roomIds = listOf(-1L) + rooms.map { it.id },
            defaultUnit = prefs.units,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, value, unit, roomId, notes ->
                viewModel.addMeasurement(name, value, unit, roomId, notes)
                showAddDialog = false
            }
        )
    }

    deleteTarget?.let { m ->
        ConfirmDeleteDialog(
            title = "Delete Measurement",
            message = "Delete \"${m.name}\"?",
            onConfirm = { viewModel.deleteMeasurement(m); deleteTarget = null },
            onDismiss = { deleteTarget = null }
        )
    }
}

@Composable
private fun MeasurementCard(measurement: Measurement, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(DustyBlue, DustyBlue.copy(alpha = 0.6f)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Straighten, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(measurement.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                if (measurement.notes.isNotEmpty()) Text(measurement.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            Text(
                "${measurement.value} ${measurement.unit}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DustyBlue
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun AddMeasurementDialog(
    rooms: List<String>,
    roomIds: List<Long>,
    defaultUnit: String,
    onDismiss: () -> Unit,
    onConfirm: (String, Float, String, Long, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf(defaultUnit) }
    var selectedRoomIndex by remember { mutableIntStateOf(0) }
    var notes by remember { mutableStateOf("") }

    val allRooms = listOf("General") + rooms
    val units = listOf("m", "cm", "mm", "ft", "in")

    InputDialog(title = "Add Measurement", onDismiss = onDismiss, onConfirm = {
        if (name.isNotBlank() && value.isNotBlank()) {
            onConfirm(name, value.toFloatOrNull() ?: 0f, unit, roomIds.getOrElse(selectedRoomIndex) { -1L }, notes)
        }
    }) {
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name *") }, modifier = Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("e.g. Living room wall") })
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = value, onValueChange = { value = it }, label = { Text("Value *") }, modifier = Modifier.weight(1.5f), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            Column(modifier = Modifier.weight(1f)) {
                Text("Unit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                ChipRow(items = units, selected = unit, onSelect = { unit = it })
            }
        }
        Spacer(Modifier.height(8.dp))
        if (allRooms.size > 1) {
            Text("Room", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            ChipRow(
                items = allRooms,
                selected = allRooms.getOrElse(selectedRoomIndex) { "General" },
                onSelect = { sel -> selectedRoomIndex = allRooms.indexOf(sel) }
            )
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
    }
}
