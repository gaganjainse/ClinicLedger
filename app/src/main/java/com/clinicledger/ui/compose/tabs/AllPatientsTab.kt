package com.clinicledger.ui.compose.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.clinicledger.data.models.Patient
import com.clinicledger.ui.compose.components.PatientListItem

/**
 * Tab displaying a searchable list of all patients in the ledger.
 */
@Composable
fun AllPatientsTab(
    allPatients: List<Patient>,
    onNavigateToDetail: (Long) -> Unit,
    isHindi: Boolean
) {
    var query by remember { mutableStateOf("") }
    
    // Derived state to filter patients based on the local query
    val filtered = remember(allPatients, query) {
        allPatients.filter { 
            it.name.contains(query, ignoreCase = true) || 
            (it.phone ?: "").contains(query) 
        }
    }
    val scrollState = rememberLazyListState()

    // Automatic scroll-to-top whenever the search query is modified
    LaunchedEffect(query) {
        scrollState.animateScrollToItem(0)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            placeholder = { Text(if (isHindi) "सभी मरीजों में खोजें..." else "Search all patients...") },
            leadingIcon = { Icon(Icons.Rounded.People, contentDescription = null) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        LazyColumn(
            state = scrollState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            items(
                items = filtered, 
                key = { it.id },
                contentType = { "patient" }
            ) { patient ->
                PatientListItem(
                    patient = patient, 
                    isHindi = isHindi, 
                    onClick = { onNavigateToDetail(patient.id) }
                )
            }
        }
    }
}
