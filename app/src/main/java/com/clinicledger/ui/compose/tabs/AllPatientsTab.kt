package com.clinicledger.ui.compose.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.clinicledger.R
import com.clinicledger.data.models.Patient
import com.clinicledger.ui.compose.components.ClinicEmptyState
import com.clinicledger.ui.compose.components.PatientListItem

/**
 * Tab displaying a sortable and searchable list of all clinic patients.
 */
@Suppress("HardcodedStringLiteral")
@Composable
fun AllPatientsTab(
    /** Full patient database */
    allPatients: List<Patient>,
    /** Navigation callback */
    onNavigateToDetail: (Long) -> Unit,
    /** Current locale state */
    isHindi: Boolean,
) {
    var query by remember { mutableStateOf(value = "") }
    var sortOrder by remember { mutableStateOf(value = "name") }
    var showSortMenu by remember { mutableStateOf(value = false) }

    val filteredList = remember(query, allPatients, sortOrder) {
        val list = allPatients.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.phone.contains(query, ignoreCase = true)
        }
        when (sortOrder) {
            "dues" -> list.sortedByDescending { it.currentBalance }
            "visited" -> list.sortedByDescending { it.updatedAt }
            else -> list.sortedBy { it.name }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.search_patients_placeholder)) },
                leadingIcon = { Icon(Icons.Rounded.Search, null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Box {
                IconButton(onClick = { showSortMenu = true }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Sort, 
                        contentDescription = stringResource(R.string.sort_by_label),
                    )
                }
                
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false },
                ) {
                    val sortOptions = listOf(
                        "name" to stringResource(R.string.sort_name),
                        "dues" to stringResource(R.string.sort_dues),
                        "visited" to stringResource(R.string.sort_visited),
                    )
                    
                    sortOptions.forEach { (id, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                sortOrder = id
                                showSortMenu = false
                            },
                            trailingIcon = {
                                if (sortOrder == id) {
                                    Icon(Icons.Rounded.Check, null)
                                } else {
                                    Spacer(Modifier.size(24.dp))
                                }
                            },
                        )
                    }
                }
            }
        }

        // List
        if (filteredList.isEmpty()) {
            ClinicEmptyState(
                message = stringResource(R.string.no_patients_found),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp),
            ) {
                items(
                    items = filteredList,
                    key = { it.id },
                    contentType = { "patient" },
                ) { /** patient */ patient ->
                    PatientListItem(patient = patient, isHindi = isHindi) {
                        onNavigateToDetail(patient.id)
                    }
                }
            }
        }
    }
}
