package com.clinicledger.ui.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.clinicledger.R
import com.clinicledger.data.models.Patient
import com.clinicledger.ui.analytics.viewmodel.TransactionWithPatient
import com.clinicledger.ui.util.LocaleManager

/**
 * Full-screen dialog displaying a filtered list of patients from analytics.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientListDialog(
    /** Title text */
    title: String,
    /** Data set */
    patients: List<Patient>,
    /** Locale */
    isHindi: Boolean,
    /** Callback */
    onDismiss: () -> Unit,
    /** callback */
    onNavigateToPatient: (Long) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredPatients = remember(searchQuery, patients) {
        patients.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                it.phone.contains(searchQuery, ignoreCase = true) ||
                (it.village?.name ?: "").contains(searchQuery, ignoreCase = true)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = title, 
                                fontWeight = FontWeight.Bold, 
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = stringResource(R.string.patients_count, patients.size),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Filled.Close, 
                                contentDescription = stringResource(R.string.cancel),
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text(stringResource(R.string.search_hint)) },
                    leadingIcon = { Icon(Icons.Rounded.Search, null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
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

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                ) {
                    itemsIndexed(
                        items = filteredPatients,
                        key = { /** ignore */ _, p -> "dialog_p_${p.id}" },
                    ) { index, patient ->
                        PatientListItem(
                            patient = patient,
                            index = index,
                            isHindi = isHindi,
                        ) {
                            onNavigateToPatient(patient.id)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Full-screen dialog displaying a filtered list of transactions from analytics.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListDialog(
    /** title */
    title: String,
    /** data */
    transactions: List<TransactionWithPatient>,
    /** locale */
    isHindi: Boolean,
    /** callback */
    onDismiss: () -> Unit,
    /** callback */
    onNavigateToPatient: (Long) -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                val totalAmount = transactions.sumOf { it.amount }
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = title, 
                                fontWeight = FontWeight.Bold, 
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = stringResource(
                                    R.string.total_count_format, 
                                    LocaleManager.formatCurrency(totalAmount), 
                                    transactions.size,
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Filled.Close, 
                                contentDescription = stringResource(R.string.cancel),
                            )
                        }
                    },
                )

                if (transactions.isEmpty()) {
                    ClinicEmptyState(
                        message = stringResource(R.string.no_transactions),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(
                            items = transactions,
                            key = { it.id },
                        ) { item ->
                            ClinicTransactionItem(
                                transaction = com.clinicledger.data.models.Transaction(
                                    id = item.id,
                                    patientId = item.patientId,
                                    type = item.type,
                                    amount = item.amount,
                                    notes = item.notes,
                                    createdAt = item.createdAt,
                                ),
                                patientName = item.patientName,
                                isHindi = isHindi,
                            ) {
                                onNavigateToPatient(item.patientId)
                            }
                        }
                    }
                }
            }
        }
    }
}
