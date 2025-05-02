package com.stopprogressif

import android.app.Application
import androidx.compose.foundation.Canvas          // ✅
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path           // ✅
import androidx.compose.ui.graphics.drawscope.Stroke // ✅
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

/* ------------------------------------------------------------------ */
/*  Données & modèle                                                  */
/* ------------------------------------------------------------------ */

data class DailyStats(
    val date: LocalDate,
    val cigarettes: Int,
    val moneySaved: Float,
    val healthScore: Int
)

/* ------------------------------------------------------------------ */
/*  Écran Progression (vide au départ)                                */
/* ------------------------------------------------------------------ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressionScreen(
    navController: NavController,
    progressVM: ProgressifViewModel = viewModel(
        factory = ProgressifViewModelFactory(
            LocalContext.current.applicationContext as Application
        )
    )
) {
    /* Remplace “emptyList()” par ton flux réel de stats quand disponible */
    val stats by remember { mutableStateOf(emptyList<DailyStats>()) }

    val grouped = stats.groupBy {
        it.date.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear())
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Progression", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { pad ->
        if (stats.isEmpty()) {
            /* ---------------- Aucune donnée ---------------- */
            Box(
                Modifier
                    .padding(pad)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Aucune donnée pour l’instant",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        } else {
            /* ---------------- Affichage complet ------------- */
            LazyColumn(
                Modifier
                    .padding(pad)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { ChartsRow(stats) }
                grouped.entries.sortedByDescending { it.key }.forEach { (week, list) ->
                    item { WeekHeader(week, list.last().date, list.first().date) }
                    items(list) { DayCard(it) }
                }
                item { Spacer(Modifier.height(88.dp)) }
            }
        }
    }
}

/* ------------------------------------------------------------------ */
/*  Graphes sparkline                                                 */
/* ------------------------------------------------------------------ */

@Composable
private fun ChartsRow(stats: List<DailyStats>) {
    Surface(
        Modifier
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp)),
        tonalElevation = 2.dp
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Sparkline(
                label = "🚬  Cigarettes",
                values = stats.reversed().map { it.cigarettes.toFloat() },
                lineColor = MaterialTheme.colorScheme.error
            )
            Sparkline(
                label = "💰  Économies (€)",
                values = stats.reversed().map { it.moneySaved },
                lineColor = MaterialTheme.colorScheme.tertiary
            )
            Sparkline(
                label = "❤️  Santé (%)",
                values = stats.reversed().map { it.healthScore.toFloat() },
                lineColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun Sparkline(label: String, values: List<Float>, lineColor: Color) {
    val maxVal = (values.maxOrNull() ?: 1f).coerceAtLeast(1f)

    Column {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            if (values.size < 2) return@Canvas
            val stepX = size.width / (values.size - 1)   // largeur entre deux points
            val path = Path()
            values.forEachIndexed { i, v ->
                val x = i * stepX
                val y = size.height - (v / maxVal) * size.height
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, lineColor, style = Stroke(width = 4.dp.toPx()))
        }
    }
}

/* ------------------------------------------------------------------ */
/*  UI hebdo + journalière                                            */
/* ------------------------------------------------------------------ */

@Composable
private fun WeekHeader(week: Int, start: LocalDate, end: LocalDate) {
    Surface(
        tonalElevation = 4.dp,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
    ) {
        Row(
            Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(vertical = 10.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Semaine $week",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "${start.dayOfMonth} ${start.month.getDisplayName(TextStyle.SHORT, Locale.FRENCH)} – " +
                        "${end.dayOfMonth} ${end.month.getDisplayName(TextStyle.SHORT, Locale.FRENCH)}",
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun DayCard(day: DailyStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    day.date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.FRENCH)
                        .replaceFirstChar { it.uppercase() },
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    "${day.date.dayOfMonth} " +
                            day.date.month.getDisplayName(TextStyle.SHORT, Locale.FRENCH),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            MetricsRow(day)
        }
    }
}

@Composable
private fun MetricsRow(day: DailyStats) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Pill("🚬", "${day.cigarettes}", MaterialTheme.colorScheme.errorContainer)
        Pill("💰", "%.2f€".format(day.moneySaved), MaterialTheme.colorScheme.tertiaryContainer)
        Pill("❤️", "${day.healthScore}%", MaterialTheme.colorScheme.primaryContainer)
    }
}

@Composable
private fun Pill(icon: String, value: String, bg: Color) {
    Surface(
        shape = RoundedCornerShape(30),
        color = bg,
        tonalElevation = 1.dp
    ) {
        Column(
            Modifier
                .widthIn(min = 68.dp)
                .padding(vertical = 6.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 16.sp)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}
