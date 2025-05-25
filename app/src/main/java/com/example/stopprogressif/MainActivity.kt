package com.example.stopprogressif

import android.app.AlarmManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel // Import de hiltViewModel
import com.example.stopprogressif.ui.StopProgressifApp // Import de votre Composable StopProgressifApp
import com.example.stopprogressif.ui.theme.StopProgressifTheme
import com.example.stopprogressif.viewmodel.ProgressifViewModel
import dagger.hilt.android.AndroidEntryPoint // Import de l'annotation AndroidEntryPoint

@AndroidEntryPoint // Ajoute cette annotation
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Demande d'autorisation pour les alarmes exactes (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }

        setContent {
            StopProgressifTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Obtient le ViewModel via Hilt.
                    // Cette instance sera utilisée et passée au Composable StopProgressifApp.
                    val progressifViewModel: ProgressifViewModel = hiltViewModel()

                    // Appel de votre Composable principal
                    StopProgressifApp(progressifViewModel = progressifViewModel)
                }
            }
        }
    }
}