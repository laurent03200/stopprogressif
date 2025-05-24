package com.example.stopprogressif.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

object WidgetGraphics {

    fun generateProgressBitmap(context: Context, ratio: Float, size: Int = 256): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val strokeWidth = 20f
        val radius = size / 2f - strokeWidth

        val centerX = size / 2f
        val centerY = size / 2f

        val backgroundPaint = Paint().apply {
            style = Paint.Style.STROKE
            color = Color.LTGRAY
            this.strokeWidth = strokeWidth
            isAntiAlias = true
        }

        val progressPaint = Paint().apply {
            style = Paint.Style.STROKE
            color = if (ratio <= 0f) Color.GREEN else Color.RED
            this.strokeWidth = strokeWidth
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
        }

        val rect = RectF(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )

        // Draw full background circle
        canvas.drawArc(rect, 0f, 360f, false, backgroundPaint)

        // Draw progress arc
        if (ratio > 0f) {
            val sweep = ratio.coerceIn(0f, 1f) * 360f
            canvas.drawArc(rect, -90f, sweep, false, progressPaint)
        }

        return bitmap
    }
}
