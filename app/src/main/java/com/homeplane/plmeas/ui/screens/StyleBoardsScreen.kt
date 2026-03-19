package com.homeplane.plmeas.ui.screens

import androidx.compose.foundation.background
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
import com.homeplane.plmeas.ui.components.ScreenTopBar
import com.homeplane.plmeas.ui.theme.*

data class StyleBoard(
    val name: String,
    val description: String,
    val colors: List<Color>,
    val keywords: List<String>,
    val gradient: List<Color>
)

private val styleBoardData = listOf(
    StyleBoard(
        name = "Minimalist",
        description = "Clean lines, neutral tones, and functional elegance. Let every piece breathe.",
        colors = listOf(Color(0xFFF5F5F5), Color(0xFFE0E0E0), Color(0xFF212121), Color(0xFFBDBDBD)),
        keywords = listOf("Clean", "Simple", "Functional", "Neutral"),
        gradient = listOf(Color(0xFFECEFF1), Color(0xFFCFD8DC))
    ),
    StyleBoard(
        name = "Scandinavian",
        description = "Warm wood tones, cozy textiles, and hygge vibes. Nature meets modern comfort.",
        colors = listOf(Color(0xFFF5DEB3), Color(0xFF8FBC8F), Color(0xFF4682B4), Color(0xFFFFFAF0)),
        keywords = listOf("Cozy", "Natural", "Warm", "Hygge"),
        gradient = listOf(Color(0xFFFFF8E1), Color(0xFFFFECB3))
    ),
    StyleBoard(
        name = "Industrial Loft",
        description = "Raw materials, exposed brick, metal accents and open spaces.",
        colors = listOf(Color(0xFF607D8B), Color(0xFF795548), Color(0xFF424242), Color(0xFFBCAAA4)),
        keywords = listOf("Raw", "Urban", "Metal", "Concrete"),
        gradient = listOf(Color(0xFF546E7A), Color(0xFF37474F))
    ),
    StyleBoard(
        name = "Modern Luxury",
        description = "Bold statements, rich textures, gold accents and premium materials.",
        colors = listOf(Color(0xFFDAA520), Color(0xFF2F4F4F), Color(0xFFF5F5DC), Color(0xFF36454F)),
        keywords = listOf("Luxe", "Gold", "Bold", "Premium"),
        gradient = listOf(Color(0xFFDAA520), Color(0xFFB8860B))
    ),
    StyleBoard(
        name = "Boho Chic",
        description = "Earthy hues, rattan, macramé and an eclectic mix of global patterns.",
        colors = listOf(Terracotta, Sand, Olive, Color(0xFF6B4F3A)),
        keywords = listOf("Earthy", "Eclectic", "Textured", "Global"),
        gradient = listOf(Terracotta, Color(0xFFE9C46A))
    ),
    StyleBoard(
        name = "Coastal",
        description = "Blue-white palette, natural linen, driftwood and ocean-inspired elements.",
        colors = listOf(DustyBlue, Color(0xFFE0F7FA), Color(0xFFFFFFFF), Color(0xFFD2B48C)),
        keywords = listOf("Ocean", "Airy", "Fresh", "Natural"),
        gradient = listOf(DustyBlue, Color(0xFF4FC3F7))
    ),
    StyleBoard(
        name = "Art Deco",
        description = "Geometric patterns, jewel tones, brass and velvet textures.",
        colors = listOf(Color(0xFF1A237E), Color(0xFFFFD700), Color(0xFF4A148C), Color(0xFFE8EAF6)),
        keywords = listOf("Geometric", "Opulent", "Jewel", "Brass"),
        gradient = listOf(Color(0xFF1A237E), Color(0xFF4A148C))
    ),
    StyleBoard(
        name = "Japandi",
        description = "Japanese wabi-sabi meets Scandinavian minimalism. Quiet beauty in imperfection.",
        colors = listOf(Color(0xFFF5F0E8), Color(0xFF6D4C41), Color(0xFF78909C), Color(0xFFECEFE8)),
        keywords = listOf("Wabi-sabi", "Serene", "Natural", "Refined"),
        gradient = listOf(Color(0xFFF5F0E8), Color(0xFFD7CCC8))
    )
)

@Composable
fun StyleBoardsScreen(onBack: () -> Unit) {
    val favorites = remember { mutableStateSetOf<String>() }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenTopBar(title = "Style Boards", onBack = onBack)

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    "Find your interior style",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(styleBoardData) { board ->
                StyleBoardCard(
                    board = board,
                    isFavorite = favorites.contains(board.name),
                    onFavoriteToggle = {
                        if (favorites.contains(board.name)) favorites.remove(board.name)
                        else favorites.add(board.name)
                    }
                )
            }
        }
    }
}

@Composable
private fun StyleBoardCard(board: StyleBoard, isFavorite: Boolean, onFavoriteToggle: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            // Color palette strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(Brush.linearGradient(board.gradient))
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    board.colors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(color)
                                .then(
                                    Modifier.padding(1.dp)
                                )
                        )
                    }
                }
                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                ) {
                    Icon(
                        if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red else Color.White
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(board.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (isFavorite) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.Red.copy(alpha = 0.1f)
                        ) {
                            Text("Saved", style = MaterialTheme.typography.labelSmall, color = Color.Red, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(board.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    board.keywords.forEach { kw ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(kw, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

private fun <E> mutableStateSetOf(vararg elements: E): MutableSet<E> =
    mutableSetOf(*elements)
