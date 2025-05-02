package com.stopprogressif

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
    navController: NavController
) {
    // ⚡️ même scoping : la VM est partagée sur l’activité
    val activity = LocalContext.current as ComponentActivity
    val progressVM: ProgressifViewModel = viewModel(
        viewModelStoreOwner = activity,
        factory = ProgressifViewModelFactory(activity.application)
    )

    val settings by progressVM.settingsData.collectAsState()

    var prixPaquet by rememberSaveable { mutableStateOf("") }
    var parPaquet by rememberSaveable { mutableStateOf("") }
    var mode by rememberSaveable { mutableStateOf(SettingsData.MODE_OBJECTIF) }
    var objectif by rememberSaveable { mutableStateOf("") }
    var heuresInt by rememberSaveable { mutableStateOf("") }
    var minutesInt by rememberSaveable { mutableStateOf("") }
    var habitude by rememberSaveable { mutableStateOf("") }
    var heureDebut by rememberSaveable { mutableStateOf("") }
    var heureFin by rememberSaveable { mutableStateOf("") }

    // on recharge dès que settings change
    LaunchedEffect(settings) {
        prixPaquet  = settings.prixPaquet.toString()
        parPaquet   = settings.cigarettesParPaquet.toString()
        mode        = settings.mode
        objectif    = settings.objectifParJour.toString()
        heuresInt   = settings.heuresEntreCigarettes.toString()
        minutesInt  = settings.minutesEntreCigarettes.toString()
        habitude    = settings.cigarettesHabituelles.toString()
        heureDebut  = settings.heuresDebut.toString()
        heureFin    = settings.heuresFin.toString()
    }
    // quand on change de mode en live
    LaunchedEffect(mode) {
        if (mode == SettingsData.MODE_INTERVALLE) {
            heuresInt  = settings.heuresEntreCigarettes.toString()
            minutesInt = settings.minutesEntreCigarettes.toString()
        } else {
            objectif = settings.objectifParJour.toString()
        }
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Paramètres", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Retour")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            /* Bloc 1 – Paquet */
            SettingsCard("Informations sur le paquet") {
                NumberField(prixPaquet, "Prix du paquet (€)")   { prixPaquet   = it }
                NumberField(parPaquet,  "Cigarettes par paquet") { parPaquet    = it }
            }

            /* Bloc 2 – Mode */
            SettingsCard("Mode de progression") {
                ModeChips(mode) { mode = it }
                Spacer(Modifier.height(8.dp))
                if (mode == SettingsData.MODE_OBJECTIF) {
                    NumberField(objectif, "Objectif / jour") { objectif = it }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        NumberField(
                            value    = heuresInt,
                            label    = "Heures",
                            modifier = Modifier.weight(1f)
                        ) { heuresInt  = it.filter { c -> c.isDigit() } }
                        NumberField(
                            value    = minutesInt,
                            label    = "Minutes",
                            modifier = Modifier.weight(1f)
                        ) { minutesInt = it.filter { c -> c.isDigit() } }
                    }
                }
            }

            /* Bloc 3 – Habitude & Plage horaire */
            SettingsCard("Habitude & plage horaire active") {
                NumberField(habitude, "Cigarettes habituelles / jour") { habitude = it }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NumberField(
                        value    = heureDebut,
                        label    = "Début (0‑23h)",
                        modifier = Modifier.weight(1f)
                    ) { heureDebut = it.filter { c -> c.isDigit() } }
                    NumberField(
                        value    = heureFin,
                        label    = "Fin (0‑23h)",
                        modifier = Modifier.weight(1f)
                    ) { heureFin   = it.filter { c -> c.isDigit() } }
                }
            }

            Button(
                onClick = {
                    val hI = heuresInt.toIntOrNull()?.coerceAtLeast(0) ?: 0
                    val mI = minutesInt.toIntOrNull()?.coerceIn(0,59) ?: 0
                    val hD = heureDebut.toIntOrNull()?.coerceIn(0,23) ?: 7
                    val hF = heureFin.toIntOrNull()?.coerceIn(0,23) ?: 23

                    progressVM.saveSettings(
                        SettingsData(
                            prixPaquet              = prixPaquet.toFloatOrNull() ?: 10f,
                            cigarettesParPaquet     = parPaquet.toIntOrNull() ?: 20,
                            mode                    = mode,
                            objectifParJour         = objectif.toIntOrNull() ?: 20,
                            heuresEntreCigarettes   = hI,
                            minutesEntreCigarettes  = mI,
                            cigarettesHabituelles   = habitude.toIntOrNull() ?: 30,
                            heuresDebut             = hD,
                            minutesDebut            = 0,
                            heuresFin               = hF,
                            minutesFin              = 0
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

/*────────────────────────────────────────────────────────────────────────────*/

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
        value           = value,
        onValueChange   = { onValueChange(it.filter { c -> c.isDigit() || c == '.' }) },
        label           = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier        = modifier.fillMaxWidth(),
        singleLine      = true
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
            onClick  = { onChange(SettingsData.MODE_OBJECTIF) },
            label    = { Text("Objectif / jour") }
        )
        FilterChip(
            selected = selected == SettingsData.MODE_INTERVALLE,
            onClick  = { onChange(SettingsData.MODE_INTERVALLE) },
            label    = { Text("Intervalle") }
        )
    }
}
