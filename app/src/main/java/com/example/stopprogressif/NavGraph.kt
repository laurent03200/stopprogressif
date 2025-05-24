package com.example.stopprogressif

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.stopprogressif.ui.screens.HomeScreen
import com.example.stopprogressif.ui.screens.SettingsScreen
import com.example.stopprogressif.ui.screens.CigaretteTrackerScreen
import com.example.stopprogressif.ui.screens.ProgressionScreen
import com.example.stopprogressif.viewmodel.ProgressifViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    progressifViewModel: ProgressifViewModel
) {
    // Collect states as they are flows
    val historique by progressifViewModel.historique.collectAsState()
    val semaineMoyenne by progressifViewModel.semaineMoyenne.collectAsState()
    val moisMoyenne by progressifViewModel.moisMoyenne.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                navController = navController,
                progressifViewModel = progressifViewModel
            )
        }
        composable("settings") {
            SettingsScreen(
                navController = navController,
                progressifViewModel = progressifViewModel
            )
        }
        composable("tracker") {
            CigaretteTrackerScreen(
                navController = navController,
                progressifViewModel = progressifViewModel
            )
        }
        composable("progression") {
            ProgressionScreen(
                dailyReports = historique, // Use the collected state
                semaineMoyenne = semaineMoyenne, // Use the collected state
                moisMoyenne = moisMoyenne // Use the collected state
            )
        }
    }
}
