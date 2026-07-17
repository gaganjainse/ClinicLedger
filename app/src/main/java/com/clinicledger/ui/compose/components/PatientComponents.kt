package com.clinicledger.ui.compose.components

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.clinicledger.R
import com.clinicledger.data.models.Patient
import com.clinicledger.data.models.Village
import com.clinicledger.service.PhotoStorageService
import com.clinicledger.ui.util.LocaleManager

/**
 * Standard list item for displaying patient summary in search and dashboard.
 */
@Composable
fun PatientListItem(
    /** Patient model */
    patient: Patient,
    /** Optional index for stylized numbering */
    index: Int? = null,
    /** Current locale (Legacy - UI now uses string resources) */
    isHindi: Boolean = false,
    /** Click handler */
    onClick: () -> Unit,
) {
    val accentColor = if (patient.currentBalance > 0) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.05f),
        ),
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Avatar with Index
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                if (index != null) {
                    Text(
                        text = (index + 1).toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = accentColor,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Person, 
                        contentDescription = null, 
                        tint = accentColor,
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = LocaleManager.formatPatientName(patient.name),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                Text(
                    text = if (patient.phone.isNotBlank()) {
                        patient.phone
                    } else {
                        stringResource(R.string.no_phone_label)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = LocaleManager.formatCurrency(patient.currentBalance),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = accentColor,
                )
                if (patient.currentBalance > 0) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.error,
                    ) {
                        Text(
                            text = stringResource(R.string.debt_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            fontSize = 8.sp,
                        )
                    }
                } else {
                    Text(
                        text = stringResource(R.string.settled_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

/**
 * Dialog for adding or editing a patient profile.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPatientDialog(
    /** Registered villages for selection */
    villages: List<Village>,
    /** Callback to close dialog */
    onDismiss: () -> Unit,
    /** Callback with new/updated patient model */
    onAddPatient: (Patient) -> Unit,
    /** Optional model for editing mode */
    existingPatient: Patient? = null,
) {
    var name by remember { mutableStateOf(existingPatient?.name ?: "") }
    var phone by remember { mutableStateOf(existingPatient?.phone ?: "") }
    var selectedVillage by remember { 
        mutableStateOf(villages.find { it.id == existingPatient?.villageId } ?: villages.firstOrNull()) 
    }
    val relationship = remember { existingPatient?.relationship ?: "Self" }
    var villageExpanded by remember { mutableStateOf(value = false) }
    var photoPath by remember { mutableStateOf(existingPatient?.photoPath) }

    val context = LocalContext.current
    val photoStorage = remember { PhotoStorageService(context) }

    val cameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview(),
    ) { bitmap ->
        bitmap?.let {
            photoPath = photoStorage.savePatientPhoto(it)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = if (existingPatient != null) {
                        stringResource(R.string.edit_patient_title)
                    } else {
                        stringResource(R.string.add_patient_title)
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // Photo Picker
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { cameraLauncher.launch(null) },
                    contentAlignment = Alignment.Center,
                ) {
                    if (photoPath != null) {
                        val bitmap = remember(photoPath) { photoStorage.loadPhoto(photoPath!!) }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                        }
                    } else {
                        Icon(Icons.Rounded.AddAPhoto, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
                TextButton(onClick = { cameraLauncher.launch(null) }) {
                    Text(
                        if (photoPath == null) {
                            stringResource(R.string.add_profile_photo_btn)
                        } else {
                            stringResource(R.string.change_photo_btn)
                        },
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.hint_name_required)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(stringResource(R.string.hint_phone_optional)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Village Dropdown
                ExposedDropdownMenuBox(
                    expanded = villageExpanded,
                    onExpandedChange = { villageExpanded = it },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = selectedVillage?.let { 
                            LocaleManager.getLocalizedVillage(it.name, it.nameHindi) 
                        } ?: "",
                        onValueChange = {},
                        label = { Text(stringResource(R.string.hint_village)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = villageExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    )
                    ExposedDropdownMenu(
                        expanded = villageExpanded,
                        onDismissRequest = { villageExpanded = false },
                    ) {
                        villages.forEach { village ->
                            DropdownMenuItem(
                                text = { 
                                    Text(LocaleManager.getLocalizedVillage(village.name, village.nameHindi)) 
                                },
                                onClick = {
                                    selectedVillage = village
                                    villageExpanded = false
                                },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank() && selectedVillage != null) {
                                val villageId = selectedVillage!!.id
                                onAddPatient(
                                    existingPatient?.copy(
                                        name = name,
                                        phone = phone,
                                        villageId = villageId,
                                        relationship = relationship,
                                        photoPath = photoPath,
                                    ) ?: Patient(
                                        name = name,
                                        phone = phone,
                                        villageId = villageId,
                                        relationship = relationship,
                                        photoPath = photoPath,
                                    ),
                                )
                            }
                        },
                        enabled = name.isNotBlank() && selectedVillage != null,
                    ) {
                        Text(
                            if (existingPatient != null) {
                                stringResource(R.string.update_btn)
                            } else {
                                stringResource(R.string.add)
                            },
                        )
                    }
                }
            }
        }
    }
}

/**
 * Visual card for managing entities like Villages or Family Groups.
 */
@Composable
fun EntityCard(
    /** Primary name */
    name: String,
    /** Representative icon */
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    /** Edit handler */
    onEdit: () -> Unit,
    /** Delete handler */
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Rounded.Edit, 
                        contentDescription = stringResource(R.string.edit_btn), 
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Rounded.Delete, 
                        contentDescription = stringResource(R.string.delete), 
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}
