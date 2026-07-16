package com.clinicledger.ui.compose

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.clinicledger.data.models.FamilyGroup
import com.clinicledger.data.models.Patient
import com.clinicledger.ui.compose.components.ClinicEmptyState
import com.clinicledger.ui.compose.components.MorningBriefSection
import com.clinicledger.ui.compose.components.PatientListItem

/**
 * The default "Ledger" tab content.
 * Displays a morning summary, search bar, and recent patients.
 */
@Composable
fun LedgerTab(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    recentPatients: List<Patient>,
    searchResults: List<Patient>,
    totalCollectedToday: Double,
    allPatients: List<Patient>,
    familyGroups: List<FamilyGroup>,
    onNavigateToDetail: (Long) -> Unit,
    isHindi: Boolean,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            MorningBriefSection(
                allPatients = allPatients,
                familyGroups = familyGroups,
                totalCollectedToday = totalCollectedToday
            )
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(if (isHindi) "मरीज का नाम या मोबाइल..." else "Search name or phone...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(28.dp),
                singleLine = true
            )
        }

        if (searchQuery.isEmpty()) {
            item {
                Text(
                    text = if (isHindi) "हाल के मरीज" else "Recent Patients",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            items(recentPatients) { patient ->
                PatientListItem(patient = patient, isHindi = isHindi) { onNavigateToDetail(patient.id) }
            }
        } else {
            if (searchResults.isEmpty()) {
                item {
                    ClinicEmptyState(message = "No patients found", messageHindi = "कोई मरीज नहीं मिला")
                }
            } else {
                items(searchResults) { patient ->
                    PatientListItem(patient = patient, isHindi = isHindi) { onNavigateToDetail(patient.id) }
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}
