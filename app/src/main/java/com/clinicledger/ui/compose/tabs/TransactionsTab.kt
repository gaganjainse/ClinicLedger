package com.clinicledger.ui.compose.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.clinicledger.R
import com.clinicledger.data.models.Patient
import com.clinicledger.data.models.Transaction
import com.clinicledger.data.models.Village
import com.clinicledger.ui.compose.components.ClinicEmptyState
import com.clinicledger.ui.compose.components.ClinicTransactionItem
import com.clinicledger.ui.compose.components.UnifiedSearchFilterBar

/**
 * Tab displaying a chronological log of all clinical transactions.
 */
@Suppress("HardcodedStringLiteral")
@Composable
fun TransactionsTab(
    /** Search filter text */
    query: String,
    /** Callback for filter changes */
    onQueryChange: (String) -> Unit,
    /** List of all system transactions */
    transactions: List<Transaction>,
    /** Full patient database for name resolution */
    allPatients: List<Patient>,
    /** Registered villages */
    villages: List<Village>,
    /** Navigation callback */
    onNavigateToDetail: (Long) -> Unit,
    /** Current language state */
    isHindi: Boolean,
) {
    var selectedVillageId by remember { mutableStateOf<Long?>(null) }
    var sortOrder by remember { mutableStateOf("date") }

    val sortOptions = listOf(
        "date" to R.string.sort_visited,
        "amount" to R.string.analytics_collected_today
    )

    val filteredTransactions = remember(query, transactions, allPatients, selectedVillageId, sortOrder) {
        val patientMap = allPatients.associateBy { it.id }
        transactions.filter { /** tx */ tx ->
            val p = patientMap[tx.patientId]
            val pName = p?.name ?: ""
            val matchesQuery = pName.contains(query, ignoreCase = true) || 
                tx.notes.contains(query, ignoreCase = true) || 
                tx.type.contains(query, ignoreCase = true)
            val matchesVillage = selectedVillageId == null || p?.villageId == selectedVillageId
            matchesQuery && matchesVillage
        }.let { list ->
            when (sortOrder) {
                "amount" -> list.sortedByDescending { it.amount }
                else -> list.sortedByDescending { it.createdAt }
            }
        }
    }

    val patientMap = remember(allPatients) { allPatients.associateBy { it.id } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        UnifiedSearchFilterBar(
            query = query,
            onQueryChange = onQueryChange,
            selectedVillageId = selectedVillageId,
            onVillageChange = { selectedVillageId = it },
            villages = villages,
            sortOrder = sortOrder,
            onSortChange = { sortOrder = it },
            sortOptions = sortOptions
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (filteredTransactions.isEmpty()) {
                item {
                    ClinicEmptyState(
                        message = stringResource(R.string.no_transactions),
                    )
                }
            } else {
                items(
                    items = filteredTransactions, 
                    key = { it.id },
                    contentType = { "transaction" },
                ) { /** tx */ tx ->
                    val p = patientMap[tx.patientId]
                    ClinicTransactionItem(
                        transaction = tx,
                        patientName = p?.name ?: stringResource(R.string.no_patients_found),
                        isHindi = isHindi,
                    ) {
                        onNavigateToDetail(tx.patientId)
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}
