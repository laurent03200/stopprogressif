package com.example.stopprogressif.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.stopprogressif.viewmodel.CigaretteTrackerViewModel
import com.example.stopprogressif.factory.GenericViewModelFactory
import java.time.format.DateTimeFormatter
import java.time.Instant
import java.time.ZoneId
import com.example.stopprogressif.viewmodel.ProgressifViewModel
import com.example.stopprogressif.ui.components.TopBarAvecMenu // ✅ import menu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CigaretteTrackerScreen(
    navController: NavController,
    progressifViewModel: ProgressifViewModel
) {
    val context = LocalContext.current

    val viewModel: CigaretteTrackerViewModel = viewModel(
        factory = GenericViewModelFactory { CigaretteTrackerViewModel(context.applicationContext as android.app.Application) }
    )

    val lastCigTime by viewModel.lastCigaretteTime.collectAsState()
    val nextCigTime by viewModel.nextCigaretteTime.collectAsState()

    Scaffold(
        topBar = {
            TopBarAvecMenu(
                title = "Suivi des cigarettes",
                navController = navController,
                showBack = true
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            lastCigTime?.let { time ->
                val formatter = DateTimeFormatter.ofPattern("HH:mm")
                val displayTime = formatter.format(Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()))
                Text("Dernière cigarette : $displayTime", color = Color.Black, style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(16.dp))

            nextCigTime?.let { time ->
                val formatter = DateTimeFormatter.ofPattern("HH:mm")
                val displayTime = formatter.format(Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()))
                Text("Prochaine cigarette : $displayTime", color = Color.DarkGray, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
