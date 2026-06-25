package com.auraplay.player.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun SleepTimerDialog(
    isRunning: Boolean,
    remainingTime: String,
    onStart: (Long) -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Bedtime, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Sleep Timer", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                if (isRunning) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(remainingTime, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onCancel, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                        Icon(Icons.Default.Close, null); Spacer(modifier = Modifier.width(8.dp)); Text("Cancel Timer")
                    }
                } else {
                    Spacer(modifier = Modifier.height(24.dp))
                    val presets = listOf(15 to "15 min", 30 to "30 min", 45 to "45 min", 60 to "1 hour", 90 to "1.5 hours", 120 to "2 hours")
                    presets.chunked(3).forEach { row ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { (minutes, label) ->
                                OutlinedButton(onClick = { onStart(minutes * 60 * 1000L) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                                    Text(label, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        }
    }
}