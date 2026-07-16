package com.clinicledger.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.clinicledger.data.models.FamilyGroup
import com.clinicledger.data.models.Patient
import com.clinicledger.ui.compose.components.ClinicEmptyState
import com.clinicledger.ui.compose.components.MorningBriefSection
import com.clinicledger.ui.compose.components.PatientListItem

/**
 * High-Performance Ledger Search Hub.
 * Optimized for 120FPS with keys and content types.
 */
@Composable
fun LedgerSearchHub(
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
    // Memoize the emptiness check to prevent redundant list scans
    val isSearchEmpty by remember(searchQuery) { derivedStateOf { searchQuery.isEmpty() } }
    val resultsFound by remember(searchResults) { derivedStateOf { searchResults.isNotEmpty() } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "briefing") {
            MorningBriefSection(
                allPatients = allPatients,
                familyGroups = familyGroups,
                totalCollectedToday = totalCollectedToday
            )
        }

        item(key = "search_input") {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(if (isHindi) "मरीज का नाम या मोबाइल..." else "Search name or phone...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = {
                    if (!isSearchEmpty) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(28.dp),
                singleLine = true
            )
        }

        if (isSearchEmpty) {
            item(key = "recent_title") {
                Text(
                    text = if (isHindi) "हाल के मरीज" else "Recent Patients",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            items(
                items = recentPatients,
                key = { "recent_${it.id}" },
                contentType = { "patient" }
            ) { patient ->
                PatientListItem(patient = patient, isHindi = isHindi) { onNavigateToDetail(patient.id) }
            }
        } else {
            if (!resultsFound) {
                item(key = "empty_results") {
                    ClinicEmptyState(message = "No patients found", messageHindi = "कोई मरीज नहीं मिला")
                }
            } else {
                items(
                    items = searchResults,
                    key = { "search_${it.id}" },
                    contentType = { "patient" }
                ) { patient ->
                    PatientListItem(patient = patient, isHindi = isHindi) { onNavigateToDetail(patient.id) }
                }
            }
        }
        
        item(key = "footer_spacer") { Spacer(modifier = Modifier.height(80.dp)) }
    }
}
