package com.stopprogressif

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    progressVM: ProgressifViewModel = viewModel(
        factory = ProgressifViewModelFactory(
            LocalContext.current.applicationContext as Application
        )
    )
) {
    val settings by progressVM.settingsData.collectAsState()

    // États des champs, initialisés directement depuis `settings`
    var prixPaquet by rememberSaveable { mutableStateOf(settings.prixPaquet.toString()) }
    var parPaquet by rememberSaveable { mutableStateOf(settings.cigarettesParPaquet.toString()) }
    var mode by rememberSaveable { mutableStateOf(settings.mode) }
    var objectif by rememberSaveable { mutableStateOf(settings.objectifParJour.toString()) }
    var heuresInt by rememberSaveable { mutableStateOf(settings.heuresEntreCigarettes.toString()) }
    var minutesInt by rememberSaveable { mutableStateOf(settings.minutesEntreCigarettes.toString()) }
    var habitude by rememberSaveable { mutableStateOf(settings.cigarettesHabituelles.toString()) }
    var heureDebut by rememberSaveable { mutableStateOf(settings.heuresDebut.toString()) }
    var heureFin by rememberSaveable { mutableStateOf(settings.heuresFin.toString()) }

    // Quand les valeurs de `settings` changent, on resynchronise les champs
    LaunchedEffect(settings) {
        prixPaquet = settings.prixPaquet.toString()
        parPaquet = settings.cigarettesParPaquet.toString()
        mode = settings.mode
        objectif = settings.objectifParJour.toString()
        heuresInt = settings.heuresEntreCigarettes.toString()
        minutesInt = settings.minutesEntreCigarettes.toString()
        habitude = settings.cigarettesHabituelles.toString()
        heureDebut = settings.heuresDebut.toString()
        heureFin = settings.heuresFin.toString()
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Paramètres", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Bloc 1 : paquet
            SettingsCard("Informations sur le paquet") {
                NumberField(prixPaquet, "Prix du paquet (€)") { prixPaquet = it }
                NumberField(parPaquet, "Cigarettes par paquet") { parPaquet = it }
            }

            // Bloc 2 : mode
            SettingsCard("Mode de progression") {
                ModeChips(mode) { mode = it }
                Spacer(Modifier.height(8.dp))
                if (mode == SettingsData.MODE_OBJECTIF) {
                    NumberField(objectif, "Objectif / jour") { objectif = it }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        NumberField(
                            heuresInt,
                            "Heures",
                            Modifier.fillMaxWidth(0.5f)
                        ) { heuresInt = it.filter { c -> c.isDigit() } }
                        NumberField(
                            minutesInt,
                            "Minutes",
                            Modifier.fillMaxWidth(0.5f)
                        ) { minutesInt = it.filter { c -> c.isDigit() } }
                    }
                }
            }

            // Bloc 3 : habitude & plage horaire
            SettingsCard("Habitude & plage horaire active") {
                NumberField(habitude, "Cigarettes habituelles / jour") { habitude = it }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NumberField(
                        heureDebut, "Début (0‑23h)", Modifier.fillMaxWidth(0.5f)
                    ) { heureDebut = it.filter { c -> c.isDigit() } }
                    NumberField(
                        heureFin, "Fin (0‑23h)", Modifier.fillMaxWidth(0.5f)
                    ) { heureFin = it.filter { c -> c.isDigit() } }
                }
            }

            // Bouton Sauvegarder
            Button(
                onClick = {
                    // Lecture et validation des inputs
                    val prix = prixPaquet.toFloatOrNull() ?: settings.prixPaquet
                    val par = parPaquet.toIntOrNull() ?: settings.cigarettesParPaquet
                    val obj = objectif.toIntOrNull() ?: settings.objectifParJour
                    val hInt = heuresInt.toIntOrNull() ?: settings.heuresEntreCigarettes
                    val mInt = minutesInt.toIntOrNull() ?: settings.minutesEntreCigarettes
                    val hab = habitude.toIntOrNull() ?: settings.cigarettesHabituelles
                    val hDeb = heureDebut.toIntOrNull() ?: settings.heuresDebut
                    val hFinVal = heureFin.toIntOrNull() ?: settings.heuresFin

                    progressVM.saveSettings(
                        SettingsData(
                            prixPaquet = prix,
                            cigarettesParPaquet = par,
                            mode = mode,
                            objectifParJour = obj,
                            heuresDebut = hDeb,
                            minutesDebut = 0,
                            heuresFin = hFinVal,
                            minutesFin = 0,
                            heuresEntreCigarettes = hInt,
                            minutesEntreCigarettes = mInt,
                            cigarettesHabituelles = hab
                        )
                    )
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Sauvegarder", fontSize = 18.sp)
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            content()
        }
    }
}

@Composable
private fun NumberField(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { txt -> onValueChange(txt.filter { c -> c.isDigit() || c == '.' }) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Composable
private fun ModeChips(
    selected: String,
    onChange: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        FilterChip(
            selected = selected == SettingsData.MODE_OBJECTIF,
            onClick = { onChange(SettingsData.MODE_OBJECTIF) },
            label = { Text("Objectif / jour") }
        )
        FilterChip(
            selected = selected == SettingsData.MODE_INTERVALLE,
            onClick = { onChange(SettingsData.MODE_INTERVALLE) },
            label = { Text("Intervalle") }
        )
    }
}
