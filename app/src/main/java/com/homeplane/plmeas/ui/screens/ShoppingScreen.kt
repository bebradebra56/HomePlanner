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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.homeplane.plmeas.data.entity.ShoppingItem
import com.homeplane.plmeas.ui.components.*
import com.homeplane.plmeas.ui.theme.*
import com.homeplane.plmeas.utils.CurrencyUtils
import com.homeplane.plmeas.viewmodel.AppViewModel

private val shoppingCategories = listOf("Furniture", "Decor", "Lighting", "Textiles", "Accessories", "Other")

@Composable
fun ShoppingScreen(viewModel: AppViewModel, navController: NavController) {
    val items by viewModel.shoppingItems.collectAsStateWithLifecycle()
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<ShoppingItem?>(null) }
    val currency = prefs.currency

    val pending = items.filter { !it.purchased }
    val purchased = items.filter { it.purchased }
    val totalValue = items.sumOf { it.price }
    val purchasedValue = purchased.sumOf { it.price }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenTopBar(
            title = "Shopping List",
            actions = {
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Item")
                }
            }
        )

        if (items.isNotEmpty()) {
            // Summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryChip("${pending.size} to buy", Terracotta, Modifier.weight(1f))
                SummaryChip("${purchased.size} bought", Olive, Modifier.weight(1f))
                SummaryChip("${CurrencyUtils.formatShort(purchasedValue, currency)}/${CurrencyUtils.formatShort(totalValue, currency)}", DustyBlue, Modifier.weight(1.5f))
            }
        }

        if (items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyStateView(
                    icon = Icons.Filled.ShoppingCart,
                    title = "Shopping list is empty",
                    subtitle = "Add items you want to buy for your interior"
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (pending.isNotEmpty()) {
                    item { SectionHeader("To Buy (${pending.size})") }
                    items(pending) { item ->
                        ShoppingItemCard(item = item, currency = currency, onToggle = { viewModel.toggleShoppingItem(item.id, item.purchased) }, onDelete = { deleteTarget = item })
                    }
                }
                if (purchased.isNotEmpty()) {
                    item { SectionHeader("Purchased (${purchased.size})") }
                    items(purchased) { item ->
                        ShoppingItemCard(item = item, currency = currency, onToggle = { viewModel.toggleShoppingItem(item.id, item.purchased) }, onDelete = { deleteTarget = item })
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddShoppingItemDialog(
            currency = currency,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, room, cat, price, store ->
                viewModel.addShoppingItem(name, room, cat, price, store)
                showAddDialog = false
            }
        )
    }

    deleteTarget?.let { item ->
        ConfirmDeleteDialog(
            title = "Remove Item",
            message = "Remove \"${item.name}\" from shopping list?",
            onConfirm = { viewModel.deleteShoppingItem(item); deleteTarget = null },
            onDismiss = { deleteTarget = null }
        )
    }
}

@Composable
private fun SummaryChip(text: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ShoppingItemCard(item: ShoppingItem, currency: String, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().alpha(if (item.purchased) 0.7f else 1f),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.purchased) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(if (item.purchased) 0.dp else 1.dp)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = item.purchased,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = Olive)
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (item.purchased) TextDecoration.LineThrough else TextDecoration.None
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (item.room.isNotEmpty()) Text(item.room, style = MaterialTheme.typography.bodySmall, color = DustyBlue)
                    if (item.category.isNotEmpty()) Text(item.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (item.store.isNotEmpty()) Text(item.store, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (item.price > 0) {
                Text(CurrencyUtils.format(item.price, currency), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = if (item.purchased) Olive else Terracotta)
                Spacer(Modifier.width(8.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Filled.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun AddShoppingItemDialog(
    currency: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Double, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Other") }
    var price by remember { mutableStateOf("") }
    var store by remember { mutableStateOf("") }

    InputDialog(title = "Add Item", onDismiss = onDismiss, onConfirm = {
        if (name.isNotBlank()) onConfirm(name, room, category, price.toDoubleOrNull() ?: 0.0, store)
    }) {
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item Name *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = room, onValueChange = { room = it }, label = { Text("Room") }, modifier = Modifier.weight(1f), singleLine = true)
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price (${CurrencyUtils.symbol(currency)})") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = store, onValueChange = { store = it }, label = { Text("Store") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(8.dp))
        Text("Category", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        ChipRow(items = shoppingCategories, selected = category, onSelect = { category = it })
    }
}
