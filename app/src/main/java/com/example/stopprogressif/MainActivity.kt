package com.example.stopprogressif

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.compose.rememberNavController
import com.example.stopprogressif.ui.theme.StopProgressifTheme
import com.example.stopprogressif.viewmodel.ProgressifViewModel

class MainActivity : ComponentActivity() {

    private val progressifViewModel: ProgressifViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StopProgressifTheme {
                val navController = rememberNavController()

                NavGraph(
                    navController = navController,
                    progressifViewModel = progressifViewModel
                )
            }
        }
    }
}
