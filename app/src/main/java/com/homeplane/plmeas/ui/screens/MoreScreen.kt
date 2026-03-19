package com.homeplane.plmeas.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.homeplane.plmeas.navigation.Routes
import com.homeplane.plmeas.ui.theme.*

data class MoreMenuItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val gradient: List<Color>
)

private val moreMenuItems = listOf(
    MoreMenuItem(
        "Projects",
        Icons.Filled.FolderOpen,
        Routes.PROJECTS,
        listOf(Terracotta, Color(0xFFE9A062))
    ),
    MoreMenuItem(
        "Layout Planner",
        Icons.Filled.Map,
        Routes.ROOMS,
        listOf(DustyBlue, Color(0xFF4A8FDE))
    ),
    MoreMenuItem(
        "Style Boards",
        Icons.Filled.Palette,
        Routes.STYLE_BOARDS,
        listOf(Color(0xFFB39DDB), Color(0xFF7E57C2))
    ),
    MoreMenuItem(
        "Budget",
        Icons.Filled.AttachMoney,
        Routes.BUDGET,
        listOf(Olive, Color(0xFF6B7A5D))
    ),
    MoreMenuItem(
        "Measurements",
        Icons.Filled.Straighten,
        Routes.MEASUREMENTS,
        listOf(Sand, Color(0xFFD4A843))
    ),
    MoreMenuItem(
        "Photos",
        Icons.Filled.PhotoLibrary,
        Routes.PHOTOS,
        listOf(Color(0xFF80DEEA), Color(0xFF00ACC1))
    ),
    MoreMenuItem(
        "Ideas",
        Icons.Filled.Lightbulb,
        Routes.IDEAS,
        listOf(Color(0xFFFFAB40), Color(0xFFFF6D00))
    ),
    MoreMenuItem(
        "Notes",
        Icons.Filled.Notes,
        Routes.NOTES,
        listOf(Color(0xFFAED581), Color(0xFF558B2F))
    ),
    MoreMenuItem(
        "Reports",
        Icons.Filled.Assessment,
        Routes.REPORTS,
        listOf(Color(0xFFEF9A9A), Color(0xFFE53935))
    ),
    MoreMenuItem(
        "Settings",
        Icons.Filled.Settings,
        Routes.SETTINGS,
        listOf(Color(0xFF90A4AE), Color(0xFF546E7A))
    )
)


@Composable
fun MoreScreen(navController: NavController) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column {
                Text(
                    "More",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "All features in one place",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(moreMenuItems) { item ->
                MoreMenuCard(item = item, onClick = { navController.navigate(item.route) })
            }
            item {
                MoreMenuCard(
                    item = MoreMenuItem(
                        "Privacy Policy",
                        Icons.Filled.Policy,
                        Routes.SETTINGS,
                        listOf(Sand, Color(0xFFD4A843))
                    ), onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://homepllanner.com/privacy-policy.html"))
                        context.startActivity(intent)
                    })
            }
        }
    }
}

@Composable
private fun MoreMenuCard(item: MoreMenuItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(item.gradient)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    item.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                item.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
