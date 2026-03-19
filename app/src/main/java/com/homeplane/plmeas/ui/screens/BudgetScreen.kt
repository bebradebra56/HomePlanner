package com.homeplane.plmeas.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.homeplane.plmeas.data.entity.BudgetItem
import com.homeplane.plmeas.ui.components.*
import com.homeplane.plmeas.ui.theme.*
import com.homeplane.plmeas.utils.CurrencyUtils
import com.homeplane.plmeas.viewmodel.AppViewModel

private val budgetCategories = listOf("Furniture", "Decor", "Lighting", "Textiles", "Accessories", "Renovation", "Other")
private val categoryColors = listOf(Terracotta, DustyBlue, Sand, Olive, Color(0xFFB39DDB), Color(0xFF80DEEA), Color(0xFFBCAAA4))

@Composable
fun BudgetScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val budgetItems by viewModel.budgetItems.collectAsStateWithLifecycle()
    val totalSpent by viewModel.totalSpent.collectAsStateWithLifecycle()
    val activeProject by viewModel.activeProject.collectAsStateWithLifecycle()
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<BudgetItem?>(null) }
    val budget = activeProject?.budget ?: 0.0
    val currency = prefs.currency

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenTopBar(title = "Budget", onBack = onBack, actions = {
            IconButton(onClick = { showAddDialog = true }) { Icon(Icons.Filled.Add, contentDescription = "Add") }
        })

        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Budget overview card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Budget Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            BudgetStat(
                                "Total Budget",
                                if (budget > 0) CurrencyUtils.format(budget, currency) else "—",
                                DustyBlue, Modifier.weight(1f)
                            )
                            BudgetStat("Spent", CurrencyUtils.format(totalSpent, currency), Terracotta, Modifier.weight(1f))
                            BudgetStat(
                                "Remaining",
                                CurrencyUtils.format((budget - totalSpent).coerceAtLeast(0.0), currency),
                                Olive, Modifier.weight(1f)
                            )
                        }
                        if (budget > 0) {
                            Spacer(Modifier.height(16.dp))
                            val progress = (totalSpent / budget).coerceIn(0.0, 1.0).toFloat()
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
                                color = if (progress > 0.9f) MaterialTheme.colorScheme.error else Terracotta,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "${(progress * 100).toInt()}% used",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Pie chart
            if (budgetItems.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("By Category", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(16.dp))
                            val categoryTotals = budgetItems
                                .groupBy { it.category }
                                .mapValues { (_, v) -> v.sumOf { it.amount } }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BudgetPieChart(
                                    data = categoryTotals,
                                    modifier = Modifier.size(120.dp)
                                )
                                Spacer(Modifier.width(20.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    categoryTotals.entries.take(5).forEachIndexed { i, (cat, amt) ->
                                        val color = categoryColors[budgetCategories.indexOf(cat).takeIf { it >= 0 } ?: (i % categoryColors.size)]
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(color))
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                "$cat: ${CurrencyUtils.formatShort(amt, currency)}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Transactions
            if (budgetItems.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Filled.AttachMoney,
                        title = "No expenses yet",
                        subtitle = "Track your interior spending"
                    )
                }
            } else {
                item { SectionHeader("Expenses") }
                items(budgetItems) { item ->
                    BudgetItemCard(item = item, currency = currency, onDelete = { deleteTarget = item })
                }
            }
        }
    }

    if (showAddDialog) {
        AddBudgetItemDialog(
            currency = currency,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, category, amount, date, notes ->
                viewModel.addBudgetItem(name, category, amount, date, notes)
                showAddDialog = false
            }
        )
    }

    deleteTarget?.let { item ->
        ConfirmDeleteDialog(
            title = "Delete Expense",
            message = "Delete \"${item.name}\"?",
            onConfirm = { viewModel.deleteBudgetItem(item); deleteTarget = null },
            onDismiss = { deleteTarget = null }
        )
    }
}

@Composable
private fun BudgetStat(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color, maxLines = 1)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun BudgetPieChart(data: Map<String, Double>, modifier: Modifier = Modifier) {
    val total = data.values.sum().takeIf { it > 0 } ?: 1.0
    Canvas(modifier = modifier) {
        var startAngle = -90f
        data.entries.forEachIndexed { i, (cat, amt) ->
            val sweep = (amt / total * 360f).toFloat()
            val color = categoryColors[budgetCategories.indexOf(cat).takeIf { it >= 0 } ?: (i % categoryColors.size)]
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true,
                topLeft = Offset(4f, 4f),
                size = Size(size.width - 8f, size.height - 8f)
            )
            startAngle += sweep
        }
    }
}

@Composable
private fun BudgetItemCard(item: BudgetItem, currency: String, onDelete: () -> Unit) {
    val catIndex = budgetCategories.indexOf(item.category).takeIf { it >= 0 } ?: 0
    val color = categoryColors[catIndex % categoryColors.size]
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Box(Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(color))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(item.category, style = MaterialTheme.typography.bodySmall, color = color)
                    if (item.date.isNotEmpty()) Text(item.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(
                CurrencyUtils.format(item.amount, currency),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Terracotta
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun AddBudgetItemDialog(
    currency: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Furniture") }
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    InputDialog(title = "Add Expense", onDismiss = onDismiss, onConfirm = {
        if (name.isNotBlank() && amount.isNotBlank()) onConfirm(name, category, amount.toDoubleOrNull() ?: 0.0, date, notes)
    }) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount (${CurrencyUtils.symbol(currency)}) *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        Spacer(Modifier.height(8.dp))
        DatePickerField(
            label = "Date",
            value = date,
            onDateSelected = { date = it }
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
        Spacer(Modifier.height(8.dp))
        Text("Category", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        ChipRow(items = budgetCategories, selected = category, onSelect = { category = it })
    }
}
