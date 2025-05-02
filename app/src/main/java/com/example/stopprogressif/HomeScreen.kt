package com.stopprogressif

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    modifier: Modifier = Modifier
) {
    // ⚡️ on récupère l’activité pour scoper la ViewModel dessus
    val activity = LocalContext.current as ComponentActivity
    val progressViewModel: ProgressifViewModel = viewModel(
        viewModelStoreOwner = activity,
        factory = ProgressifViewModelFactory(activity.application)
    )

    val cigarettesFumees by progressViewModel.cigarettesFumees.collectAsState()
    val tempsRestant by progressViewModel.tempsRestant.collectAsState()
    val settings by progressViewModel.settingsData.collectAsState()

    val prixCigarette = settings.prixPaquet / max(settings.cigarettesParPaquet, 1)
    val économies = max(settings.cigarettesHabituelles - cigarettesFumees, 0) * prixCigarette

    val totalTime = progressViewModel.getInitialIntervalle()
    val progress = (1f - tempsRestant / totalTime.toFloat()).coerceIn(0f, 1f)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text("Stop Progressif", color = Color.White, fontWeight = FontWeight.Bold)
                },
                actions = {
                    IconButton(
                        onClick = { navController.navigate("settings") { launchSingleTop = true } }
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Paramètres",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF2196F3))
            )
        }
    ) { inner ->
        Box(
            modifier = Modifier
                .padding(inner)
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
                CircularTimer(tempsRestant = tempsRestant, progress = progress)

                if (tempsRestant < 0L) {
                    Text(
                        "👏 Bien joué ! Vous dépassez votre intervalle prévu !",
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
                    InfoCard("💰", "%.2f €".format(économies), Color(0xFF4CAF50))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { progressViewModel.fumerUneCigarette() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7043))
                    ) {
                        Text("🚬 J'ai fumé")
                    }
                    Button(
                        onClick = { progressViewModel.annulerDerniereCigarette() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBDBDBD))
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
    val isOver = tempsRestant < 0
    val absTime = abs(tempsRestant)
    val h = (absTime / 3_600_000) % 24
    val m = (absTime / 60_000) % 60
    val s = (absTime / 1_000) % 60
    val formatted = (if (isOver) "+" else "") + String.format("%02d:%02d:%02d", h, m, s)

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
                color = if (isOver) Color(0xFF00E676) else Color.Cyan,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(formatted, fontSize = 28.sp, color = Color.White, fontWeight = FontWeight.Bold)
            if (isOver) {
                Spacer(Modifier.height(8.dp))
                Text("🎉 Bien joué !", color = Color.Yellow, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
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
            Text(value, fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
