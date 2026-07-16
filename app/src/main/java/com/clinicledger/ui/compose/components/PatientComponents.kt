package com.clinicledger.ui.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.clinicledger.R
import com.clinicledger.data.models.Patient
import com.clinicledger.data.models.Village
import com.clinicledger.ui.util.LocaleManager

/**
 * A card representing a single patient in a list.
 * Shows name, village, and current balance with color-coded status.
 */
@Composable
fun PatientListItem(
    patient: Patient,
    index: Int? = null,
    isHindi: Boolean,
    onClick: () -> Unit
) {
    val accentColor = if (patient.currentBalance > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val containerColor = if (patient.currentBalance > 0) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.05f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Patient Avatar / Index
            Surface(
                shape = CircleShape,
                color = containerColor,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (index != null) {
                        Text(
                            text = (index + 1).toString(),
                            fontWeight = FontWeight.Black,
                            color = accentColor.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.titleMedium
                        )
                    } else {
                        Icon(Icons.Rounded.Person, contentDescription = null, tint = accentColor)
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = LocaleManager.formatPatientName(patient.name),
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Village & Status Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Rounded.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                    )
                    Text(
                        text = patient.village?.let {
                            if (isHindi) it.nameHindi.ifEmpty { it.name } else it.name
                        } ?: stringResource(R.string.no_village),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1
                    )
                }
            }

            // Financial Status Column
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = LocaleManager.formatCurrency(patient.currentBalance),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = accentColor
                )
                if (patient.currentBalance > 0) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (isHindi) "बकाया" else "DEBT",
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dialog for adding a new patient or editing an existing one.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPatientDialog(
    villages: List<Village>,
    onDismiss: () -> Unit,
    onAddPatient: (Patient) -> Unit,
    existingPatient: Patient? = null
) {
    var name by remember { mutableStateOf(existingPatient?.name ?: "") }
    var phone by remember { mutableStateOf(existingPatient?.phone ?: "") }
    var selectedVillage by remember { mutableStateOf(villages.find { it.id == existingPatient?.villageId }) }
    var expanded by remember { mutableStateOf(value = false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (existingPatient != null) "Edit Patient" else stringResource(R.string.add_patient_title),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.patient_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(stringResource(R.string.phone_number_optional)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = selectedVillage?.let { LocaleManager.getLocalizedVillage(it.name, it.nameHindi) } ?: stringResource(R.string.select_village_label),
                        onValueChange = {},
                        label = { Text(stringResource(R.string.village_label)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        villages.forEach { village ->
                            DropdownMenuItem(
                                text = { Text(LocaleManager.getLocalizedVillage(village.name, village.nameHindi)) },
                                onClick = {
                                    selectedVillage = village
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && (selectedVillage != null)) {
                        val p = existingPatient?.copy(
                            name = name,
                            phone = phone,
                            villageId = selectedVillage!!.id
                        ) ?: Patient(
                            name = name,
                            phone = phone,
                            villageId = selectedVillage!!.id
                        )
                        onAddPatient(p)
                    }
                },
                enabled = name.isNotBlank() && (selectedVillage != null)
            ) {
                Text(if (existingPatient != null) "Update" else stringResource(R.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

/**
 * Generic card for entities like Villages or Family Groups with Edit and Delete actions.
 */
@Composable
fun EntityCard(
    name: String,
    icon: ImageVector,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = name,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface
            )

            IconButton(onClick = onEdit) {
                Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
