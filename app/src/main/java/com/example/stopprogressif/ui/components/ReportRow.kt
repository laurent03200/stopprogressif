package com.example.stopprogressif.ui.components // <-- CORRIGE ICI

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.stopprogressif.model.DailyReport // Assurez-vous que cette importation est correcte


@Composable
fun ReportRow(report: DailyReport) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(report.date)
        Text(report.cigarettesSmoked.toString()) // <-- CORRIGE ICI
        Text("${report.avgTimeExceededMs / 1000}s")
        Text("€%.2f".format(report.moneySavedCents / 100.0)) // Meilleur formatage pour l'argent
    }
}