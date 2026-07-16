package com.clinicledger.ui.compose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.clinicledger.data.models.FamilyGroup
import com.clinicledger.data.models.Village
import com.clinicledger.ui.util.LocaleManager
import java.util.Date

/**
 * Dialog for adding a secondary name (alias) for a patient.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAliasDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    val isHindi = LocaleManager.LocalIsHindi.current
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isHindi) "उपनाम जोड़ें" else "Add Alias") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(if (isHindi) "नाम" else "Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onConfirm(name) }) {
                Text(if (isHindi) "जोड़ें" else "Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isHindi) "रद्द करें" else "Cancel")
            }
        }
    )
}

/**
 * Dialog for linking a patient to a family group or creating a new group.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkFamilyDialog(
    familyGroups: List<FamilyGroup>,
    currentFamilyId: Long?,
    villages: List<Village>,
    onDismiss: () -> Unit,
    onLink: (Long?) -> Unit,
    onCreateNew: (String, Long) -> Unit
) {
    val isHindi = LocaleManager.LocalIsHindi.current
    var showCreateNew by remember { mutableStateOf(false) }
    var newFamilyName by remember { mutableStateOf("") }
    var selectedVillageId by remember { mutableLongStateOf(villages.firstOrNull()?.id ?: 0L) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isHindi) "परिवार से जोड़ें" else "Link to Family") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                if (showCreateNew) {
                    OutlinedTextField(
                        value = newFamilyName,
                        onValueChange = { newFamilyName = it },
                        label = { Text(if (isHindi) "नया परिवार नाम" else "New Family Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(if (isHindi) "गाँव चुनें" else "Select Village")
                    villages.forEach { v ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { selectedVillageId = v.id }) {
                            RadioButton(selected = selectedVillageId == v.id, onClick = { selectedVillageId = v.id })
                            Text(LocaleManager.getLocalizedVillage(v.name, v.nameHindi))
                        }
                    }
                } else {
                    TextButton(onClick = { showCreateNew = true }) {
                        Text(if (isHindi) "+ नया परिवार बनाएँ" else "+ Create New Family")
                    }
                    if (currentFamilyId != null) {
                        TextButton(onClick = { onLink(null) }) {
                            Text(if (isHindi) "परिवार से हटाएँ" else "Unlink from Family", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    LazyColumn {
                        items(familyGroups) { group ->
                            val isSelected = group.id == currentFamilyId
                            Surface(
                                modifier = Modifier.fillMaxWidth().clickable { onLink(group.id) },
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(group.name, modifier = Modifier.padding(12.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (showCreateNew) {
                Button(onClick = { if (newFamilyName.isNotBlank()) onCreateNew(newFamilyName, selectedVillageId) }) {
                    Text(if (isHindi) "बनाएँ और जोड़ें" else "Create & Link")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = if (showCreateNew) { { showCreateNew = false } } else onDismiss) {
                Text(if (isHindi) "रद्द करें" else "Cancel")
            }
        }
    )
}

/**
 * Dialog for adding a new medicine charge or recording a payment for the patient.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    type: String,
    isHindi: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Double, String, Date) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val title = when (type) {
        "medicine" -> if (isHindi) "दवाई जोड़ें" else "Add Medicine"
        "payment" -> if (isHindi) "जमा जोड़ें" else "Add Payment"
        else -> if (isHindi) "समायोजन" else "Adjustment"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(if (isHindi) "राशि (₹)" else "Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(if (isHindi) "विवरण" else "Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    if (amt > 0 || type == "adjustment") onConfirm(amt, notes, Date())
                },
            ) {
                Text(if (isHindi) "पुष्टि करें" else "Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isHindi) "रद्द करें" else "Cancel")
            }
        }
    )
}
