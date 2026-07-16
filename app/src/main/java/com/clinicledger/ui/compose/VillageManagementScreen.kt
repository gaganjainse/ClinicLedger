package com.clinicledger.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.clinicledger.R
import com.clinicledger.data.models.Village
import com.clinicledger.data.repository.VillageRepository
import com.clinicledger.ui.compose.components.*
import com.clinicledger.ui.util.LocaleManager
import com.clinicledger.ui.util.LocaleManager.LocalIsHindi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Village Management Content.
 * Removed internal Scaffold to prevent header nesting issues.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VillageManagementContent(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val villageRepository = remember { VillageRepository(context) }
    val isHindi = LocalIsHindi.current

    val villages by villageRepository.getAllVillages().observeAsState(emptyList())
    var inputName by remember { mutableStateOf("") }
    var editingVillage by remember { mutableStateOf<Village?>(null) }
    var villageToDelete by remember { mutableStateOf<Village?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Quick Add Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 2.dp,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    placeholder = { Text(stringResource(R.string.new_village_placeholder)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Button(
                    onClick = {
                        if (inputName.isNotBlank()) {
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    villageRepository.insertVillage(Village(name = inputName.trim()))
                                }
                                inputName = ""
                            }
                        }
                    },
                    enabled = inputName.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (villages.isEmpty()) {
                item { ClinicEmptyState(message = stringResource(R.string.no_villages_msg)) }
            } else {
                items(
                    items = villages, 
                    key = { it.id },
                    contentType = { "village" }
                ) { village ->
                    EntityCard(
                        name = LocaleManager.getLocalizedVillage(village.name, village.nameHindi),
                        icon = Icons.Rounded.LocationOn,
                        onEdit = { editingVillage = village },
                        onDelete = { villageToDelete = village }
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }

    // Edit Dialog
    if (editingVillage != null) {
        var tempName by remember(editingVillage) { 
            mutableStateOf(if (isHindi) editingVillage!!.nameHindi.ifEmpty { editingVillage!!.name } else editingVillage!!.name) 
        }
        AlertDialog(
            onDismissRequest = { editingVillage = null },
            title = { Text(stringResource(R.string.edit_village_header)) },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    label = { Text(stringResource(R.string.village_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (tempName.isNotBlank()) {
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                // Update name in a way that repo can split or preserve correctly
                                val updated = if (isHindi) {
                                    editingVillage!!.copy(nameHindi = tempName, name = if (editingVillage!!.name.isBlank()) tempName else editingVillage!!.name)
                                } else {
                                    editingVillage!!.copy(name = tempName)
                                }
                                villageRepository.updateVillage(updated)
                            }
                            editingVillage = null
                        }
                    }
                }, enabled = tempName.isNotBlank()) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { editingVillage = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    // Delete Dialog
    if (villageToDelete != null) {
        AlertDialog(
            onDismissRequest = { villageToDelete = null },
            title = { Text(stringResource(R.string.delete_village_title)) },
            text = { Text(stringResource(R.string.delete_village_confirm, villageToDelete!!.name)) },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                villageRepository.deleteVillage(villageToDelete!!)
                            }
                            villageToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { villageToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
