package com.stopprogressif

import android.app.Application
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlin.math.abs
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    progressViewModel: ProgressifViewModel = viewModel(
        factory = ProgressifViewModelFactory(
            LocalContext.current.applicationContext as Application
        )
    )
) {
    val cigarettesFumees by progressViewModel.cigarettesFumees.collectAsState()
    val tempsRestant by progressViewModel.tempsRestant.collectAsState()
    val settings by progressViewModel.settingsData.collectAsState()

    val prixCigarette = settings.prixPaquet / max(settings.cigarettesParPaquet, 1)
    val economiesRealisees =
        max(settings.cigarettesHabituelles - cigarettesFumees, 0) * prixCigarette

    val totalTime = progressViewModel.getInitialIntervalle()
    val progress = (1f - tempsRestant / totalTime.toFloat()).coerceIn(0f, 1f)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Stop Progressif",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2196F3)
                )
                /* plus de section actions : le menu 3 points disparaît */
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF2196F3),
                            Color(0xFF673AB7),
                            Color(0xFFE91E63)
                        )
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
                CircularTimer(tempsRestant = tempsRestant, progress = progress)

                if (tempsRestant < 0L) {
                    Text(
                        text = "👏 Bien joué ! Vous dépassez votre intervalle prévu !",
                        color = Color.Yellow,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    InfoCard("🚬", cigarettesFumees.toString(), Color(0xFFFFA726))
                    InfoCard(
                        "💰",
                        "%.2f €".format(economiesRealisees),
                        Color(0xFF4CAF50)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { progressViewModel.fumerUneCigarette() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF7043)
                        )
                    ) {
                        Text("🚬 J'ai fumé")
                    }
                    Button(
                        onClick = { progressViewModel.annulerDerniereCigarette() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFBDBDBD)
                        )
                    ) {
                        Text("❌ Annuler")
                    }
                }
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/* Composables auxiliaires                                                    */
/* -------------------------------------------------------------------------- */

@Composable
fun CircularTimer(tempsRestant: Long, progress: Float) {
    val isDepassement = tempsRestant < 0
    val absTime = abs(tempsRestant)
    val hours = (absTime / 3_600_000) % 24
    val minutes = (absTime / 60_000) % 60
    val seconds = (absTime / 1_000) % 60
    val formattedTime =
        (if (isDepassement) "+" else "") + String.format("%02d:%02d:%02d", hours, minutes, seconds)

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(220.dp)) {
        Canvas(Modifier.size(220.dp)) {
            drawArc(
                color = Color.LightGray,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = if (isDepassement) Color(0xFF00E676) else Color.Cyan,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formattedTime,
                fontSize = 28.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            if (isDepassement) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "🎉 Bien joué !",
                    color = Color.Yellow,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun InfoCard(icon: String, value: String, color: Color) {
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 26.sp)
            Text(
                text = value,
                fontSize = 20.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
