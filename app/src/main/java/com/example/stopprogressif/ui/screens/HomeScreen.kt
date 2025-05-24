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
    val timeLeft by progressifViewModel.tempsRestant.collectAsState()
    val isOver = timeLeft <= 0
    val tempsDepasse by progressifViewModel.tempsDepasse.collectAsState()
    val displayTime = if (timeLeft < 0 && tempsDepasse > 0) tempsDepasse else abs(timeLeft)
    val cercleColor by progressifViewModel.cercleColor.collectAsState()
    val lastCigTime by progressifViewModel.lastCigaretteTime.collectAsState()
    val nextCigTime by progressifViewModel.nextCigaretteTime.collectAsState()
    val cigarettesFumees by progressifViewModel.cigarettesFumees.collectAsState()
    val settings by progressifViewModel.settingsData.collectAsState()

    val prixUnitaire = settings.prixPaquet / max(settings.cigarettesParPaquet, 1)
    val economieActuelle = max(settings.cigarettesHabituelles - cigarettesFumees, 0) * prixUnitaire

    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }

    val interval = (settings.espacementHeures * 60 + settings.espacementMinutes) * 60_000f
    val progress = if (!isOver) {
        if (interval > 0f) {
            1f - (abs(timeLeft).toFloat() / interval)
        } else {
            0f
        }
    } else {
        (tempsDepasse % 1_800_000f) / 1_800_000f
    }

    LaunchedEffect(Unit) {
        while (true) {
            progressifViewModel.refresh()
            delay(1000)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Stop Progressif", color = Color.White) },
                actions = {
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.White)
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Accueil") },
                                onClick = {
                                    expanded = false
                                    navController.navigate("home")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Suivi") },
                                onClick = {
                                    expanded = false
                                    navController.navigate("tracker")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Progression") },
                                onClick = {
                                    expanded = false
                                    navController.navigate("progression")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Réglages") },
                                onClick = {
                                    expanded = false
                                    navController.navigate("settings")
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF2196F3))
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF2196F3), Color(0xFF673AB7), Color(0xFFE91E63))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                CircularTimerModern(
        timeMillis = displayTime,
        progress = progress,
        modifier = Modifier.size(200.dp),
        circleColor = cercleColor
    )

                Spacer(modifier = Modifier.height(8.dp))

                val messageText = when {
                    !isOver -> when {
                        abs(timeLeft) < 5 * 60 * 1000 -> "🕐 Presque... reste fort !"
                        abs(timeLeft) < 10 * 60 * 1000 -> "💪 Tu tiens bon !"
                        else -> "⏳ Patience, ça avance."
                    }
                    tempsDepasse > 30 * 60 * 1000 -> "🧠 Héroïque ! Tu continues à résister."
                    tempsDepasse > 5 * 60 * 1000 -> "🔥 Tu bats ton record !"
                    tempsDepasse > 60 * 1000 -> "💪 Tu résistes, bravo !"
                    else -> "🔓 C’est bon, tu peux fumer maintenant."
                }

                Text(
                    text = messageText,
                    color = if (isOver) Color(0xFF4CAF50) else Color(0xFFFF5252),
                    style = MaterialTheme.typography.bodyLarge
                )

                lastCigTime?.let {
                    val heure = DateTimeFormatter.ofPattern("HH:mm")
                        .format(Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()))
                    Text("Dernière cigarette : $heure", color = Color.White, fontSize = 14.sp)
                }
                nextCigTime?.let {
                    val heure = DateTimeFormatter.ofPattern("HH:mm")
                        .format(Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()))
                    Text("Prochaine cigarette : $heure", color = Color.White, fontSize = 14.sp)
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    InfoCard("🚬", cigarettesFumees.toString(), Color(0xFFFFA726))
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
                        onClick = { progressifViewModel.annulerDerniereCigarette() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBDBDBD))
                    ) {
                        Text("❌ Annuler")
                    }
                }
            }
        }
    }
}
