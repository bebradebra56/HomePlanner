package com.homeplane.plmeas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.homeplane.plmeas.navigation.Routes
import com.homeplane.plmeas.ui.components.EmptyStateView
import com.homeplane.plmeas.ui.components.NoProjectBanner
import com.homeplane.plmeas.ui.components.StatCard
import com.homeplane.plmeas.ui.theme.*
import com.homeplane.plmeas.utils.CurrencyUtils
import com.homeplane.plmeas.viewmodel.AppViewModel

@Composable
fun DashboardScreen(viewModel: AppViewModel, navController: NavController) {
    val activeProject by viewModel.activeProject.collectAsStateWithLifecycle()
    val rooms by viewModel.rooms.collectAsStateWithLifecycle()
    val recentFurniture by viewModel.recentFurniture.collectAsStateWithLifecycle()
    val totalSpent by viewModel.totalSpent.collectAsStateWithLifecycle()
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Show banner when no project is selected
        if (activeProject == null) {
            item {
                Spacer(Modifier.height(12.dp))
                NoProjectBanner(onCreateProject = { navController.navigate(Routes.PROJECTS) })
            }
        }

        item {
            // Hero card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(listOf(Terracotta, Color(0xFFE9A062)))
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Home, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Active Project", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.8f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        activeProject?.name ?: "No project selected",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (activeProject != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            buildString {
                                if (activeProject!!.apartmentType.isNotEmpty()) append(activeProject!!.apartmentType)
                                if (activeProject!!.area > 0) append(" · ${activeProject!!.area.toInt()} ${prefs.units}²")
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { navController.navigate(Routes.PROJECTS) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (activeProject != null) "Switch Project" else "Create Project")
                    }
                }
            }
        }

        item {
            // Stats row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = "Rooms",
                    value = rooms.size.toString(),
                    icon = Icons.Filled.GridView,
                    color = DustyBlue,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Furniture",
                    value = recentFurniture.size.toString(),
                    icon = Icons.Filled.Chair,
                    color = Olive,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Spent",
                    value = CurrencyUtils.formatShort(totalSpent, prefs.currency),
                    icon = Icons.Filled.AttachMoney,
                    color = Sand,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Actions
        item {
            Text(
                "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionCard(
                    "Add Room", Icons.Filled.AddHome, DustyBlue,
                    modifier = Modifier.weight(1f)
                ) { navController.navigate(Routes.ADD_ROOM) }
                QuickActionCard(
                    "Furniture", Icons.Filled.Chair, Olive,
                    modifier = Modifier.weight(1f)
                ) { navController.navigate(Routes.ADD_FURNITURE) }
                QuickActionCard(
                    "Idea", Icons.Filled.Lightbulb, Sand,
                    modifier = Modifier.weight(1f)
                ) { navController.navigate(Routes.IDEAS) }
                QuickActionCard(
                    "Measure", Icons.Filled.Straighten, Terracotta,
                    modifier = Modifier.weight(1f)
                ) { navController.navigate(Routes.MEASUREMENTS) }
            }
        }

        // Rooms overview
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 20.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Rooms Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                TextButton(onClick = { navController.navigate(Routes.ROOMS) }) { Text("See all") }
            }
        }

        if (rooms.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Filled.GridView,
                    title = "No rooms yet",
                    subtitle = "Add rooms to your project to get started"
                )
            }
        } else {
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(rooms.take(6)) { room ->
                        RoomMiniCard(
                            name = room.name,
                            style = room.style,
                            area = room.area,
                            units = prefs.units
                        ) {
                            navController.navigate(Routes.roomDetail(room.id))
                        }
                    }
                }
            }
        }

        // Budget snapshot
        if (activeProject != null && activeProject!!.budget > 0) {
            item {
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Budget Snapshot", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            TextButton(onClick = { navController.navigate(Routes.BUDGET) }) { Text("Details") }
                        }
                        Spacer(Modifier.height(12.dp))
                        val budget = activeProject!!.budget
                        val progress = (totalSpent / budget).coerceIn(0.0, 1.0).toFloat()
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                            color = if (progress > 0.9f) MaterialTheme.colorScheme.error else Terracotta,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Spent", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(CurrencyUtils.format(totalSpent, prefs.currency), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Terracotta)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Budget", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(CurrencyUtils.format(budget, prefs.currency), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun QuickActionCard(
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(6.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun RoomMiniCard(name: String, style: String, area: Float, units: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DustyBlue.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.GridView, contentDescription = null, tint = DustyBlue, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
            if (style.isNotEmpty()) Text(style, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            if (area > 0) Text("${area.toInt()} $units²", style = MaterialTheme.typography.bodySmall, color = Terracotta)
        }
    }
}
