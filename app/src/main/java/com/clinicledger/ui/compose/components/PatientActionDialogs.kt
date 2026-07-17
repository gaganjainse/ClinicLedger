package com.clinicledger.ui.compose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.clinicledger.R
import com.clinicledger.data.models.FamilyGroup
import com.clinicledger.data.models.Patient
import com.clinicledger.data.models.Village
import com.clinicledger.ui.util.LocaleManager

/**
 * Dialog for adding a patient alias.
 */
@Composable
fun AddAliasDialog(
    /** dismiss callback */
    onDismiss: () -> Unit, 
    /** success callback */
    onConfirm: (String) -> Unit,
) {
    var alias by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_alias_label)) },
        text = {
            OutlinedTextField(
                value = alias,
                onValueChange = { alias = it },
                label = { Text(stringResource(R.string.alias_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
            )
        },
        confirmButton = {
            Button(
                onClick = { if (alias.isNotBlank()) onConfirm(alias) },
                enabled = alias.isNotBlank(),
            ) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

/**
 * Dialog to link a patient to a family group.
 */
@Composable
fun LinkFamilyDialog(
    /** choices */
    familyGroups: List<FamilyGroup>,
    /** current selection */
    currentFamilyId: Long?,
    /** villages for new group */
    villages: List<Village>,
    /** dismiss callback */
    onDismiss: () -> Unit,
    /** callback */
    onLink: (Long?) -> Unit,
    /** callback */
    onCreateNew: (String, Long) -> Unit,
) {
    var newFamilyName by remember { mutableStateOf("") }
    var selectedVillageId by remember { mutableLongStateOf(villages.firstOrNull()?.id ?: 0L) }
    var showCreateNew by remember { mutableStateOf(value = false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.link_to_family_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (showCreateNew) {
                    OutlinedTextField(
                        value = newFamilyName,
                        onValueChange = { newFamilyName = it },
                        label = { Text(stringResource(R.string.new_family_name_label)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    
                    Text(
                        text = stringResource(R.string.select_village_label), 
                        style = MaterialTheme.typography.labelSmall,
                    )
                    villages.forEach { /** village */ v ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically, 
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedVillageId = v.id }
                                .padding(vertical = 4.dp),
                        ) {
                            RadioButton(
                                selected = selectedVillageId == v.id, 
                                onClick = { selectedVillageId = v.id },
                            )
                            Text(LocaleManager.getLocalizedVillage(v.name, v.nameHindi))
                        }
                    }
                } else {
                    TextButton(onClick = { showCreateNew = true }) {
                        Text(stringResource(R.string.new_family_button))
                    }
                    
                    if (currentFamilyId != null) {
                        TextButton(onClick = { onLink(null) }) {
                            Text(
                                stringResource(R.string.unlink_family), 
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }

                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(familyGroups) { /** group */ group ->
                            ListItem(
                                headlineContent = { Text(LocaleManager.getLocalizedText(group.name)) },
                                trailingContent = {
                                    RadioButton(
                                        selected = group.id == currentFamilyId,
                                        onClick = { onLink(group.id) },
                                    )
                                },
                                modifier = Modifier.clickable { onLink(group.id) },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (showCreateNew) {
                Button(
                    onClick = { onCreateNew(newFamilyName, selectedVillageId) },
                    enabled = newFamilyName.isNotBlank(),
                ) {
                    Text(stringResource(R.string.create_and_link_btn))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}

/**
 * Dialog for adding a medicine or payment transaction.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("HardcodedStringLiteral")
@Composable
fun AddTransactionDialog(
    /** tag */
    type: String,
    /** context */
    currentPatient: Patient,
    /** data set */
    familyMembers: List<Patient>,
    /** locale (legacy) */
    isHindi: Boolean = false,
    /** dismiss callback */
    onDismiss: () -> Unit,
    /** callback */
    onConfirm: (amount: Long, notes: String, date: java.util.Date, beneficiaryId: Long?) -> Unit,
) {
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedBeneficiary by remember { mutableStateOf(currentPatient) }
    var expanded by remember { mutableStateOf(value = false) }

    val amountAsLong = amount.toLongOrNull() ?: 0L
    val isAmountValid = if (type == "payment") {
        (amountAsLong > 0) && (amountAsLong <= selectedBeneficiary.currentBalance)
    } else {
        (amountAsLong > 0) || (type == "adjustment")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (type) {
                    "medicine" -> stringResource(R.string.medicine_given_label)
                    "payment" -> stringResource(R.string.payment_received_label)
                    else -> stringResource(R.string.balance_adjustment_label)
                },
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if ((type == "payment") && familyMembers.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.paying_for_whom_label), 
                        style = MaterialTheme.typography.labelSmall,
                    )
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = if (selectedBeneficiary.id == currentPatient.id) {
                                stringResource(R.string.self_format, currentPatient.name)
                            } else {
                                selectedBeneficiary.name
                            },
                            onValueChange = {},
                            trailingIcon = { 
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) 
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        )
                        ExposedDropdownMenu(
                            expanded = expanded, 
                            onDismissRequest = { expanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { 
                                    Text(stringResource(R.string.self_format, currentPatient.name)) 
                                },
                                onClick = {
                                    selectedBeneficiary = currentPatient
                                    expanded = false
                                },
                            )
                            familyMembers.filter { it.id != currentPatient.id }.forEach { /** member */ member ->
                                DropdownMenuItem(
                                    text = { Text(member.name) },
                                    onClick = {
                                        selectedBeneficiary = member
                                        expanded = false
                                    },
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { input -> 
                        if (input.all { it.isDigit() }) amount = input 
                    },
                    label = { Text(stringResource(R.string.amount_rs_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    suffix = {
                        if (type == "payment") {
                            Text(
                                text = stringResource(
                                    R.string.max_dues_format, 
                                    LocaleManager.formatAmount(selectedBeneficiary.currentBalance),
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    },
                    isError = (type == "payment") && (amountAsLong > selectedBeneficiary.currentBalance),
                )
                
                if ((type == "payment") && (amountAsLong > selectedBeneficiary.currentBalance)) {
                    Text(
                        text = stringResource(R.string.cannot_exceed_dues_msg), 
                        style = MaterialTheme.typography.labelSmall, 
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.notes_optional_label)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    onConfirm(amountAsLong, notes, java.util.Date(), selectedBeneficiary.id) 
                },
                enabled = isAmountValid,
            ) {
                Text(stringResource(R.string.confirm_btn))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}
