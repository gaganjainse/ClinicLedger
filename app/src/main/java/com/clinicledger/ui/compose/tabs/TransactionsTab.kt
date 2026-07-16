package com.clinicledger.ui.compose.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.clinicledger.data.models.Patient
import com.clinicledger.data.models.Transaction
import com.clinicledger.ui.compose.components.ClinicTransactionItem

/**
 * Tab displaying a searchable chronological list of all transactions across all patients.
 */
@Composable
fun TransactionsTab(
    query: String,
    onQueryChange: (String) -> Unit,
    transactions: List<Transaction>,
    allPatients: List<Patient>,
    onNavigateToDetail: (Long) -> Unit,
    isHindi: Boolean
) {
    val patientMap = remember(allPatients) { allPatients.associateBy { it.id } }
    
    // Filtered transaction list based on search term
    val filtered = remember(transactions, query) {
        transactions.filter { tx ->
            val pName = patientMap[tx.patientId]?.name ?: ""
            pName.contains(query, ignoreCase = true) || tx.notes.contains(query, ignoreCase = true)
        }
    }
    val scrollState = rememberLazyListState()

    // Ensure the view resets to top when search query changes
    LaunchedEffect(query) {
        scrollState.animateScrollToItem(0)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            placeholder = { Text(if (isHindi) "लेनदेन खोजें..." else "Search transactions...") },
            leadingIcon = { Icon(Icons.AutoMirrored.Rounded.ReceiptLong, contentDescription = null) },
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
                contentType = { "transaction" }
            ) { tx ->
                ClinicTransactionItem(
                    transaction = tx,
                    patientName = patientMap[tx.patientId]?.name ?: "Unknown",
                    onClick = { onNavigateToDetail(tx.patientId) },
                    isHindi = isHindi
                )
            }
        }
    }
}
