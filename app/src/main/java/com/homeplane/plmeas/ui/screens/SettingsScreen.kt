package com.homeplane.plmeas.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.homeplane.plmeas.ui.components.ScreenTopBar
import com.homeplane.plmeas.ui.theme.*
import com.homeplane.plmeas.viewmodel.AppViewModel

@Composable
fun SettingsScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // CSV export launcher
    val exportCsvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            viewModel.buildExportCsv(prefs.currency, prefs.units) { csv ->
                try {
                    context.contentResolver.openOutputStream(uri)?.use { it.write(csv.toByteArray()) }
                    Toast.makeText(context, "Export saved successfully", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Full backup launcher
    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri != null) {
            viewModel.buildFullBackup { backup ->
                try {
                    context.contentResolver.openOutputStream(uri)?.use { it.write(backup.toByteArray()) }
                    Toast.makeText(context, "Backup saved successfully", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Backup failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenTopBar(title = "Settings", onBack = onBack)

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { SettingsSectionHeader("Appearance") }

            item {
                SettingsToggleItem(
                    icon = if (prefs.isDarkTheme) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                    iconTint = if (prefs.isDarkTheme) DustyBlue else Sand,
                    title = "Dark Theme",
                    subtitle = if (prefs.isDarkTheme) "Currently dark" else "Currently light",
                    checked = prefs.isDarkTheme,
                    onToggle = { viewModel.setDarkTheme(it) }
                )
            }

            item { Spacer(Modifier.height(8.dp)); SettingsSectionHeader("Units & Currency") }

            item {
                SettingsDropdownItem(
                    icon = Icons.Filled.AttachMoney,
                    iconTint = Olive,
                    title = "Currency",
                    subtitle = "Selected: ${prefs.currency} — affects all price displays",
                    value = prefs.currency,
                    options = listOf("USD", "EUR", "GBP", "RUB", "JPY", "CNY"),
                    onSelect = { viewModel.setCurrency(it) }
                )
            }

            item {
                SettingsDropdownItem(
                    icon = Icons.Filled.Straighten,
                    iconTint = DustyBlue,
                    title = "Measurement Units",
                    subtitle = "Selected: ${prefs.units} — affects all area & size displays",
                    value = prefs.units,
                    options = listOf("m", "cm", "ft", "in"),
                    onSelect = { viewModel.setUnits(it) }
                )
            }

            item { Spacer(Modifier.height(8.dp)); SettingsSectionHeader("Notifications") }

            item {
                SettingsToggleItem(
                    icon = Icons.Filled.Notifications,
                    iconTint = Terracotta,
                    title = "Notifications",
                    subtitle = if (prefs.notificationsEnabled) "Reminders enabled" else "Reminders disabled",
                    checked = prefs.notificationsEnabled,
                    onToggle = { viewModel.setNotifications(it) }
                )
            }

            item { Spacer(Modifier.height(8.dp)); SettingsSectionHeader("Data") }

            item {
                SettingsActionItem(
                    icon = Icons.Filled.Backup,
                    iconTint = Olive,
                    title = "Backup All Data",
                    subtitle = "Save all projects to a .txt file on your device"
                ) {
                    backupLauncher.launch("HomePlanner_Backup.txt")
                }
            }

            item {
                SettingsActionItem(
                    icon = Icons.Filled.FileDownload,
                    iconTint = DustyBlue,
                    title = "Export as CSV",
                    subtitle = "Export current project data as a spreadsheet-compatible CSV"
                ) {
                    exportCsvLauncher.launch("HomePlanner_Export.csv")
                }
            }

            item { Spacer(Modifier.height(8.dp)); SettingsSectionHeader("About") }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(Terracotta),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Home, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                            }
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text("HomePlanner", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Version 1.0.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Plan your perfect interior. Design rooms, manage furniture, track budgets and bring your dream home to life.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge,
        color = Terracotta,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
    )
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = checked,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Terracotta)
            )
        }
    }
}

@Composable
private fun SettingsDropdownItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(value, style = MaterialTheme.typography.labelMedium)
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = { onSelect(option); expanded = false },
                            trailingIcon = if (option == value) ({
                                Icon(Icons.Filled.Check, contentDescription = null, tint = Terracotta, modifier = Modifier.size(16.dp))
                            }) else null
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsActionItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        onClick = onClick
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
