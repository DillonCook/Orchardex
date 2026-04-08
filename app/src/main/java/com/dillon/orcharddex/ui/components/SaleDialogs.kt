package com.dillon.orcharddex.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dillon.orcharddex.data.model.SaleChannel
import com.dillon.orcharddex.time.OrchardTime
import com.dillon.orcharddex.ui.displayAmount
import java.time.LocalDate

data class SaleDraftState(
    val soldDate: LocalDate = OrchardTime.today(),
    val quantityValue: String = "",
    val quantityUnit: String = "",
    val unitPrice: String = "",
    val saleChannel: SaleChannel = SaleChannel.DIRECT,
    val notes: String = ""
)

@Composable
fun SaleDialog(
    title: String,
    confirmLabel: String,
    state: SaleDraftState,
    onStateChange: (SaleDraftState) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    quantityEnabled: Boolean = true,
    unitEnabled: Boolean = true,
    unitPriceLabel: String = "Unit price",
    remainingLabel: String? = null,
    errorMessage: String? = null,
    saving: Boolean = false
) {
    val quantityValue = state.quantityValue.toDoubleOrNull()
    val unitPrice = state.unitPrice.toDoubleOrNull()
    val totalPrice = if (quantityValue != null && unitPrice != null) quantityValue * unitPrice else null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DateField(
                    label = "Sale date",
                    value = state.soldDate,
                    onDateSelected = { onStateChange(state.copy(soldDate = it)) }
                )
                OutlinedTextField(
                    value = state.quantityValue,
                    onValueChange = { onStateChange(state.copy(quantityValue = it)) },
                    label = { Text("Quantity") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = quantityEnabled,
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.quantityUnit,
                    onValueChange = { onStateChange(state.copy(quantityUnit = it)) },
                    label = { Text("Unit") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = unitEnabled,
                    singleLine = true
                )
                remainingLabel?.let { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedTextField(
                    value = state.unitPrice,
                    onValueChange = { onStateChange(state.copy(unitPrice = it)) },
                    label = { Text(unitPriceLabel) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                totalPrice?.let { total ->
                    Text(
                        text = "Gross revenue: $${total.displayAmount()}",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Sale channel",
                        style = MaterialTheme.typography.labelLarge
                    )
                    androidx.compose.foundation.layout.FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SaleChannel.entries.forEach { channel ->
                            FilterChip(
                                selected = state.saleChannel == channel,
                                onClick = { onStateChange(state.copy(saleChannel = channel)) },
                                label = { Text(channel.label) }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = state.notes,
                    onValueChange = { onStateChange(state.copy(notes = it)) },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                errorMessage?.takeIf(String::isNotBlank)?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !saving) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !saving) {
                Text("Cancel")
            }
        }
    )
}
