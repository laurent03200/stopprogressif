package com.example.stopprogressif

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.widget.RemoteViews
import com.example.stopprogressif.util.WidgetGraphics
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit
import com.example.stopprogressif.data.DataStoreManager


class ProgressWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (widgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_progress_graphic)

        CoroutineScope(Dispatchers.IO).launch {
            val dataStore = DataStoreManager(context)
            val (initRestant, _, timestamp) = dataStore.loadStateWithTimestamp()

            val now = SystemClock.elapsedRealtime()
            val diff = now - timestamp
            val remaining = (initRestant - diff).coerceAtLeast(0)

            val minutes = TimeUnit.MILLISECONDS.toMinutes(remaining)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(remaining) % 60
            val formatted = String.format("%02d:%02d", minutes, seconds)

            val progressRatio = if (initRestant > 0) remaining.toFloat() / initRestant else 0f
            val bitmap = WidgetGraphics.generateProgressBitmap(context, 1f - progressRatio)

            views.setImageViewBitmap(R.id.widget_circle_image, bitmap)
            views.setTextViewText(R.id.widget_timer_text, formatted)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    companion object {
        fun refreshAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, ProgressWidgetProvider::class.java))
            for (id in ids) {
                val intent = Intent(context, ProgressWidgetProvider::class.java)
                intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                context.sendBroadcast(intent)
            }
        }
    }
}