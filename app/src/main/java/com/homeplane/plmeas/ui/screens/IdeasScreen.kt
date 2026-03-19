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
import com.homeplane.plmeas.data.entity.Idea
import com.homeplane.plmeas.ui.components.*
import com.homeplane.plmeas.ui.theme.*
import com.homeplane.plmeas.viewmodel.AppViewModel

private val ideaColors = listOf(
    "#E76F51", "#6FA3EF", "#8F9779", "#E9C46A",
    "#B39DDB", "#80DEEA", "#FFAB40", "#AED581"
)

@Composable
fun IdeasScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val ideas by viewModel.ideas.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<Idea?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenTopBar(title = "Ideas", onBack = onBack, actions = {
            IconButton(onClick = { showAddDialog = true }) { Icon(Icons.Filled.Add, contentDescription = "Add") }
        })

        if (ideas.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyStateView(
                    icon = Icons.Filled.Lightbulb,
                    title = "No ideas yet",
                    subtitle = "Save interior design ideas and inspiration"
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(ideas) { idea ->
                    IdeaCard(idea = idea, onDelete = { deleteTarget = idea })
                }
            }
        }
    }

    if (showAddDialog) {
        AddIdeaDialog(
            colors = ideaColors,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, desc, tags, color ->
                viewModel.addIdea(title, desc, tags, color)
                showAddDialog = false
            }
        )
    }

    deleteTarget?.let { idea ->
        ConfirmDeleteDialog(
            title = "Delete Idea",
            message = "Delete \"${idea.title}\"?",
            onConfirm = { viewModel.deleteIdea(idea); deleteTarget = null },
            onDismiss = { deleteTarget = null }
        )
    }
}

@Composable
private fun IdeaCard(idea: Idea, onDelete: () -> Unit) {
    val color = try { Color(android.graphics.Color.parseColor(idea.colorHex)) } catch (e: Exception) { Terracotta }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(0.dp)) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(color)
            )
            Column(modifier = Modifier.weight(1f).padding(16.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(idea.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                }
                if (idea.description.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(idea.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3)
                }
                if (idea.tags.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        idea.tags.split(",").take(3).forEach { tag ->
                            val trimmed = tag.trim()
                            if (trimmed.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = color.copy(alpha = 0.12f)
                                ) {
                                    Text(trimmed, style = MaterialTheme.typography.labelSmall, color = color, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddIdeaDialog(colors: List<String>, onDismiss: () -> Unit, onConfirm: (String, String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(colors.first()) }

    InputDialog(title = "New Idea", onDismiss = onDismiss, onConfirm = {
        if (title.isNotBlank()) onConfirm(title, description, tags, selectedColor)
    }) {
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth().height(100.dp), maxLines = 5)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = tags, onValueChange = { tags = it }, label = { Text("Tags") }, modifier = Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("e.g. minimal, wood, cozy") })
        Spacer(Modifier.height(12.dp))
        Text("Color", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            colors.forEach { hex ->
                val c = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Terracotta }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(c)
                        .let { mod -> if (selectedColor == hex) mod.then(Modifier.padding(2.dp)) else mod },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (selectedColor == hex) 28.dp else 32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(c)
                            .clickable { selectedColor = hex }
                    )
                    if (selectedColor == hex) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

