package com.example.stopprogressif.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProjectedStatsChart(
    projectedCigarettes: List<Int>,
    projectedSavings: List<Float>,
    daysLabels: List<String>
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            daysLabels.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        Sparkline(
            values = projectedCigarettes.map { it.toFloat() },
            color = MaterialTheme.colorScheme.error,
            strokeWidth = 2.dp,
            label = "Cigarettes"
        )

        Sparkline(
            values = projectedSavings,
            color = MaterialTheme.colorScheme.tertiary,
            strokeWidth = 2.dp,
            label = "Économies (€)"
        )
    }
}

@Composable
fun Sparkline(
    values: List<Float>,
    color: Color,
    strokeWidth: Dp,
    label: String
) {
    if (values.isEmpty() || values.all { it == 0f }) {
        Text("Aucune donnée pour $label", color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }

    val maxValue = values.maxOrNull() ?: 1f
    val minValue = values.minOrNull() ?: 0f
    val valueRange = (maxValue - minValue).takeIf { it != 0f } ?: 1f

    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val spacing = canvasWidth / (values.size - 1).coerceAtLeast(1)
            val path = Path()

            values.forEachIndexed { index, value ->
                val x = index * spacing
                val y = canvasHeight - ((value - minValue) / valueRange) * canvasHeight
                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }

            drawPath(
                path = path,
                color = color,
                style = Stroke(width = strokeWidth.toPx())
            )
        }
    }
}
