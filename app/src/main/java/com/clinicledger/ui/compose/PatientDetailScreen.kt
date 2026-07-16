package com.clinicledger.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.clinicledger.R
import com.clinicledger.data.models.Patient
import com.clinicledger.ui.compose.components.*
import com.clinicledger.ui.patientdetail.viewmodel.PatientDetailViewModel
import com.clinicledger.ui.util.LocaleManager.LocalIsHindi
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailScreen(
    patientId: Long,
    viewModel: PatientDetailViewModel,
    voiceViewModel: VoiceAssistantViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToPatientDetail: (Long) -> Unit,
) {
    val isHindi = LocalIsHindi.current
    
    // Update Agentic Brain with context
    LaunchedEffect(patientId) {
        viewModel.loadPatient(patientId)
        voiceViewModel.updateBrain(patientId = patientId, screen = "DETAIL")
    }

    val patient by viewModel.patient.observeAsState()
    val transactions by viewModel.transactions.observeAsState(emptyList())
    val aliases by viewModel.aliases.observeAsState(emptyList())
    val familyMembers by viewModel.familyMembers.observeAsState(emptyList())

    var showAddMedicine by remember { mutableStateOf(false) }
    var showRecordPayment by remember { mutableStateOf(false) }
    var showAddAlias by remember { mutableStateOf(false) }
    var showFamilyTree by remember { mutableStateOf(false) }
    var showEditProfile by remember { mutableStateOf(false) }

    patient?.let { p ->
        Scaffold(
            topBar = {
                ClinicTopAppBar(
                    title = "Medical Record",
                    titleHindi = "मेडिकल रिकॉर्ड",
                    subtitle = if (p.phone.isNotBlank()) p.phone else "ID: ${p.id}",
                    navigationIcon = { BackNavigationIcon(onNavigateBack) },
                    actions = {
                        IconButton(onClick = { showEditProfile = true }) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Edit")
                        }
                    }
                )
            },
            floatingActionButton = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    FloatingActionButton(
                        onClick = { showAddMedicine = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Rounded.MedicalServices, contentDescription = "Add Medicine")
                    }
                    FloatingActionButton(
                        onClick = { showRecordPayment = true },
                        containerColor = Color(0xFF0D9488), // Medical Teal
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Rounded.Payments, contentDescription = "Record Payment")
                    }
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // 1. Patient Info Header
                item {
                    PatientDetailHeader(
                        patient = p,
                        villageName = p.village?.name ?: "Unknown",
                        aliases = aliases,
                        onAddAlias = { showAddAlias = true },
                        onDeleteAlias = { viewModel.deleteAlias(it) },
                        onShowFamilyTree = { showFamilyTree = true },
                        hasFamily = p.familyGroupId != null
                    )
                }

                // 2. Financial Summary
                item {
                    BalanceCard(balance = p.currentBalance)
                }

                // 3. Family / Relationships Section
                if (familyMembers.isNotEmpty()) {
                    item {
                        SectionHeader(if (isHindi) "पारिवारिक संबंध" else "Family Relationships")
                        FamilyQuickScroll(familyMembers.filter { it.id != p.id }, onNavigateToPatientDetail)
                    }
                }

                // 4. Clinical History (Timeline)
                item {
                    SectionHeader(if (isHindi) "इलाज का इतिहास" else "Clinical History")
                }
                
                if (transactions.isEmpty()) {
                    item {
                        ClinicEmptyState(message = "No clinical history found", messageHindi = "कोई इतिहास नहीं मिला")
                    }
                } else {
                    items(
                        items = transactions.sortedByDescending { it.createdAt },
                        key = { it.id }
                    ) { tx ->
                        TransactionTimelineItem(transaction = tx)
                    }
                }

                item { Spacer(modifier = Modifier.height(120.dp)) }
            }
        }

        // Dialogs
        if (showAddMedicine) {
            AddTransactionDialog(
                type = "medicine",
                isHindi = isHindi,
                onDismiss = { showAddMedicine = false },
                onConfirm = { amount, notes, date ->
                    viewModel.addTransaction(p.id, "medicine", amount, notes, date)
                    showAddMedicine = false
                }
            )
        }
        if (showRecordPayment) {
            AddTransactionDialog(
                type = "payment",
                isHindi = isHindi,
                onDismiss = { showRecordPayment = false },
                onConfirm = { amount, notes, date ->
                    viewModel.addTransaction(p.id, "payment", amount, notes, date)
                    showRecordPayment = false
                }
            )
        }
        if (showAddAlias) {
            AddAliasDialog(
                onDismiss = { showAddAlias = false },
                onConfirm = { alias ->
                    viewModel.addAlias(p.id, alias)
                    showAddAlias = false
                }
            )
        }
        if (showFamilyTree && p.familyGroupId != null) {
            FamilyTreeDialog(
                familyName = p.name,
                members = familyMembers,
                onDismiss = { showFamilyTree = false },
                onNavigateToPatient = { id ->
                    showFamilyTree = false
                    onNavigateToPatientDetail(id)
                }
            )
        }
        
        if (showEditProfile) {
            val villages by viewModel.villages.observeAsState(emptyList())
            AddPatientDialog(
                villages = villages,
                onDismiss = { showEditProfile = false },
                onAddPatient = { updated ->
                    viewModel.updatePatient(updated)
                    showEditProfile = false
                },
                existingPatient = p
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified // Just standard
    )
}

@Composable
private fun FamilyQuickScroll(members: List<Patient>, onNavigate: (Long) -> Unit) {
    androidx.compose.foundation.lazy.LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(members) { member ->
            Surface(
                onClick = { onNavigate(member.id) },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Person, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = member.name.split(" ").first(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
