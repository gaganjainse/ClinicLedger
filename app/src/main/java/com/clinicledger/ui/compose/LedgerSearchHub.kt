package com.clinicledger.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.clinicledger.R
import com.clinicledger.data.models.FamilyGroup
import com.clinicledger.data.models.Patient
import com.clinicledger.ui.compose.components.ClinicEmptyState
import com.clinicledger.ui.compose.components.MorningBriefSection
import com.clinicledger.ui.compose.components.PatientListItem

/**
 * Centrally coordinated search hub for the clinic ledger.
 */
@Composable
fun LedgerSearchHub(
    /** query text */
    searchQuery: String,
    /** callback */
    onSearchChange: (String) -> Unit,
    /** active set */
    recentPatients: List<Patient>,
    /** search set */
    searchResults: List<Patient>,
    /** stats */
    totalCollectedToday: Long,
    /** data set */
    allPatients: List<Patient>,
    /** data set */
    familyGroups: List<FamilyGroup>,
    /** callback */
    onNavigateToDetail: (Long) -> Unit,
    /** locale flag */
    isHindi: Boolean,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        ) {
            // Summary Card
            item(key = "briefing") {
                MorningBriefSection(
                    allPatients = allPatients,
                    familyGroups = familyGroups,
                    totalCollectedToday = totalCollectedToday,
                )
            }

            // Input Field
            item(key = "search_input") {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { 
                        Text(stringResource(R.string.search_name_phone_hint)) 
                    },
                    leadingIcon = { Icon(Icons.Rounded.Search, null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(
                                    imageVector = Icons.Rounded.Clear, 
                                    contentDescription = stringResource(R.string.delete),
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                )
            }

            // Section: Results
            if (searchQuery.isEmpty()) {
                item(key = "recent_title") {
                    Text(
                        text = stringResource(R.string.recent_patients_label),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
                items(
                    items = recentPatients,
                    key = { "recent_${it.id}" },
                    contentType = { "patient" },
                ) { patient ->
                    PatientListItem(patient = patient, isHindi = isHindi) { 
                        onNavigateToDetail(patient.id) 
                    }
                }
            } else {
                if (searchResults.isEmpty()) {
                    item(key = "empty_results") {
                        ClinicEmptyState(
                            message = stringResource(R.string.no_patients_found_msg),
                        )
                    }
                } else {
                    items(
                        items = searchResults,
                        key = { "search_${it.id}" },
                        contentType = { "patient" },
                    ) { patient ->
                        PatientListItem(patient = patient, isHindi = isHindi) { 
                            onNavigateToDetail(patient.id) 
                        }
                    }
                }
            }
        }
    }
}
