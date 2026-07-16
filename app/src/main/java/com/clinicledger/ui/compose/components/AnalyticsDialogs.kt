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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.clinicledger.data.models.Patient
import com.clinicledger.ui.analytics.viewmodel.TransactionWithPatient
import com.clinicledger.ui.util.LocaleManager

/**
 * Dialog displaying a searchable list of patients (e.g., all debtors or non-visitors).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientListDialog(
    title: String,
    patients: List<Patient>,
    isHindi: Boolean,
    onDismiss: () -> Unit,
    onNavigateToPatient: (Long) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredPatients = remember(searchQuery, patients) {
        if (searchQuery.isBlank()) {
            patients
        } else {
            patients.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                (it.village?.name ?: "").contains(searchQuery, ignoreCase = true) ||
                (it.village?.nameHindi ?: "").contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = if (isHindi) "कुल मरीज: ${patients.size}" else "Total Patients: ${patients.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "Close")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(if (isHindi) "मरीज खोजें..." else "Search patients...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(filteredPatients) { index, patient ->
                        PatientListItem(
                            patient = patient,
                            index = index,
                            isHindi = isHindi,
                            onClick = { onNavigateToPatient(patient.id) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dialog displaying a list of transactions (e.g., today's collections).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListDialog(
    title: String,
    transactions: List<TransactionWithPatient>,
    isHindi: Boolean,
    onDismiss: () -> Unit,
    onNavigateToPatient: (Long) -> Unit
) {
    val totalAmount = remember(transactions) { transactions.sumOf { it.amount } }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = if (isHindi) "कुल: ₹${LocaleManager.formatAmount(totalAmount)} (${transactions.size} लेनदेन)" else "Total: ₹${LocaleManager.formatAmount(totalAmount)} (${transactions.size} Tx)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close"
                            )

                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (transactions.isEmpty()) {
                        item {
                            ClinicEmptyState(
                                message = "No transactions found",
                                messageHindi = "कोई लेनदेन नहीं मिला"
                            )
                        }
                    } else {
                        items(transactions) { item ->
                            ClinicTransactionItem(
                                transaction = com.clinicledger.data.models.Transaction(
                                    patientId = item.patientId,
                                    type = item.type,
                                    amount = item.amount,
                                    notes = item.notes
                                ),
                                patientName = item.patientName,
                                villageName = item.villageName,
                                isHindi = isHindi,
                                onClick = { onNavigateToPatient(item.patientId) }
                            )
                        }
                    }
                }
            }
        }
    }
}
