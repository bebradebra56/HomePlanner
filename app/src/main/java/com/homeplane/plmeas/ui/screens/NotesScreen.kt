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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.homeplane.plmeas.data.entity.Note
import com.homeplane.plmeas.ui.components.*
import com.homeplane.plmeas.ui.theme.*
import com.homeplane.plmeas.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotesScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editNote by remember { mutableStateOf<Note?>(null) }
    var deleteTarget by remember { mutableStateOf<Note?>(null) }
    var viewNote by remember { mutableStateOf<Note?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenTopBar(title = "Notes", onBack = onBack, actions = {
            IconButton(onClick = { showAddDialog = true }) { Icon(Icons.Filled.Add, contentDescription = "Add") }
        })

        if (notes.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyStateView(
                    icon = Icons.Filled.Notes,
                    title = "No notes yet",
                    subtitle = "Capture design ideas and reminders"
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notes) { note ->
                    NoteCard(
                        note = note,
                        onClick = { viewNote = note },
                        onEdit = { editNote = note },
                        onDelete = { deleteTarget = note }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        NoteEditorDialog(
            title = "New Note",
            initialTitle = "",
            initialContent = "",
            onDismiss = { showAddDialog = false },
            onSave = { t, c ->
                viewModel.addNote(t, c)
                showAddDialog = false
            }
        )
    }

    editNote?.let { note ->
        NoteEditorDialog(
            title = "Edit Note",
            initialTitle = note.title,
            initialContent = note.content,
            onDismiss = { editNote = null },
            onSave = { t, c ->
                viewModel.updateNote(note.copy(title = t, content = c))
                editNote = null
            }
        )
    }

    viewNote?.let { note ->
        NoteViewDialog(note = note, onDismiss = { viewNote = null }, onEdit = { viewNote = null; editNote = note })
    }

    deleteTarget?.let { note ->
        ConfirmDeleteDialog(
            title = "Delete Note",
            message = "Delete \"${note.title}\"?",
            onConfirm = { viewModel.deleteNote(note); deleteTarget = null },
            onDismiss = { deleteTarget = null }
        )
    }
}

@Composable
private fun NoteCard(note: Note, onClick: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val noteColors = listOf(Color(0xFFFFF3E0), Color(0xFFE8F5E9), Color(0xFFE3F2FD), Color(0xFFFCE4EC), Color(0xFFF3E5F5))
    val colorIndex = (note.title.hashCode() % noteColors.size).let { if (it < 0) -it else it }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = noteColors[colorIndex]),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(note.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                }
            }
            if (note.content.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(note.content, style = MaterialTheme.typography.bodySmall, maxLines = 3, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(note.createdAt)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NoteEditorDialog(title: String, initialTitle: String, initialContent: String, onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var noteTitle by remember { mutableStateOf(initialTitle) }
    var noteContent by remember { mutableStateOf(initialContent) }

    InputDialog(title = title, onDismiss = onDismiss, onConfirm = {
        if (noteTitle.isNotBlank()) onSave(noteTitle, noteContent)
    }) {
        OutlinedTextField(value = noteTitle, onValueChange = { noteTitle = it }, label = { Text("Title *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = noteContent, onValueChange = { noteContent = it }, label = { Text("Content") }, modifier = Modifier.fillMaxWidth().height(160.dp), maxLines = 8)
    }
}

@Composable
private fun NoteViewDialog(note: Note, onDismiss: () -> Unit, onEdit: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(note.title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                if (note.content.isNotEmpty()) Text(note.content, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))
                Text(
                    SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(note.createdAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onEdit) { Text("Edit") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
