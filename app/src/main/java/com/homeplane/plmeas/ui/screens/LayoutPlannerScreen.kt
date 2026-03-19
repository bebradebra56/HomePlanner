package com.homeplane.plmeas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.homeplane.plmeas.data.entity.FurnitureItem
import com.homeplane.plmeas.data.entity.InteriorRoom
import com.homeplane.plmeas.ui.components.EmptyStateView
import com.homeplane.plmeas.ui.components.ScreenTopBar
import com.homeplane.plmeas.ui.theme.*
import com.homeplane.plmeas.viewmodel.AppViewModel
import kotlin.math.roundToInt

@Composable
fun LayoutPlannerScreen(roomId: Long, viewModel: AppViewModel, onBack: () -> Unit) {
    var room by remember { mutableStateOf<InteriorRoom?>(null) }
    val furniture by viewModel.getFurnitureByRoom(roomId).collectAsStateWithLifecycle(emptyList())

    val positions = remember { mutableStateMapOf<Long, Offset>() }

    LaunchedEffect(roomId) { room = viewModel.getRoomById(roomId) }

    LaunchedEffect(furniture) {
        furniture.forEach { item ->
            if (!positions.containsKey(item.id)) {
                positions[item.id] = Offset(item.posX, item.posY)
            }
        }
    }

    var canvasWidth by remember { mutableFloatStateOf(0f) }
    var canvasHeight by remember { mutableFloatStateOf(0f) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenTopBar(
            title = room?.name ?: "Layout Planner",
            onBack = onBack,
            actions = {
                IconButton(onClick = {
                    positions.clear()
                    furniture.forEach { item ->
                        positions[item.id] = Offset(item.posX, item.posY)
                    }
                }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Reset")
                }
            }
        )

        if (furniture.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyStateView(
                    icon = Icons.Filled.Chair,
                    title = "No furniture in this room",
                    subtitle = "Add furniture to the room first, then plan the layout"
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Info bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Info, contentDescription = null, tint = DustyBlue, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Drag furniture to arrange layout", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Room canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                            .border(2.dp, Terracotta.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .background(Color(0xFFFAF3EC))
                            .onGloballyPositioned { coords ->
                                canvasWidth = coords.size.width.toFloat()
                                canvasHeight = coords.size.height.toFloat()
                            }
                    ) {
                        furniture.forEach { item ->
                            val pos = positions[item.id] ?: Offset(item.posX, item.posY)
                            DraggableFurnitureItem(
                                item = item,
                                position = pos,
                                canvasWidth = canvasWidth,
                                canvasHeight = canvasHeight,
                                onPositionChange = { newPos ->
                                    positions[item.id] = newPos
                                },
                                onDragEnd = {
                                    val finalPos = positions[item.id] ?: pos
                                    viewModel.updateFurniturePosition(item.id, finalPos.x, finalPos.y)
                                }
                            )
                        }
                    }
                }

                // Furniture legend
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(furniture) { item ->
                        LegendChip(item = item)
                    }
                }
            }
        }
    }
}

@Composable
private fun DraggableFurnitureItem(
    item: FurnitureItem,
    position: Offset,
    canvasWidth: Float,
    canvasHeight: Float,
    onPositionChange: (Offset) -> Unit,
    onDragEnd: () -> Unit
) {
    val itemWidthPx = 80.dp
    val itemHeightPx = 60.dp
    val density = LocalDensity.current
    val itemWidthF = with(density) { itemWidthPx.toPx() }
    val itemHeightF = with(density) { itemHeightPx.toPx() }

    val color = categoryColor(item.category)

    Box(
        modifier = Modifier
            .offset { IntOffset(position.x.roundToInt(), position.y.roundToInt()) }
            .size(itemWidthPx, itemHeightPx)
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.85f))
            .border(1.dp, color, RoundedCornerShape(8.dp))
            .pointerInput(item.id) {
                detectDragGestures(
                    onDragEnd = onDragEnd
                ) { change, dragAmount ->
                    change.consume()
                    val newX = (position.x + dragAmount.x).coerceIn(0f, canvasWidth - itemWidthF)
                    val newY = (position.y + dragAmount.y).coerceIn(0f, canvasHeight - itemHeightF)
                    onPositionChange(Offset(newX, newY))
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                item.name.take(8),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 9.sp,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun LegendChip(item: FurnitureItem) {
    val color = categoryColor(item.category)
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(color))
            Spacer(Modifier.width(6.dp))
            Text(item.name, style = MaterialTheme.typography.labelSmall, color = color)
        }
    }
}

fun categoryColor(category: String): Color = when (category) {
    "Sofa" -> DustyBlue
    "Bed" -> Color(0xFFB39DDB)
    "Table" -> Sand
    "Chair" -> Olive
    "Wardrobe" -> Color(0xFF80DEEA)
    "Lighting" -> Color(0xFFFFCC02)
    "Decor" -> Terracotta
    "Storage" -> Color(0xFFBCAAA4)
    else -> Color(0xFF9E9E9E)
}
