package com.clinicledger.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
 * Hub for clinical records, search, and the morning briefing.
 * Renamed from ClinicalDashboard as per industry standards.
 */
@Composable
fun LedgerDashboardScreen(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    recentPatients: List<Patient>,
    searchResults: List<Patient>,
    totalCollectedToday: Double,
    allPatients: List<Patient>,
    familyGroups: List<FamilyGroup>,
    onNavigateToDetail: (Long) -> Unit,
    isHindi: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
        ) {
            // Morning Briefing Card
            item(key = "briefing") {
                Box(modifier = Modifier.padding(16.dp)) {
                    MorningBriefSection(
                        allPatients = allPatients,
                        familyGroups = familyGroups,
                        totalCollectedToday = totalCollectedToday
                    )
                }
            }

            // High-Performance Search Bar
            item(key = "search_bar") {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = onSearchChange,
                    isHindi = isHindi
                )
            }

            // Section: Results
            if (searchQuery.isEmpty()) {
                item(key = "recent_records_title") {
                    SectionTitle(if (isHindi) "हाल के रिकॉर्ड" else "Recent Records")
                }
                items(
                    items = recentPatients,
                    key = { it.id },
                    contentType = { "patient" }
                ) { patient ->
                    // High-Performance Item with ripple optimization
                    PatientListItem(patient = patient, isHindi = isHindi) {
                        onNavigateToDetail(patient.id)
                    }
                }
            } else {
                if (searchResults.isEmpty()) {
                    item(key = "empty_search") {
                        ClinicEmptyState(message = "No clinical records found", messageHindi = "कोई रिकॉर्ड नहीं मिला")
                    }
                } else {
                    item(key = "search_results_title") {
                        SectionTitle(if (isHindi) "खोज परिणाम" else "Search Results")
                    }
                    items(
                        items = searchResults,
                        key = { it.id },
                        contentType = { "patient" }
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

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit, isHindi: Boolean) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text(if (isHindi) "खोजें..." else "Quick Search...") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Rounded.Close, contentDescription = null)
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
    )
}
