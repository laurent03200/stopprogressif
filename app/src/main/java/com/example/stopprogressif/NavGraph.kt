package com.example.stopprogressif

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.stopprogressif.ui.screens.HomeScreen
import com.example.stopprogressif.ui.screens.SettingsScreen
import com.example.stopprogressif.ui.screens.ProgressionScreen
import com.example.stopprogressif.viewmodel.ProgressifViewModel
// import com.example.stopprogressif.ui.screens.formatMillisToHoursMinutes // Cette importation n'est plus nécessaire ici si elle n'est pas utilisée directement dans NavGraph

@Composable
fun NavGraph(
    navController: NavHostController,
    progressifViewModel: ProgressifViewModel
) {
    // Collect states as they are flows
    val historique by progressifViewModel.historique.collectAsState()
    // Les lignes suivantes ne sont plus nécessaires car ProgressionScreen n'attend plus ces paramètres directement
    // val semaineMoyenne by progressifViewModel.semaineMoyenne.collectAsState()
    // val moisMoyenne by progressifViewModel.moisMoyenne.collectAsState()

    // Les lignes suivantes ne sont plus nécessaires car ProgressionScreen n'attend plus ces paramètres directement
    // val semaineMoyenneText = formatMillisToHoursMinutes(semaineMoyenne)
    // val moisMoyenneText = formatMillisToHoursMinutes(moisMoyenne)

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

        composable("progression") {
            ProgressionScreen(
                // dailyReports = historique, // Pas nécessaire car ProgressionScreen utilise hiltViewModel() pour obtenir l'historique
                progressifViewModel = progressifViewModel // Passé car votre `ProgressionScreen` l'attend
            )
        }
    }
}