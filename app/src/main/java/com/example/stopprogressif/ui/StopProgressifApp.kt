package com.example.stopprogressif.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.stopprogressif.NavGraph
import com.example.stopprogressif.viewmodel.ProgressifViewModel

@Composable
fun StopProgressifApp(progressifViewModel: ProgressifViewModel) {
    val navController = rememberNavController()
    NavGraph(
        navController = navController,
        progressifViewModel = progressifViewModel
    )
}