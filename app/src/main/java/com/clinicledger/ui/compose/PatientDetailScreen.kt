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

/**
 * Screen displaying comprehensive medical and financial data for a single patient.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailScreen(
    /** Unique ID of the patient to display */
    patientId: Long,
    /** Data provider for patient state */
    viewModel: PatientDetailViewModel,
    /** Voice assistant context manager */
    voiceViewModel: VoiceAssistantViewModel = viewModel(),
    /** Callback for back navigation */
    onNavigateBack: () -> Unit,
    /** Callback for navigating to related family members */
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

    var showAddMedicine by remember { mutableStateOf(value = false) }
    var showRecordPayment by remember { mutableStateOf(value = false) }
    var showAddAlias by remember { mutableStateOf(value = false) }
    var showFamilyTree by remember { mutableStateOf(value = false) }
    var showEditProfile by remember { mutableStateOf(value = false) }

    val takePictureLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview(),
    ) { bitmap ->
        bitmap?.let { viewModel.updatePhoto(patientId, it) }
    }

    patient?.let { p ->
        Scaffold(
            topBar = {
                ClinicTopAppBar(
                    title = stringResource(R.string.medical_record_title),
                    subtitle = p.phone.ifBlank { stringResource(R.string.id_prefix, p.id) },
                    navigationIcon = { BackNavigationIcon(onNavigateBack) },
                    actions = {
                        IconButton(onClick = { showEditProfile = true }) {
                            Icon(Icons.Rounded.Edit, contentDescription = stringResource(R.string.edit_btn))
                        }
                    },
                )
            },
            floatingActionButton = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    FloatingActionButton(
                        onClick = { showAddMedicine = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                    ) {
                        Icon(Icons.Rounded.MedicalServices, contentDescription = stringResource(R.string.add_medicine))
                    }
                    FloatingActionButton(
                        onClick = { showRecordPayment = true },
                        containerColor = Color(0xFF0D9488), // Medical Teal
                        contentColor = Color.White,
                    ) {
                        Icon(Icons.Rounded.Payments, contentDescription = stringResource(R.string.record_payment))
                    }
                }
            },
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = paddingValues.calculateTopPadding() / 2, // Reduced top padding
                        bottom = paddingValues.calculateBottomPadding(),
                    )
                    .background(MaterialTheme.colorScheme.background),
            ) {
                // 1. Patient Info Header
                item {
                    PatientDetailHeader(
                        patient = p,
                        villageName = p.village?.name ?: stringResource(R.string.no_village),
                        aliases = aliases,
                        onAddAlias = { showAddAlias = true },
                        onDeleteAlias = { viewModel.deleteAlias(it) },
                        onShowFamilyTree = { showFamilyTree = true },
                        onCapturePhoto = { takePictureLauncher.launch(null) },
                        hasFamily = p.familyGroupId != null,
                    )
                }

                // 2. Financial Summary
                item {
                    BalanceCard(balance = p.currentBalance)
                }

                // 3. Family / Relationships Section
                if (familyMembers.isNotEmpty()) {
                    item {
                        SectionHeader(stringResource(R.string.family_relationships_header))
                        FamilyQuickScroll(familyMembers.filter { it.id != p.id }, onNavigateToPatientDetail)
                    }
                }

                // 4. Clinical History (Timeline)
                item {
                    SectionHeader(stringResource(R.string.clinical_history_header))
                }
                
                if (transactions.isEmpty()) {
                    item {
                        ClinicEmptyState(
                            message = stringResource(R.string.no_clinical_history_msg),
                        )
                    }
                } else {
                    items(
                        items = transactions.sortedByDescending { it.createdAt },
                        key = { it.id },
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
                currentPatient = p,
                familyMembers = familyMembers,
                isHindi = isHindi,
                onDismiss = { showAddMedicine = false },
            ) { amount, notes, date, beneficiaryId ->
                viewModel.addTransaction(p.id, "medicine", amount, notes, date, beneficiaryId)
                showAddMedicine = false
            }
        }
        if (showRecordPayment) {
            AddTransactionDialog(
                type = "payment",
                currentPatient = p,
                familyMembers = familyMembers,
                isHindi = isHindi,
                onDismiss = { showRecordPayment = false },
            ) { amount, notes, date, beneficiaryId ->
                viewModel.addTransaction(p.id, "payment", amount, notes, date, beneficiaryId)
                showRecordPayment = false
            }
        }
        if (showAddAlias) {
            AddAliasDialog(
                onDismiss = { showAddAlias = false },
                onConfirm = { alias ->
                    viewModel.addAlias(p.id, alias)
                    showAddAlias = false
                },
            )
        }
        if (showFamilyTree && (p.familyGroupId != null)) {
            FamilyTreeDialog(
                familyName = p.name,
                members = familyMembers,
                onDismiss = { showFamilyTree = false },
                onNavigateToPatient = { id ->
                    showFamilyTree = false
                    onNavigateToPatientDetail(id)
                },
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
                existingPatient = p,
            )
        }
    }
}

/**
 * Visual header for data sections.
 */
@Composable
private fun SectionHeader(/** Header text */ title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
    )
}

/**
 * Horizontal scroll of family member avatars.
 */
@Composable
private fun FamilyQuickScroll(
    /** Other members in the group */
    members: List<Patient>,
    /** Navigation callback */
    onNavigate: (Long) -> Unit,
) {
    androidx.compose.foundation.lazy.LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(members) { member ->
            Surface(
                onClick = { onNavigate(member.id) },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.Person, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = member.name.split(" ").first(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
