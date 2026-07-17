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
import androidx.compose.ui.res.stringResource
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
    /** Data provider for financial analytics */
    viewModel: AnalyticsViewModel,
    /** Callback for back navigation */
    onNavigateBack: () -> Unit,
    /** Navigation callback for specific patient records */
    onNavigateToPatientDetail: (Long) -> Unit,
    /** Optional UI customizer */
    modifier: Modifier = Modifier,
    /** Optional village ID to filter the initial view */
    initialVillageId: Long? = null,
) {
    val isHindi = LocalIsHindi.current
    
    ClinicScaffold(
        title = if (isHindi) {
            stringResource(R.string.financial_analytics_title)
        } else {
            stringResource(R.string.nav_analytics)
        },
        onBack = onNavigateBack,
    ) { paddingValues ->
        AnalyticsContent(
            viewModel = viewModel,
            onNavigateToPatientDetail = onNavigateToPatientDetail,
            modifier = modifier.padding(paddingValues),
            initialVillageId = initialVillageId,
        )
    }
}

/**
 * Core content of the Analytics screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsContent(
    /** The analytics view model */
    viewModel: AnalyticsViewModel,
    /** Navigation callback */
    onNavigateToPatientDetail: (Long) -> Unit,
    /** Optional UI customizer */
    modifier: Modifier = Modifier,
    /** Optional village ID filter */
    initialVillageId: Long? = null,
) {
    val isHindi = LocalIsHindi.current
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var activePatientListTitleRes by remember { mutableStateOf<Int?>(null) }
    var activePatientListTitleArgs by remember { mutableStateOf<List<Any>>(emptyList()) }
    var activePatientListTitleRaw by remember { mutableStateOf<String?>(null) }
    var activePatientList by remember { mutableStateOf<List<Patient>?>(null) }

    val resolvedPatientListTitle = when {
        activePatientListTitleRes != null -> {
            stringResource(activePatientListTitleRes!!, *activePatientListTitleArgs.toTypedArray())
        }
        else -> activePatientListTitleRaw ?: stringResource(R.string.patient_details_title)
    }

    // Handle Deep Link Filtering
    LaunchedEffect(initialVillageId, uiState.villageDuesList) {
        if ((initialVillageId != null) && uiState.villageDuesList.isNotEmpty()) {
            val village = uiState.villageDuesList.find { it.villageId == initialVillageId }
            if (village != null) {
                activePatientListTitleRes = R.string.analytics_village_outstanding_title
                activePatientListTitleArgs = listOf(
                    if (isHindi) village.nameHindi.ifEmpty { village.name } else village.name,
                )
                activePatientListTitleRaw = null
                activePatientList = uiState.outstandingPatientsList.filter { 
                    it.villageId == initialVillageId 
                }
            }
        }
    }

    // Scroll to top when data is refreshed
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            scrollState.animateScrollToItem(index = 0)
        }
    }

    var activeTransactionListTitleRes by remember { mutableStateOf<Int?>(null) }
    var activeTransactionListTitleArgs by remember { mutableStateOf<List<Any>>(emptyList()) }
    var activeTransactionListTitleRaw by remember { mutableStateOf<String?>(null) }
    var activeTransactionList by remember { 
        mutableStateOf<List<com.clinicledger.ui.analytics.viewmodel.TransactionWithPatient>?>(null) 
    }

    val resolvedTransactionListTitle = when {
        activeTransactionListTitleRes != null -> {
            stringResource(
                activeTransactionListTitleRes!!, 
                *activeTransactionListTitleArgs.toTypedArray(),
            )
        }
        else -> activeTransactionListTitleRaw ?: stringResource(R.string.journal_title)
    }

    activePatientList?.let { patients ->
        PatientListDialog(
            title = resolvedPatientListTitle,
            patients = patients,
            isHindi = isHindi,
            onDismiss = { activePatientList = null },
        ) { patientId ->
            activePatientList = null
            onNavigateToPatientDetail(patientId)
        }
    }

    activeTransactionList?.let { transactions ->
        TransactionListDialog(
            title = resolvedTransactionListTitle,
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
            contentPadding = PaddingValues(bottom = 100.dp),
        ) {
            item(key = "patient_count_summary") {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.People, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.primary, 
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = stringResource(
                                R.string.patient_total_count_format, 
                                uiState.totalPatients,
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            item(key = "cards_section", contentType = "summary") {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        AnalyticsCard(
                            title = stringResource(R.string.total_due_label),
                            value = LocaleManager.formatCurrency(uiState.totalOutstandingDues),
                            icon = Icons.Rounded.AccountBalanceWallet,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                activePatientListTitleRes = R.string.analytics_total_outstanding_patients
                                activePatientListTitleArgs = emptyList()
                                activePatientListTitleRaw = null
                                activePatientList = uiState.outstandingPatientsList
                            },
                        )

                        AnalyticsCard(
                            title = stringResource(R.string.collected_today_label),
                            value = LocaleManager.formatCurrency(uiState.todayCollected),
                            icon = Icons.Rounded.Payments,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                activeTransactionListTitleRes = R.string.analytics_collected_today
                                activeTransactionListTitleArgs = emptyList()
                                activeTransactionListTitleRaw = null
                                activeTransactionList = uiState.todayCollectedList
                            },
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        AnalyticsCard(
                            title = stringResource(R.string.analytics_monthly_collection),
                            value = LocaleManager.formatCurrency(uiState.thisMonthCollected),
                            icon = Icons.Rounded.CalendarMonth,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.weight(1f),
                        ) {
                            activeTransactionListTitleRes = R.string.analytics_monthly_collection
                            activeTransactionListTitleArgs = emptyList()
                            activeTransactionListTitleRaw = null
                            scope.launch {
                                val monthStart = DateTimeUtils.getStartOfMonth()
                                val monthTxs = viewModel.getTransactionsSince(monthStart).filter { 
                                    it.type == "payment" 
                                }
                                activeTransactionList = monthTxs
                            }
                        }

                        val recoveryRateText = remember(uiState.recoveryRate) {
                            String.format(java.util.Locale.US, "%.1f%%", uiState.recoveryRate)
                        }
                        AnalyticsCard(
                            title = stringResource(R.string.monthly_ratio),
                            value = recoveryRateText,
                            icon = Icons.AutoMirrored.Rounded.TrendingUp,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                activePatientListTitleRes = R.string.analytics_all_debtors
                                activePatientListTitleArgs = emptyList()
                                activePatientListTitleRaw = null
                                activePatientList = uiState.outstandingPatientsList
                            },
                        )
                    }
                }
            }

            item(key = "village_breakdown_title") {
                Text(
                    text = stringResource(R.string.dues_breakdown_by_village),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            if (uiState.villageDuesList.isEmpty()) {
                item(key = "empty_villages") {
                    ClinicEmptyState(
                        message = stringResource(R.string.no_outstanding_balances),
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
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp), 
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            val maxDues = uiState.villageDuesList.maxOfOrNull { it.totalDues }?.toDouble() ?: 1.0
                            uiState.villageDuesList.forEach { item ->
                                val progress = (item.totalDues.toDouble() / maxDues).toFloat().coerceIn(0f, 1f)
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val vName = if (isHindi) {
                                                item.nameHindi.ifEmpty { item.name }
                                            } else {
                                                item.name
                                            }
                                            activePatientListTitleRes = R.string.analytics_village_outstanding_title
                                            activePatientListTitleArgs = listOf(vName)
                                            activePatientListTitleRaw = null
                                            activePatientList = uiState.outstandingPatientsList.filter { 
                                                it.villageId == item.villageId 
                                            }
                                        }
                                        .padding(vertical = 4.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = if (isHindi) {
                                                item.nameHindi.ifEmpty { item.name }
                                            } else {
                                                item.name
                                            },
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                        )
                                        Text(
                                            text = LocaleManager.formatCurrency(item.totalDues),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error,
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = stringResource(R.string.patients_count, item.patientCount),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        val percentageText = remember(item.totalDues, uiState.totalOutstandingDues) {
                                            val total = uiState.totalOutstandingDues
                                            val ratio = if (total > 0) (item.totalDues.toDouble() / total) * 100.0 else 0.0
                                            String.format(java.util.Locale.US, "%.1f%%", ratio)
                                        }
                                        Text(
                                            text = percentageText,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                        trackColor = barColor.copy(alpha = 0.15f),
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
                    text = stringResource(R.string.overdue_dues_period),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            item(key = "aging_row") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    DefaulterCard(
                        label = stringResource(R.string.more_than_30_days),
                        count = uiState.defaulters30Days,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            activePatientListTitleRes = R.string.analytics_overdue_30
                            activePatientListTitleArgs = emptyList()
                            activePatientListTitleRaw = null
                            activePatientList = uiState.defaulters30List
                        },
                    )
                    DefaulterCard(
                        label = stringResource(R.string.more_than_90_days),
                        count = uiState.defaulters90Days,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            activePatientListTitleRes = R.string.analytics_overdue_90
                            activePatientListTitleArgs = emptyList()
                            activePatientListTitleRaw = null
                            activePatientList = uiState.defaulters90List
                        },
                    )
                    DefaulterCard(
                        label = stringResource(R.string.more_than_180_days),
                        count = uiState.defaulters180Days,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            activePatientListTitleRes = R.string.analytics_overdue_180
                            activePatientListTitleArgs = emptyList()
                            activePatientListTitleRaw = null
                            activePatientList = uiState.defaulters180List
                        },
                    )
                }
            }

            item(key = "top_debtors_title") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable {
                            activePatientListTitleRes = R.string.analytics_all_debtors
                            activePatientListTitleArgs = emptyList()
                            activePatientListTitleRaw = null
                            activePatientList = uiState.outstandingPatientsList
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.top_dues_patients),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = stringResource(R.string.see_all),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            activePatientListTitleRes = R.string.analytics_total_outstanding_patients
                            activePatientListTitleArgs = emptyList()
                            activePatientListTitleRaw = null
                            activePatientList = uiState.outstandingPatientsList
                        },
                    )
                }
            }

            if (uiState.topPatientsWithDues.isEmpty()) {
                item(key = "empty_debtors") {
                    ClinicEmptyState(
                        message = stringResource(R.string.no_outstanding_balances),
                    )
                }
            } else {
                itemsIndexed(
                    items = uiState.topPatientsWithDues.take(n = 5),
                    key = { _, p -> "debtor_${p.id}" },
                    contentType = { _, _ -> "patient" },
                ) { index, patient ->
                    PatientListItem(
                        patient = patient,
                        index = index,
                        isHindi = isHindi,
                        onClick = { onNavigateToPatientDetail(patient.id) },
                    )
                }
            }

            item(key = "inactive_title") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable {
                            activePatientListTitleRes = R.string.analytics_non_visitors_60
                            activePatientListTitleArgs = emptyList()
                            activePatientListTitleRaw = null
                            activePatientList = uiState.inactivePatients60Days
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.analytics_non_visitors_60),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = stringResource(R.string.see_all),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            if (uiState.inactivePatients60Days.isEmpty()) {
                item(key = "empty_inactive") {
                    ClinicEmptyState(
                        message = stringResource(R.string.everyone_settled_msg),
                    )
                }
            } else {
                itemsIndexed(
                    items = uiState.inactivePatients60Days,
                    key = { _, p -> "inactive_${p.id}" },
                    contentType = { _, _ -> "patient" },
                ) { index, patient ->
                    PatientListItem(
                        patient = patient,
                        index = index,
                        isHindi = isHindi,
                        onClick = { onNavigateToPatientDetail(patient.id) },
                    )
                }
            }
        }
    }
}
