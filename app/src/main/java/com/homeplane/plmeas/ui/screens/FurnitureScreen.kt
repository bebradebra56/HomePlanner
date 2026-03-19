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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.homeplane.plmeas.data.entity.FurnitureItem
import com.homeplane.plmeas.navigation.Routes
import com.homeplane.plmeas.ui.components.*
import com.homeplane.plmeas.ui.theme.*
import com.homeplane.plmeas.utils.CurrencyUtils
import com.homeplane.plmeas.viewmodel.AppViewModel

val furnitureCategories = listOf("All", "Sofa", "Bed", "Table", "Chair", "Wardrobe", "Lighting", "Decor", "Storage", "Other")

@Composable
fun FurnitureScreen(viewModel: AppViewModel, navController: NavController) {
    val furniture by viewModel.furniture.collectAsStateWithLifecycle()
    val rooms by viewModel.rooms.collectAsStateWithLifecycle()
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    var selectedCategory by remember { mutableStateOf("All") }
    var deleteTarget by remember { mutableStateOf<FurnitureItem?>(null) }
    val currency = prefs.currency

    val filtered = if (selectedCategory == "All") furniture else furniture.filter { it.category == selectedCategory }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenTopBar(
            title = "Furniture",
            actions = {
                IconButton(onClick = { navController.navigate(Routes.ADD_FURNITURE) }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Furniture")
                }
            }
        )

        ChipRow(
            items = furnitureCategories,
            selected = selectedCategory,
            onSelect = { selectedCategory = it },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        if (furniture.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyStateView(
                    icon = Icons.Filled.Chair,
                    title = "No furniture yet",
                    subtitle = "Add furniture items to your project"
                )
            }
        } else if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("No items in \"$selectedCategory\"", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered) { item ->
                    FurnitureCard(
                        item = item,
                        roomName = rooms.find { it.id == item.roomId }?.name,
                        currency = currency,
                        onDelete = { deleteTarget = item }
                    )
                }
            }
        }
    }

    deleteTarget?.let { item ->
        ConfirmDeleteDialog(
            title = "Delete Furniture",
            message = "Delete \"${item.name}\"?",
            onConfirm = { viewModel.deleteFurniture(item); deleteTarget = null },
            onDismiss = { deleteTarget = null }
        )
    }
}

@Composable
fun AddFurnitureScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val rooms by viewModel.rooms.collectAsStateWithLifecycle()
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val currency = prefs.currency
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Other") }
    var selectedRoomId by remember { mutableStateOf(-1L) }
    var width by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var depth by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var store by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val cats = listOf("Sofa", "Bed", "Table", "Chair", "Wardrobe", "Lighting", "Decor", "Storage", "Other")

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenTopBar(title = "Add Furniture", onBack = onBack)
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
            item {
                Text("Category", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                ChipRow(items = cats, selected = category, onSelect = { category = it })
            }
            if (rooms.isNotEmpty()) {
                item {
                    Text("Room", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    ChipRow(
                        items = listOf("None") + rooms.map { it.name },
                        selected = if (selectedRoomId == -1L) "None" else rooms.find { it.id == selectedRoomId }?.name ?: "None",
                        onSelect = { sel ->
                            selectedRoomId = if (sel == "None") -1L else rooms.find { it.name == sel }?.id ?: -1L
                        }
                    )
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = width, onValueChange = { width = it }, label = { Text("W (cm)") }, modifier = Modifier.weight(1f), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                    OutlinedTextField(value = height, onValueChange = { height = it }, label = { Text("H (cm)") }, modifier = Modifier.weight(1f), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                    OutlinedTextField(value = depth, onValueChange = { depth = it }, label = { Text("D (cm)") }, modifier = Modifier.weight(1f), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price (${CurrencyUtils.symbol(currency)})") }, modifier = Modifier.weight(1f), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                    OutlinedTextField(value = store, onValueChange = { store = it }, label = { Text("Store") }, modifier = Modifier.weight(1f), singleLine = true)
                }
            }
            item {
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 3)
            }
            item {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.addFurniture(
                                name, category, selectedRoomId,
                                width.toFloatOrNull() ?: 0f,
                                height.toFloatOrNull() ?: 0f,
                                depth.toFloatOrNull() ?: 0f,
                                price.toDoubleOrNull() ?: 0.0,
                                store, notes
                            )
                            onBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) { Text("Save Furniture") }
            }
        }
    }
}

@Composable
private fun FurnitureCard(item: FurnitureItem, roomName: String?, currency: String, onDelete: () -> Unit) {
    val color = categoryColor(item.category)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Chair, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = {},
                        label = { Text(item.category, style = MaterialTheme.typography.labelSmall) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = color.copy(alpha = 0.1f))
                    )
                    if (roomName != null) {
                        AssistChip(
                            onClick = {},
                            label = { Text(roomName, style = MaterialTheme.typography.labelSmall) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = DustyBlue.copy(alpha = 0.1f))
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (item.price > 0) Text(CurrencyUtils.format(item.price, currency), style = MaterialTheme.typography.bodySmall, color = Olive, fontWeight = FontWeight.SemiBold)
                    if (item.width > 0) Text("${item.width.toInt()}×${item.height.toInt()}×${item.depth.toInt()} cm", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
        }
    }
}
