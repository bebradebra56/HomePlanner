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
import com.homeplane.plmeas.data.entity.Project
import com.homeplane.plmeas.ui.components.*
import com.homeplane.plmeas.ui.theme.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.homeplane.plmeas.utils.CurrencyUtils
import com.homeplane.plmeas.viewmodel.AppViewModel

@Composable
fun ProjectsScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val projects by viewModel.allProjects.collectAsStateWithLifecycle()
    val activeProjectId by viewModel.activeProjectId.collectAsStateWithLifecycle()
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val currency = prefs.currency
    val units = prefs.units
    var showAddDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<Project?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenTopBar(title = "Projects", onBack = onBack, actions = {
            IconButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Project")
            }
        })

        if (projects.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyStateView(
                    icon = Icons.Filled.FolderOpen,
                    title = "No projects yet",
                    subtitle = "Create your first project to start planning"
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(projects) { project ->
                    ProjectCard(
                        project = project,
                        isActive = project.id == activeProjectId,
                        currency = currency,
                        units = units,
                        onSetActive = { viewModel.setActiveProject(project.id) },
                        onDelete = { deleteTarget = project }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddProjectDialog(
            currency = currency,
            units = units,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, type, area, startDate, budget, notes ->
                viewModel.addProject(name, type, area, startDate, budget, notes)
                showAddDialog = false
            }
        )
    }

    deleteTarget?.let { project ->
        ConfirmDeleteDialog(
            title = "Delete Project",
            message = "Delete \"${project.name}\"? All associated data will be removed.",
            onConfirm = {
                viewModel.deleteProject(project)
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null }
        )
    }
}

@Composable
private fun ProjectCard(
    project: Project,
    isActive: Boolean,
    currency: String,
    units: String,
    onSetActive: () -> Unit,
    onDelete: () -> Unit
) {
    val gradients = listOf(
        listOf(Terracotta, Color(0xFFE9A062)),
        listOf(DustyBlue, Color(0xFF4A8FDE)),
        listOf(Olive, Color(0xFF6B7A5D)),
        listOf(Sand, Color(0xFFD4A843))
    )
    val gradIndex = (project.name.hashCode() % gradients.size).let { if (it < 0) -it else it }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (!isActive) onSetActive() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(if (isActive) 4.dp else 1.dp),
        border = if (isActive) androidx.compose.foundation.BorderStroke(2.dp, Terracotta) else null
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(gradients[gradIndex])),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    project.name.take(2).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(project.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    if (isActive) {
                        Spacer(Modifier.width(8.dp))
                        Badge(containerColor = Terracotta) { Text("Active", style = MaterialTheme.typography.labelSmall, color = Color.White) }
                    }
                }
                if (project.apartmentType.isNotEmpty()) {
                    Text(project.apartmentType, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 4.dp)) {
                    if (project.area > 0) {
                        Text("${project.area.toInt()} ${units}²", style = MaterialTheme.typography.bodySmall, color = DustyBlue)
                    }
                    if (project.budget > 0) {
                        Text(CurrencyUtils.formatShort(project.budget, currency), style = MaterialTheme.typography.bodySmall, color = Olive)
                    }
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun AddProjectDialog(
    currency: String,
    units: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Float, String, Double, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    InputDialog(title = "New Project", onDismiss = onDismiss, onConfirm = {
        if (name.isNotBlank()) {
            onConfirm(name, type, area.toFloatOrNull() ?: 0f, startDate, budget.toDoubleOrNull() ?: 0.0, notes)
        }
    }) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Project Name *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = type,
            onValueChange = { type = it },
            label = { Text("Apartment Type") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("e.g. Studio, 2-Bedroom") }
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = area,
                onValueChange = { area = it },
                label = { Text("Area ($units²)") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            OutlinedTextField(
                value = budget,
                onValueChange = { budget = it },
                label = { Text("Budget (${CurrencyUtils.symbol(currency)})") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }
        Spacer(Modifier.height(8.dp))
        DatePickerField(
            label = "Start Date",
            value = startDate,
            onDateSelected = { startDate = it }
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 3
        )
    }
}
