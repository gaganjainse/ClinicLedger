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
import com.clinicledger.data.models.FamilyGroup
import com.clinicledger.data.models.Village
import com.clinicledger.data.repository.PatientRepository
import com.clinicledger.data.repository.VillageRepository
import com.clinicledger.ui.compose.components.*
import com.clinicledger.ui.util.LocaleManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Village Management Content.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VillageManagementContent(/** Optional UI customizer */ modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val villageRepository = remember { VillageRepository(context) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        VillageSection(villageRepository, scope)
    }
}

/**
 * Subsection for managing individual village records.
 */
@Composable
private fun VillageSection(
    /** Village data access */
    villageRepository: VillageRepository,
    /** Lifecycle scope for DB operations */
    scope: kotlinx.coroutines.CoroutineScope,
) {
    val villages by villageRepository.getAllVillages().observeAsState(emptyList())
    var inputNameEng by remember { mutableStateOf(value = "") }
    var inputNameHindi by remember { mutableStateOf(value = "") }
    var editingVillage by remember { mutableStateOf<Village?>(value = null) }
    var villageToDelete by remember { mutableStateOf<Village?>(value = null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Quick Add Bar with equal size boxes
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 2.dp,
            shadowElevation = 1.dp,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(IntrinsicSize.Min),
                ) {
                    OutlinedTextField(
                        value = inputNameEng,
                        onValueChange = { 
                            inputNameEng = it
                            if (inputNameHindi.isBlank()) {
                                inputNameHindi = LocaleManager.transliterate(it)
                            }
                        },
                        placeholder = { 
                            Text(stringResource(R.string.village_name_eng), maxLines = 1) 
                        },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                    )
                    OutlinedTextField(
                        value = inputNameHindi,
                        onValueChange = { inputNameHindi = it },
                        placeholder = { 
                            Text(stringResource(R.string.village_name_hindi), maxLines = 1) 
                        },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                    )
                    Button(
                        onClick = {
                            if (inputNameEng.isNotBlank() || inputNameHindi.isNotBlank()) {
                                scope.launch {
                                    withContext(Dispatchers.IO) {
                                        villageRepository.insertVillage(
                                            Village(
                                                name = inputNameEng.trim().ifEmpty { 
                                                    inputNameHindi.trim() 
                                                },
                                                nameHindi = inputNameHindi.trim(),
                                            ),
                                        )
                                    }
                                    inputNameEng = ""
                                    inputNameHindi = ""
                                }
                            }
                        },
                        enabled = inputNameEng.isNotBlank() || inputNameHindi.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxHeight(),
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (villages.isEmpty()) {
                item { 
                    ClinicEmptyState(message = stringResource(R.string.no_villages_msg)) 
                }
            } else {
                items(
                    items = villages, 
                    key = { it.id },
                    contentType = { "village" },
                ) { village ->
                    EntityCard(
                        name = LocaleManager.getLocalizedVillage(village.name, village.nameHindi),
                        icon = Icons.Rounded.LocationOn,
                        onEdit = { editingVillage = village },
                        onDelete = { villageToDelete = village },
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }

    // Edit Dialog
    editingVillage?.let { village ->
        var tempNameEng by remember(village) { mutableStateOf(value = village.name) }
        var tempNameHindi by remember(village) { mutableStateOf(value = village.nameHindi) }

        AlertDialog(
            onDismissRequest = { editingVillage = null },
            title = { Text(stringResource(R.string.edit_village_header)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = tempNameEng,
                        onValueChange = { tempNameEng = it },
                        label = { Text(stringResource(R.string.village_name_eng)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = tempNameHindi,
                        onValueChange = { tempNameHindi = it },
                        label = { Text(stringResource(R.string.village_name_hindi)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempNameEng.isNotBlank() || tempNameHindi.isNotBlank()) {
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    val updated = village.copy(
                                        name = tempNameEng.trim().ifEmpty { tempNameHindi.trim() },
                                        nameHindi = tempNameHindi.trim(),
                                    )
                                    villageRepository.updateVillage(updated)
                                }
                                editingVillage = null
                            }
                        }
                    },
                    enabled = tempNameEng.isNotBlank() || tempNameHindi.isNotBlank(),
                ) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { editingVillage = null }) { 
                    Text(stringResource(R.string.cancel)) 
                }
            },
        )
    }

    // Delete Dialog
    villageToDelete?.let { village ->
        AlertDialog(
            onDismissRequest = { villageToDelete = null },
            title = { Text(stringResource(R.string.delete_village_title)) },
            text = { Text(stringResource(R.string.delete_village_confirm, village.name)) },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                villageRepository.deleteVillage(village)
                            }
                            villageToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { villageToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}
