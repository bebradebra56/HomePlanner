package com.homeplane.plmeas.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.homeplane.plmeas.data.AppPreferences
import com.homeplane.plmeas.navigation.Routes
import com.homeplane.plmeas.ui.theme.Terracotta
import com.homeplane.plmeas.ui.theme.WarmBackground
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigate: (String) -> Unit,
    prefs: AppPreferences
) {
    // rememberUpdatedState ensures the coroutine always reads the latest prefs value,
    // even after DataStore loads asynchronously during the 2-second splash delay.
    val latestPrefs by rememberUpdatedState(prefs)
    var visible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.6f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "logo_scale"
    )

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
        delay(2200)
        if (latestPrefs.onboardingCompleted) {
            onNavigate(Routes.DASHBOARD)
        } else {
            onNavigate(Routes.ONBOARDING)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(WarmBackground, Color(0xFFFFEDD5))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(600)) + scaleIn(initialScale = 0.7f)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(scale)
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Terracotta, Color(0xFFE9C46A))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Home,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    "HomePlanner",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2B2D42)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Plan your perfect interior",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF5A5C6E)
                )
                Spacer(Modifier.height(48.dp))
                CircularProgressIndicator(
                    color = Terracotta,
                    modifier = Modifier.size(32.dp),
                    strokeWidth = 2.5.dp
                )
            }
        }
    }
}
