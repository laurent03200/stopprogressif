package com.stopprogressif

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/** Représente un mode avec son identifiant et son libellé affiché. */
private data class Mode(val id: String, val label: String)

/** Liste des modes proposés. */
private val MODES = listOf(
    Mode("objectif", "Par Objectif"),
    Mode("intermittence", "Par Intermittence")
)

@Composable
fun ModeSelector(
    selectedMode: String,
    onModeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MODES.forEach { mode ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .selectable(
                        selected = mode.id == selectedMode,
                        onClick = { onModeChange(mode.id) },
                        role = Role.RadioButton
                    )
                    .semantics { contentDescription = mode.label }
            ) {
                RadioButton(
                    selected = mode.id == selectedMode,
                    onClick = null      // Click géré par Row.selectable
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = mode.label,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
