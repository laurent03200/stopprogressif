package com.example.stopprogressif.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.stopprogressif.ui.components.TopBarAvecMenu
import com.example.stopprogressif.data.HistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel = viewModel()
) {
    Scaffold(
        topBar = {
            TopBarAvecMenu(
                title = "Historique",
                navController = navController,
                showBack = true
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text("Affichage de l’historique ici...")
            // Tu pourras compléter cette zone avec tes données plus tard
        }
    }
}
