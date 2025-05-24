package com.example.stopprogressif.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stopprogressif.model.DailyReport
import com.example.stopprogressif.ui.components.InfoCard
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.shape.RoundedCornerShape
import java.time.LocalDate // Import LocalDate

@Composable
fun ProgressionScreen(
    dailyReports: List<DailyReport>,
    semaineMoyenne: Long,
    moisMoyenne: Long
) {
    val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Ta progression 💪", style = MaterialTheme.typography.headlineMedium)
        Text("Continue comme ça, tu tiens bon !", fontSize = 16.sp, color = Color.Gray)

        val today = LocalDate.now().toString()
        val todayReport = dailyReports.find { it.date == today && it.type == "daily" }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E88E5)),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Aujourd’hui", fontSize = 20.sp, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    InfoCard("🚬", todayReport?.cigarettesSmoked.toString(), Color.White) // Use todayReport
                    InfoCard("💰", "%.2f €".format(todayReport?.moneySavedCents?.div(100.0) ?: 0.0), Color.White) // Use todayReport
                    InfoCard("⏱", formatTime(todayReport?.avgTimeExceededMs ?: 0L), Color.White) // Use todayReport
                }

                // Display "Temps dépassé aujourd'hui" if applicable
                todayReport?.avgTimeExceededMs?.let { depassement ->
                    if (depassement > 0) {
                        Text(
                            text = "⏱️ Temps dépassé aujourd'hui : ${formatTime(depassement)}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Green // This is the green color from previous request
                        )
                    }
                }
            }
        }

        val depassements = dailyReports.mapNotNull { it.avgTimeExceededMs.takeIf { it > 0 } }
        val moyenneDepassement = if (depassements.isNotEmpty()) depassements.sum() / depassements.size else 0L

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            MoyenneCard("Cette semaine", "⏱ " + formatTime(semaineMoyenne))
            MoyenneCard("Ce mois-ci", "⏱ " + formatTime(moisMoyenne))
        }

        // Display "Moyenne de dépassement" if applicable
        if (moyenneDepassement > 0) {
            Text(
                text = "📊 Moyenne de dépassement : ${formatTime(moyenneDepassement)}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black // Changed to Black for better contrast on light background
            )
        }


        Text("Historique", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(dailyReports.reversed()) { report ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val date = parser.parse(report.date)
                        Text("📅 ${dateFormat.format(date)}", fontWeight = FontWeight.Bold)
                        Text("🚬 Cigarettes fumées : ${report.cigarettesSmoked}")
                        Text("💰 Économie : %.2f €".format(report.moneySavedCents / 100.0))
                        Text("⏱ Moyenne dépassement : ${formatTime(report.avgTimeExceededMs)}")
                    }
                }
            }
        }
    }
}

@Composable
fun MoyenneCard(title: String, value: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7FA)),
        modifier = Modifier
            .width(160.dp)
            .padding(4.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// Keeping the more detailed formatTime function
fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return buildString {
        if (hours > 0) append("${hours}h ")
        if (minutes > 0 || hours > 0) append("${minutes}m ")
        append("${seconds}s")
    }.trim()
}