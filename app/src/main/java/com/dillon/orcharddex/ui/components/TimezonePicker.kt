package com.dillon.orcharddex.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import java.time.ZoneId

private val timezoneOptions = ZoneId.getAvailableZoneIds().sorted()

@Composable
fun TimezonePickerField(
    label: String,
    value: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    testTag: String? = null
) {
    var showPicker by rememberSaveable { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        modifier = modifier
            .clickable { showPicker = true }
            .let { base -> if (testTag == null) base else base.testTag(testTag) },
        label = { Text(label) },
        supportingText = supportingText?.let { text -> { Text(text) } }
    )
    if (showPicker) {
        TimezonePickerDialog(
            selected = value,
            onDismiss = { showPicker = false },
            onSelected = {
                onSelected(it)
                showPicker = false
            }
        )
    }
}

@Composable
fun TimezonePickerDialog(
    selected: String,
    onDismiss: () -> Unit,
    onSelected: (String) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    val matches = remember(query) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            timezoneOptions
        } else {
            timezoneOptions.filter { timezoneId ->
                timezoneId.contains(trimmed, ignoreCase = true)
            }
        }.take(120)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select timezone") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Search timezone") }
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(matches, key = { it }) { timezoneId ->
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelected(timezoneId) }
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = timezoneId,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (timezoneId == selected) {
                                    Text(
                                        text = "Selected",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
