package com.example.stopprogressif.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.material3.Text
import kotlin.math.abs
import java.util.Locale

@Composable
fun CircularTimerModern(
    timeMillis: Long,
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 12.dp,
    circleColor: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val size = size.minDimension
            val sweepAngle = 360 * progress
            val startAngle = -90f

            drawArc(
                color = Color.LightGray,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset.Zero,
                size = Size(size, size),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )

            drawArc(
                color = circleColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset.Zero,
                size = Size(size, size),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }

        val isOver = timeMillis < 0
        val prefix = if (isOver) "+" else ""
        val textColor = if (isOver) Color(0xFF00FF00) else MaterialTheme.colorScheme.onSurface

        val totalSeconds = abs(timeMillis) / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        val formattedTime = String.format(Locale.getDefault(), "%s%02d:%02d:%02d", prefix, hours, minutes, seconds)

        Text(
            text = formattedTime,
            fontSize = 20.sp,
            color = textColor
        )
    }
}
