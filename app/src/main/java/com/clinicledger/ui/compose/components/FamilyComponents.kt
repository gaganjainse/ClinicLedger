package com.clinicledger.ui.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.clinicledger.R
import com.clinicledger.data.models.FamilyGroup
import com.clinicledger.data.models.Village
import com.clinicledger.ui.util.LocaleManager

/**
 * Dialog for creating or editing family groups.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFamilyGroupDialog(
    /** list of villages */
    villages: List<Village>,
    /** dismiss callback */
    onDismiss: () -> Unit,
    /** editing model */
    existingFamily: FamilyGroup? = null,
    /** result callback */
    onCreate: (name: String, caste: String, headName: String, villageId: Long) -> Unit,
) {
    var name by remember { mutableStateOf(existingFamily?.name ?: "") }
    var caste by remember { mutableStateOf(existingFamily?.caste ?: "") }
    var headName by remember { mutableStateOf(existingFamily?.familyHeadName ?: "") }
    var selectedVillage by remember { 
        mutableStateOf(
            villages.find { it.id == existingFamily?.villageId } ?: villages.firstOrNull(),
        ) 
    }
    var expanded by remember { mutableStateOf(value = false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = if (existingFamily != null) {
                        stringResource(R.string.edit_family_header)
                    } else {
                        stringResource(R.string.create_family_dialog_title)
                    }, 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.Bold,
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.family_name_field_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Rounded.Groups, null) },
                )

                OutlinedTextField(
                    value = caste,
                    onValueChange = { caste = it },
                    label = { Text(stringResource(R.string.caste_optional_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )

                OutlinedTextField(
                    value = headName,
                    onValueChange = { headName = it },
                    label = { Text(stringResource(R.string.head_name_optional_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )

                // Village Selector
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = selectedVillage?.let { 
                            LocaleManager.getLocalizedVillage(it.name, it.nameHindi) 
                        } ?: "",
                        onValueChange = {},
                        label = { Text(stringResource(R.string.hint_village)) },
                        trailingIcon = { 
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(12.dp),
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        villages.forEach { /** target */ village ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = LocaleManager.getLocalizedVillage(
                                            village.name, 
                                            village.nameHindi,
                                        ),
                                    ) 
                                },
                                onClick = {
                                    selectedVillage = village
                                    expanded = false
                                },
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                    Button(
                        onClick = {
                            val village = selectedVillage
                            if (name.isNotBlank() && (village != null)) {
                                onCreate(name, caste, headName, village.id)
                            }
                        },
                        enabled = name.isNotBlank() && (selectedVillage != null),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(
                            if (existingFamily != null) {
                                stringResource(R.string.save)
                            } else {
                                stringResource(R.string.create_family_button_label)
                            },
                        )
                    }
                }
            }
        }
    }
}
