@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.stopprogressif.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.stopprogressif.viewmodel.ProgressifViewModel
import com.example.stopprogressif.ui.components.CircularTimerModern
import com.example.stopprogressif.ui.components.InfoCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Composable
fun HomeScreen(
    navController: NavController,
    progressifViewModel: ProgressifViewModel,
    modifier: Modifier = Modifier
) {
    // Correction des noms des StateFlows
    val timeLeftFormatted by progressifViewModel.timeLeftFormatted.collectAsState()
    val currentCigarettesCount by progressifViewModel.currentCigarettesCount.collectAsState()
    val nextCigaretteTime by progressifViewModel.nextCigaretteTime.collectAsState()
    val lastCigaretteTime by progressifViewModel.lastCigaretteTime.collectAsState()
    val tempsDepasse by progressifViewModel.tempsDepasse.collectAsState()
    val settingsData by progressifViewModel.settingsData.collectAsState()
    val isRefreshing by progressifViewModel.isRefreshing.collectAsState()
    val isOver = timeLeftFormatted == "00:00"

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        progressifViewModel.refresh()
    }

    // Calcul de l'économie actuelle
    val economieActuelle = remember(currentCigarettesCount, settingsData) {
        val coutParCigarette = settingsData.prixPaquet / settingsData.cigarettesParPaquet
        val cigarettesNonFumees = max(0, settingsData.cigarettesHabituelles - currentCigarettesCount)
        cigarettesNonFumees * coutParCigarette
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stop Progressif") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1B1B1B), // Couleur sombre pour la barre
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Filled.MoreVert, "Settings", tint = Color.White)
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color(0xFF1B1B1B),
                contentColor = Color.White
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bouton Accueil
                    Button(
                        onClick = { navController.navigate("home") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Home, contentDescription = "Accueil")
                            Text("Accueil", fontSize = 10.sp)
                        }
                    }

                    // Bouton Progression
                    Button(
                        onClick = { navController.navigate("progression") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.BarChart, contentDescription = "Progression")
                            Text("Progression", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF2C2C2C), Color(0xFF121212))
                    )
                )
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                CircularTimerModern(
                    timeLeft = if (isOver) tempsDepasse else progressifViewModel.tempsRestant.collectAsState().value,
                    isOver = isOver
                )

                if (nextCigaretteTime != null) {
                    val heure = DateTimeFormatter.ofPattern("HH:mm")
                        .format(Instant.ofEpochMilli(nextCigaretteTime!!).atZone(ZoneId.systemDefault()))
                    Text("Prochaine cigarette : $heure", color = Color.White, fontSize = 14.sp)
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    InfoCard("🚬", currentCigarettesCount.toString(), Color(0xFFFFA726)) // Utiliser currentCigarettesCount
                    InfoCard("💰", "%.2f €".format(economieActuelle), Color(0xFF4CAF50))
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(
                        onClick = {
                            progressifViewModel.fumerUneCigarette()
                            scope.launch {
                                progressifViewModel.refresh()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7043))
                    ) {
                        Text("🚬 J’ai fumé")
                    }
                    Button(
                        onClick = { progressifViewModel.annulerDerniereCigarette() }, // Correction ici
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBDBDBD))
                    ) {
                        Text("❌ Annuler")
                    }
                }
            }
        }
    }
}