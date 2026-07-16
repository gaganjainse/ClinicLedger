package com.clinicledger.ui.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.clinicledger.data.models.Village
import com.clinicledger.ui.util.LocaleManager

/**
 * Dialog for creating a new Family Group and associating it with a village.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFamilyGroupDialog(
    villages: List<Village>,
    onDismiss: () -> Unit,
    onCreate: (name: String, caste: String, headName: String, villageId: Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var caste by remember { mutableStateOf("") }
    var headName by remember { mutableStateOf("") }
    var selectedVillage by remember { mutableStateOf(villages.firstOrNull()) }
    var expanded by remember { mutableStateOf(value = false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Family Group", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Family Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = caste,
                    onValueChange = { caste = it },
                    label = { Text("Caste / Category") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = headName,
                    onValueChange = { headName = it },
                    label = { Text("Family Head Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = selectedVillage?.let { LocaleManager.getLocalizedVillage(it.name, it.nameHindi) } ?: "Select Village",
                        onValueChange = {},
                        label = { Text("Village") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable),
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
                onClick = { if (name.isNotBlank() && (selectedVillage != null)) onCreate(name, caste, headName, selectedVillage!!.id) },
                enabled = name.isNotBlank() && (selectedVillage != null)
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
