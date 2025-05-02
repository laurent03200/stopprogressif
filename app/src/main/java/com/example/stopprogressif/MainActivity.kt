package com.stopprogressif

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.stopprogressif.ui.theme.StopProgressifTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { StopProgressifApp() }
    }
}

/* ------------------------------------------------------------------ */
@Composable
fun StopProgressifApp() {
    StopProgressifTheme {
        val navController = rememberNavController()
        Scaffold(bottomBar = { BottomNavBar(navController) }) { inner ->
            NavGraph(navController, Modifier.padding(inner))
        }
    }
}

/* ------------------------------------------------------------------ */
@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        composable("home")     { HomeScreen(navController) }
        composable("progress") { ProgressionScreen(navController) }   // ✅ nom mis à jour
        composable("settings") { SettingsScreen(navController) }
    }
}

/* ------------------------------------------------------------------ */
private data class NavItem(
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
)

@Composable
private fun BottomNavBar(navController: NavHostController) {
    val items = listOf(
        NavItem("progress", Icons.Default.BarChart, "Progression"),
        NavItem("settings", Icons.Default.Settings, "Paramètres")
    )
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDest = currentBackStackEntry?.destination

    NavigationBar {
        items.forEach { item ->
            val selected = currentDest?.hierarchy?.any { it.route == item.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}
