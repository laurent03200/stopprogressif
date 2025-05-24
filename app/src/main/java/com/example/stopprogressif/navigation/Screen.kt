
package com.example.stopprogressif.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")
    object Tracker : Screen("tracker")
    object Progression : Screen("progression")
}
