package com.stopprogressif

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding      // ← import ajouté
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.stopprogressif.ui.theme.StopProgressifTheme

class NextActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { StopProgressifApp(::finish) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)            // ← opt‑in requis
@Composable
private fun StopProgressifApp(onBack: () -> Unit) {
    StopProgressifTheme {
        Scaffold(
            topBar = {
                SmallTopAppBar(
                    title = { Text("Prochaine étape") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Retour"
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),       // compile maintenant
                contentAlignment = Alignment.Center
            ) {
                Text("Vous êtes dans la prochaine activité ! 🎯")
            }
        }
    }
}
