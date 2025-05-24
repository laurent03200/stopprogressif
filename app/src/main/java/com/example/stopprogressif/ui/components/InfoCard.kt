
package com.example.stopprogressif.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InfoCard(label: String, value: String, color: Color) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .width(150.dp)
            .height(100.dp)
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontSize = 20.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(text = label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}
