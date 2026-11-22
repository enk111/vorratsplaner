package com.example.enkkaufsliste

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.enkkaufsliste.data.ShoppingItem

@Composable
fun ShoppingScreen(vm: ItemViewModel) {
    val items by vm.items.collectAsState()
    val suggestions by vm.suggestions.collectAsState()
    val connected by vm.connected.collectAsState()
    val online by vm.online.collectAsState()
    val pin by vm.pin.collectAsState()
    val error by vm.error.collectAsState()
    var showConnect by remember { mutableStateOf(false) }
    var qty by remember { mutableStateOf("1") }
    var name by remember { mutableStateOf("") }
    val open = items.filter { !it.checked }
    val done = items.filter { it.checked }

    val submit = {
        if (name.isNotBlank()) {
            vm.add(name, qty)
            name = ""
            qty = "1"
        }
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxSize().imePadding()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .statusBarsPadding()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Enk Vorratsplaner",
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    when {
                        !connected -> "Nur lokal"
                        online -> "Synchronisiert"
                        else -> "Offline"
                    },
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 13.sp
                )
                TextButton(onClick = { if (connected) vm.showPin() else showConnect = true }) {
                    Text(
                        if (connected) "Teilen" else "Verbinden",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = qty,
                        onValueChange = { qty = it },
                        modifier = Modifier.width(88.dp),
                        singleLine = true,
                        placeholder = { Text("Menge") }
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            vm.updateSuggestions(it)
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text("Produkt hinzufügen") },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { submit() })
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { submit() }) { Text("+", fontSize = 20.sp) }
                }
                if (suggestions.isNotEmpty()) {
                    Row(
                        Modifier
                            .padding(top = 8.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        suggestions.forEach { s ->
                            Text(
                                s,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                    .clickable {
                                        name = s
                                        vm.updateSuggestions(s)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                if (items.isEmpty()) {
                    item {
                        Text(
                            "Die Liste ist leer. Trage oben dein erstes Produkt ein.",
                            modifier = Modifier.fillMaxWidth().padding(48.dp),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                items(open, key = { it.id }) { ItemRow(it, vm) }
                if (done.isNotEmpty()) {
                    item {
                        Text(
                            "Erledigt",
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                    items(done, key = { it.id }) { ItemRow(it, vm) }
                }
            }
        }

        if (done.isNotEmpty()) {
            Button(
                onClick = { vm.clearChecked() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                Text("Abgehakte entfernen (${done.size})")
            }
        }
    }

    if (showConnect && !connected) {
        ConnectDialog(
            error = error,
            onCreate = { vm.createList() },
            onJoin = { vm.joinWithPin(it) },
            onDismiss = {
                showConnect = false
                vm.clearError()
            }
        )
    }
    pin?.let { PinDialog(pin = it, onLeave = { vm.leaveList() }, onDismiss = { vm.dismissPin() }) }
}

@Composable
fun ItemRow(item: ShoppingItem, vm: ItemViewModel) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val strike = if (item.checked) TextDecoration.LineThrough else TextDecoration.None
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = item.checked, onCheckedChange = { vm.setChecked(item, it) })
        Text(
            item.quantity,
            modifier = Modifier.widthIn(min = 52.dp),
            fontWeight = FontWeight.Bold,
            color = if (item.checked) muted else MaterialTheme.colorScheme.primary,
            textDecoration = strike
        )
        Text(
            item.name,
            modifier = Modifier.weight(1f),
            color = if (item.checked) muted else MaterialTheme.colorScheme.onSurface,
            textDecoration = strike
        )
        TextButton(onClick = { vm.delete(item) }) { Text("✕", color = muted) }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}