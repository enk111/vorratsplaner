package com.example.enkkaufsliste

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ConnectDialog(
    error: String?,
    onCreate: () -> Unit,
    onJoin: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Liste verbinden") },
        text = {
            Column {
                Text("Lege eine neue gemeinsame Liste an oder tritt einer bestehenden Liste mit einem PIN bei.")
                Spacer(Modifier.height(12.dp))
                Button(onClick = onCreate, modifier = Modifier.fillMaxWidth()) {
                    Text("Neue Liste anlegen")
                }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = pin,
                    onValueChange = { v -> pin = v.filter { c -> c.isDigit() }.take(6) },
                    singleLine = true,
                    label = { Text("PIN (6 Ziffern)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { onJoin(pin) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Mit PIN beitreten")
                }
                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } }
    )
}

@Composable
fun PinDialog(pin: String, onLeave: () -> Unit, onDismiss: () -> Unit) {
    var confirmLeave by remember { mutableStateOf(false) }
    if (confirmLeave) {
        AlertDialog(
            onDismissRequest = { confirmLeave = false },
            title = { Text("Liste verlassen?") },
            text = {
                Text("Die Artikel auf diesem Gerät werden entfernt. Die Liste bleibt für andere erhalten, und du kannst ihr später mit einem PIN wieder beitreten.")
            },
            confirmButton = { TextButton(onClick = onLeave) { Text("Verlassen") } },
            dismissButton = { TextButton(onClick = { confirmLeave = false }) { Text("Abbrechen") } }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Liste teilen") },
            text = {
                Column {
                    Text(
                        pin.chunked(3).joinToString(" "),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Gültig für 10 Minuten, einmal verwendbar. Gib den PIN in der App der zweiten Person ein.")
                }
            },
            confirmButton = { TextButton(onClick = onDismiss) { Text("Fertig") } },
            dismissButton = {
                TextButton(onClick = { confirmLeave = true }) {
                    Text("Liste verlassen", color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }
}