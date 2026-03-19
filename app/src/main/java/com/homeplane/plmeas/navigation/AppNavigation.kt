package com.homeplane.plmeas.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.homeplane.plmeas.ui.screens.*
import com.homeplane.plmeas.viewmodel.AppViewModel

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val DASHBOARD = "dashboard"
    const val PROJECTS = "projects"
    const val ROOMS = "rooms"
    const val ROOM_DETAIL = "room_detail/{roomId}"
    const val ADD_ROOM = "add_room"
    const val LAYOUT_PLANNER = "layout_planner/{roomId}"
    const val FURNITURE = "furniture"
    const val ADD_FURNITURE = "add_furniture"
    const val STYLE_BOARDS = "style_boards"
    const val SHOPPING = "shopping"
    const val BUDGET = "budget"
    const val MEASUREMENTS = "measurements"
    const val PHOTOS = "photos"
    const val IDEAS = "ideas"
    const val NOTES = "notes"
    const val REPORTS = "reports"
    const val SETTINGS = "settings"
    const val MORE = "more"

    fun roomDetail(roomId: Long) = "room_detail/$roomId"
    fun layoutPlanner(roomId: Long) = "layout_planner/$roomId"
}

data class BottomNavItem(val label: String, val icon: ImageVector, val route: String)

val bottomNavItems = listOf(
    BottomNavItem("Home", Icons.Filled.Home, Routes.DASHBOARD),
    BottomNavItem("Rooms", Icons.Filled.GridView, Routes.ROOMS),
    BottomNavItem("Furniture", Icons.Filled.Chair, Routes.FURNITURE),
    BottomNavItem("Shopping", Icons.Filled.ShoppingCart, Routes.SHOPPING),
    BottomNavItem("More", Icons.Filled.Apps, Routes.MORE)
)

val bottomNavRoutes = bottomNavItems.map { it.route }.toSet()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(viewModel: AppViewModel) {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val showBottomNav = currentRoute in bottomNavRoutes

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomNav) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentBackStack?.destination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(item.icon, contentDescription = item.label)
                            },
                            label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            enterTransition = { fadeIn() + slideInHorizontally { it / 4 } },
            exitTransition = { fadeOut() + slideOutHorizontally { -it / 4 } },
            popEnterTransition = { fadeIn() + slideInHorizontally { -it / 4 } },
            popExitTransition = { fadeOut() + slideOutHorizontally { it / 4 } }
        ) {
            composable(Routes.SPLASH) {
                SplashScreen(
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    },
                    prefs = prefs
                )
            }
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onFinish = {
                        viewModel.completeOnboarding()
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.DASHBOARD) {
                DashboardScreen(viewModel = viewModel, navController = navController)
            }
            composable(Routes.ROOMS) {
                RoomsScreen(viewModel = viewModel, navController = navController)
            }
            composable(Routes.ROOM_DETAIL) { backStack ->
                val roomId = backStack.arguments?.getString("roomId")?.toLongOrNull() ?: return@composable
                RoomDetailScreen(roomId = roomId, viewModel = viewModel, navController = navController)
            }
            composable(Routes.ADD_ROOM) {
                AddRoomScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Routes.LAYOUT_PLANNER) { backStack ->
                val roomId = backStack.arguments?.getString("roomId")?.toLongOrNull() ?: return@composable
                LayoutPlannerScreen(roomId = roomId, viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Routes.FURNITURE) {
                FurnitureScreen(viewModel = viewModel, navController = navController)
            }
            composable(Routes.ADD_FURNITURE) {
                AddFurnitureScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Routes.STYLE_BOARDS) {
                StyleBoardsScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.SHOPPING) {
                ShoppingScreen(viewModel = viewModel, navController = navController)
            }
            composable(Routes.BUDGET) {
                BudgetScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Routes.MEASUREMENTS) {
                MeasurementsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Routes.PHOTOS) {
                PhotosScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Routes.IDEAS) {
                IdeasScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Routes.NOTES) {
                NotesScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Routes.REPORTS) {
                ReportsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Routes.PROJECTS) {
                ProjectsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Routes.MORE) {
                MoreScreen(navController = navController)
            }
        }
    }
}
