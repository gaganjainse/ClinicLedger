package com.clinicledger.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.clinicledger.R
import com.clinicledger.data.models.Patient
import com.clinicledger.ui.analytics.viewmodel.AnalyticsViewModel
import com.clinicledger.ui.compose.components.*
import com.clinicledger.ui.util.DateTimeUtils
import com.clinicledger.ui.util.LocaleManager
import com.clinicledger.ui.util.LocaleManager.LocalIsHindi
import kotlinx.coroutines.launch

/**
 * Screen displaying various clinic statistics, dues, and performance metrics.
 * Supports deep-linking via [initialVillageId].
 */
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    initialVillageId: Long? = null,
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateToPatientDetail: (Long) -> Unit,
) {
    val isHindi = LocalIsHindi.current
    
    ClinicScaffold(
        title = if (isHindi) "क्लीनिक विश्लेषण" else "Clinic Analytics",
        onBack = onNavigateBack
    ) { paddingValues ->
        AnalyticsContent(
            viewModel = viewModel,
            initialVillageId = initialVillageId,
            onNavigateToPatientDetail = onNavigateToPatientDetail,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

/**
 * Core content of the Analytics screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsContent(
    viewModel: AnalyticsViewModel,
    initialVillageId: Long? = null,
    onNavigateToPatientDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val isHindi = LocalIsHindi.current
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var activePatientListTitle by remember { mutableStateOf<String?>(null) }
    var activePatientList by remember { mutableStateOf<List<Patient>?>(null) }

    LaunchedEffect(Unit) {
        viewModel.refreshAnalytics()
    }

    // Handle Deep Link Filtering
    LaunchedEffect(initialVillageId, uiState.villageDuesList) {
        if (initialVillageId != null && uiState.villageDuesList.isNotEmpty()) {
            val village = uiState.villageDuesList.find { it.villageId == initialVillageId }
            if (village != null) {
                activePatientListTitle = context.getString(R.string.analytics_village_outstanding_title, 
                    if (isHindi) village.nameHindi.ifEmpty { village.name } else village.name)
                activePatientList = uiState.outstandingPatientsList.filter { it.villageId == initialVillageId }
            }
        }
    }

    // Scroll to top when data is refreshed
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            scrollState.animateScrollToItem(0)
        }
    }

    var activeTransactionListTitle by remember { mutableStateOf<String?>(null) }
    var activeTransactionList by remember { mutableStateOf<List<com.clinicledger.ui.analytics.viewmodel.TransactionWithPatient>?>(null) }

    activePatientList?.let { patients ->
        PatientListDialog(
            title = activePatientListTitle ?: "Details",
            patients = patients,
            isHindi = isHindi,
            onDismiss = { activePatientList = null }
        ) { patientId ->
            activePatientList = null
            onNavigateToPatientDetail(patientId)
        }
    }

    activeTransactionList?.let { transactions ->
        TransactionListDialog(
            title = activeTransactionListTitle ?: "Transactions",
            transactions = transactions,
            isHindi = isHindi,
            onDismiss = { activeTransactionList = null },
        ) { patientId ->
            activeTransactionList = null
            onNavigateToPatientDetail(patientId)
        }
    }

    if (uiState.isLoading) {
        ClinicLoadingState()
    } else {
        LazyColumn(
            state = scrollState,
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item(key = "cards_section", contentType = "summary") {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AnalyticsCard(
                            title = if (isHindi) "कुल बकाया" else "Total Outstanding",
                            value = LocaleManager.formatCurrency(uiState.totalOutstandingDues),
                            icon = Icons.Rounded.AccountBalanceWallet,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                activePatientListTitle = context.getString(R.string.analytics_total_outstanding_patients)
                                activePatientList = uiState.outstandingPatientsList
                            }
                        )

                        AnalyticsCard(
                            title = if (isHindi) "आज का संग्रह" else "Collected Today",
                            value = LocaleManager.formatCurrency(uiState.todayCollected),
                            icon = Icons.Rounded.Payments,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                activeTransactionListTitle = context.getString(R.string.analytics_collected_today)
                                activeTransactionList = uiState.todayCollectedList
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AnalyticsCard(
                            title = if (isHindi) "इस महीने का संग्रह" else "Total Monthly Collection",
                            value = LocaleManager.formatCurrency(uiState.thisMonthCollected),
                            icon = Icons.Rounded.CalendarMonth,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.weight(1f),
                            onClick = {
                                activeTransactionListTitle = context.getString(R.string.analytics_monthly_collection)
                                scope.launch {
                                    val monthStart = DateTimeUtils.getStartOfMonth()
                                    val monthTxs = viewModel.getTransactionsSince(monthStart).filter { it.type == "payment" }
                                    activeTransactionList = monthTxs
                                }
                            }
                        )

                        val recoveryRateText = remember(uiState.recoveryRate) {
                            String.format(java.util.Locale.US, "%.1f%%", uiState.recoveryRate)
                        }
                        AnalyticsCard(
                            title = if (isHindi) "वसूली दर" else "Recovery Rate (Month)",
                            value = recoveryRateText,
                            icon = Icons.AutoMirrored.Rounded.TrendingUp,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                activePatientListTitle = context.getString(R.string.analytics_all_debtors)
                                activePatientList = uiState.outstandingPatientsList
                            }
                        )
                    }
                }
            }

            item(key = "village_breakdown_title") {
                Text(
                    text = if (isHindi) "गाँव अनुसार बकाया विवरण" else "Dues Breakdown by Village",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            if (uiState.villageDuesList.isEmpty()) {
                item(key = "empty_villages") {
                    ClinicEmptyState(
                        message = "No outstanding dues recorded.",
                        messageHindi = "कोई बकाया उपलब्ध नहीं है।"
                    )
                }
            } else {
                item(key = "village_breakdown_card", contentType = "village_card") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            val maxDues = uiState.villageDuesList.maxOfOrNull { it.totalDues } ?: 1.0
                            uiState.villageDuesList.forEach { item ->
                                val progress = (item.totalDues / maxDues).toFloat().coerceIn(0f, 1f)
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val vName = if (isHindi) item.nameHindi.ifEmpty { item.name } else item.name
                                            activePatientListTitle = context.getString(R.string.analytics_village_outstanding_title, vName)
                                            activePatientList = uiState.outstandingPatientsList.filter { it.villageId == item.villageId }
                                        }
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isHindi) item.nameHindi.ifEmpty { item.name } else item.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = LocaleManager.formatCurrency(item.totalDues),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isHindi) "${item.patientCount} मरीज" else "${item.patientCount} Patients",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        val percentageText = remember(item.totalDues, uiState.totalOutstandingDues) {
                                            String.format(java.util.Locale.US, "%.1f%%", if (uiState.totalOutstandingDues > 0) (item.totalDues / uiState.totalOutstandingDues) * 100.0 else 0.0)
                                        }
                                        Text(
                                            text = percentageText,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    val barColor = when {
                                        progress > 0.7f -> MaterialTheme.colorScheme.error
                                        progress > 0.4f -> Color(0xFFFFA000) // Orange
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = barColor,
                                        trackColor = barColor.copy(alpha = 0.15f)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }
            }

            item(key = "aging_title") {
                Text(
                    text = if (isHindi) "बकाया समय सीमा (निष्क्रिय खाते)" else "Overdue Dues Aging (Inactive Accounts)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item(key = "aging_row") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DefaulterCard(
                        label = if (isHindi) "> 30 दिन" else "> 30 Days",
                        count = uiState.defaulters30Days,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            activePatientListTitle = context.getString(R.string.analytics_overdue_30)
                            activePatientList = uiState.defaulters30List
                        }
                    )
                    DefaulterCard(
                        label = if (isHindi) "> 90 दिन" else "> 90 Days",
                        count = uiState.defaulters90Days,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            activePatientListTitle = context.getString(R.string.analytics_overdue_90)
                            activePatientList = uiState.defaulters90List
                        }
                    )
                    DefaulterCard(
                        label = if (isHindi) "> 180 दिन" else "> 180 Days",
                        count = uiState.defaulters180Days,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            activePatientListTitle = context.getString(R.string.analytics_overdue_180)
                            activePatientList = uiState.defaulters180List
                        }
                    )
                }
            }

            item(key = "top_debtors_title") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable {
                            activePatientListTitle = if (isHindi) "कुल बकायादार मरीज" else "Total Outstanding Patients"
                            activePatientList = uiState.outstandingPatientsList
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isHindi) "शीर्ष ५ बकायादार मरीज" else "Top 5 Outstanding Debtors",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = if (isHindi) "सभी देखें" else "View All",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            activePatientListTitle = context.getString(R.string.analytics_total_outstanding_patients)
                            activePatientList = uiState.outstandingPatientsList
                        }
                    )
                }
            }

            if (uiState.topPatientsWithDues.isEmpty()) {
                item(key = "empty_debtors") {
                    ClinicEmptyState(
                        message = "No patients with outstanding balance found.",
                        messageHindi = "कोई भी बकाया मरीज नहीं मिला।"
                    )
                }
            } else {
                itemsIndexed(
                    items = uiState.topPatientsWithDues.take(5),
                    key = { _, p -> "debtor_${p.id}" },
                    contentType = { _, _ -> "patient" }
                ) { index, patient ->
                    PatientListItem(
                        patient = patient,
                        index = index,
                        isHindi = isHindi,
                        onClick = { onNavigateToPatientDetail(patient.id) }
                    )
                }
            }

            item(key = "inactive_title") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable {
                            activePatientListTitle = if (isHindi) "६०+ दिनों से नहीं आए मरीज" else "Patients Not Visited in 60+ Days"
                            activePatientList = uiState.inactivePatients60Days
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isHindi) "६०+ दिनों से नहीं आए मरीज (Chronic Follow-up)" else "Patients Not Visited in 60+ Days",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = if (isHindi) "सभी देखें" else "View All",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (uiState.inactivePatients60Days.isEmpty()) {
                item(key = "empty_inactive") {
                    ClinicEmptyState(
                        message = "All patients have visited recently.",
                        messageHindi = "कोई भी निष्क्रिय मरीज नहीं मिला।"
                    )
                }
            } else {
                itemsIndexed(
                    items = uiState.inactivePatients60Days,
                    key = { _, p -> "inactive_${p.id}" },
                    contentType = { _, _ -> "patient" }
                ) { index, patient ->
                    PatientListItem(
                        patient = patient,
                        index = index,
                        isHindi = isHindi,
                        onClick = { onNavigateToPatientDetail(patient.id) }
                    )
                }
            }
        }
    }
}
