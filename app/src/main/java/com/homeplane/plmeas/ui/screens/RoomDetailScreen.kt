package com.homeplane.plmeas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.homeplane.plmeas.data.entity.FurnitureItem
import com.homeplane.plmeas.data.entity.InteriorRoom
import com.homeplane.plmeas.navigation.Routes
import com.homeplane.plmeas.ui.components.*
import com.homeplane.plmeas.ui.theme.*
import com.homeplane.plmeas.viewmodel.AppViewModel

@Composable
fun RoomDetailScreen(roomId: Long, viewModel: AppViewModel, navController: NavController) {
    var room by remember { mutableStateOf<InteriorRoom?>(null) }
    val furnitureList by viewModel.getFurnitureByRoom(roomId).collectAsStateWithLifecycle(emptyList())
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(roomId) {
        room = viewModel.getRoomById(roomId)
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        room?.let { r ->
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(
                        Brush.linearGradient(listOf(DustyBlue, Color(0xFF4A8FDE)))
                    )
                    .padding(top = 8.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { navController.navigate(Routes.layoutPlanner(roomId)) }) {
                            Icon(Icons.Filled.Map, contentDescription = "Layout", tint = Color.White)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(r.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(r.type, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                    if (r.area > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                            Icon(Icons.Filled.Straighten, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("${r.width.toInt()} × ${r.length.toInt()} m  (${r.area.toInt()} m²)", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                }
            }

            // Tabs
            val tabs = listOf("Furniture", "Details")
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = Terracotta
            ) {
                tabs.forEachIndexed { i, title ->
                    Tab(selected = selectedTab == i, onClick = { selectedTab = i }, text = { Text(title) })
                }
            }

            when (selectedTab) {
                0 -> FurnitureTab(
                    furniture = furnitureList,
                    onDelete = { viewModel.deleteFurniture(it) },
                    onAddFurniture = { navController.navigate(Routes.ADD_FURNITURE) }
                )
                1 -> DetailsTab(room = r)
            }
        } ?: run {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Terracotta)
            }
        }
    }
}

@Composable
private fun FurnitureTab(
    furniture: List<FurnitureItem>,
    onDelete: (FurnitureItem) -> Unit,
    onAddFurniture: () -> Unit
) {
    if (furniture.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                EmptyStateView(
                    icon = Icons.Filled.Chair,
                    title = "No furniture",
                    subtitle = "Add furniture items to this room"
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = onAddFurniture, shape = RoundedCornerShape(12.dp)) { Text("Add Furniture") }
            }
        }
    } else {
        Box(Modifier.fillMaxSize()) {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(furniture) { item ->
                    FurnitureListCard(item = item, onDelete = { onDelete(item) })
                }
            }
            FloatingActionButton(
                onClick = onAddFurniture,
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                containerColor = Terracotta
            ) { Icon(Icons.Filled.Add, contentDescription = "Add", tint = Color.White) }
        }
    }
}

@Composable
private fun FurnitureListCard(item: FurnitureItem, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Terracotta.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Chair, contentDescription = null, tint = Terracotta, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(item.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (item.price > 0) Text("$${item.price.toLong()}", style = MaterialTheme.typography.bodySmall, color = Olive)
            }
            if (item.width > 0) {
                Text(
                    "${item.width.toInt()}×${item.depth.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(8.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun DetailsTab(room: InteriorRoom) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            DetailRow("Type", room.type)
            DetailRow("Style", room.style.ifEmpty { "—" })
            DetailRow("Width", if (room.width > 0) "${room.width} m" else "—")
            DetailRow("Length", if (room.length > 0) "${room.length} m" else "—")
            DetailRow("Area", if (room.area > 0) "${room.area} m²" else "—")
        }
        if (room.notes.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Notes", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Text(room.notes, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}
