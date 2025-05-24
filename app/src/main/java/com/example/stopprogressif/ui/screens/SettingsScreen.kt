package com.example.stopprogressif.ui.screens

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.stopprogressif.ui.components.TopBarAvecMenu
import com.example.stopprogressif.viewmodel.ProgressifViewModel
import com.example.stopprogressif.viewmodel.SettingsViewModel
import com.example.stopprogressif.viewmodel.SettingsViewModelFactory
import com.example.stopprogressif.ui.components.DisplayCard
import com.example.stopprogressif.ui.components.EditableField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    progressifViewModel: ProgressifViewModel
) {
    val context = LocalContext.current
    val factory = SettingsViewModelFactory(context.applicationContext as Application)
    val viewModel: SettingsViewModel = viewModel(factory = factory)

    val hours by viewModel.hoursState.collectAsState()
    val minutes by viewModel.minutesState.collectAsState()
    val price by viewModel.priceState.collectAsState()
    val cigarettesPerPack by viewModel.cigarettesPerPackState.collectAsState()
    val cigarettesHabituelles by viewModel.cigarettesHabituellesState.collectAsState()

    var isEditing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopBarAvecMenu(
                title = if (isEditing) "Mode édition" else "Réglages",
                navController = navController,
                showBack = true
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { isEditing = !isEditing },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (isEditing) "Annuler l'édition" else "Éditer les réglages",
                    fontSize = 16.sp,
                    color = Color.White
                )
            }

            if (isEditing) {
                EditableField(
                    label = "Heures entre 2 cigarettes",
                    value = hours.toString(),
                    onValueChange = viewModel::updateHours,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                EditableField(
                    label = "Minutes entre 2 cigarettes",
                    value = minutes.toString(),
                    onValueChange = viewModel::updateMinutes,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                EditableField(
                    label = "Prix du paquet",
                    value = price.toString(),
                    onValueChange = viewModel::updatePrice,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                EditableField(
                    label = "Cigarettes fumées habituellement",
                    value = cigarettesHabituelles.toString(),
                    onValueChange = viewModel::updateCigarettesHabituelles,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                EditableField(
                    label = "Cigarettes par paquet",
                    value = cigarettesPerPack.toString(),
                    onValueChange = viewModel::updateCigarettesPerPack,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            } else {
                DisplayCard(
                    "Intervalle entre 2 cigarettes autorisées",
                    "${hours}h ${minutes}min",
                    Color(0xFF007ACC)
                )

                val prixAffiche = try {
                    "%.2f €".format(price.toFloat())
                } catch (e: Exception) {
                    "$price €"
                }

                DisplayCard(
                    "Prix du paquet",
                    prixAffiche,
                    Color(0xFFE6247B)
                )
                DisplayCard(
                    "Cigarettes fumées habituellement",
                    cigarettesHabituelles.toString(),
                    Color(0xFF28A745)
                )
                DisplayCard(
                    "Cigarettes par paquet",
                    cigarettesPerPack.toString(),
                    Color(0xFF9446DB)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (isEditing) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { isEditing = false },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Annuler", fontSize = 16.sp)
                    }
                    Button(
                        onClick = {
                            viewModel.saveSettings()
                            isEditing = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE6247B)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("💾 Sauvegarder", fontSize = 16.sp, color = Color.White)
                    }
                }
            }
        }
    }
}
