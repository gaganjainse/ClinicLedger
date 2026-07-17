package com.clinicledger.ui.compose.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.clinicledger.R
import com.clinicledger.data.models.FamilyGroup
import com.clinicledger.data.models.Patient
import com.clinicledger.data.models.Village
import com.clinicledger.data.repository.PatientRepository
import com.clinicledger.ui.compose.FamilyTreeDialog
import com.clinicledger.ui.compose.VillageManagementContent
import com.clinicledger.ui.compose.components.*
import com.clinicledger.ui.util.LocaleManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Sub-navigation states for the Memory hub.
 */
enum class MemorySubTab {
    /** Village management view */
    VILLAGES,
    /** Flat list of all patients */
    ALL_PATIENTS,
    /** Hierarchical family group management */
    FAMILIES,
    /** Patients not associated with any family group */
    INDIVIDUALS
}

/**
 * Redesigned Clinic Memory Tab.
 * Hub for Villages, Patients, Families, and Individual records.
 */
@Composable
fun ClinicMemoryTab(
    /** Data repository for patient lookups */
    repository: PatientRepository,
    /** Navigation callback for specific patient records */
    onNavigateToDetail: (Long) -> Unit,
    /** Static list of registered villages */
    villages: List<Village>,
    /** Static list of existing family groups */
    familyGroups: List<FamilyGroup>,
    /** Mapping of village IDs to localized names */
    villageMap: Map<Long, String>,
) {
    val scope = rememberCoroutineScope()
    var selectedSubTab by remember { mutableStateOf(value = MemorySubTab.VILLAGES) }
    var allPatients by remember { mutableStateOf<List<Patient>>(emptyList()) }
    
    // Unified Search/Filter State
    var query by remember { mutableStateOf("") }
    var selectedVillageId by remember { mutableStateOf<Long?>(null) }
    var sortOrder by remember { mutableStateOf("name") }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            allPatients = repository.getAllPatientsSync()
        }
    }

    val sortOptions = listOf(
        "name" to R.string.sort_name,
        "dues" to R.string.sort_dues,
        "visited" to R.string.sort_visited
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Sub-Tab Navigation (Ergonomic Horizontal Scroll)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MemoryNavButton(
                label = stringResource(R.string.tab_villages),
                icon = Icons.Rounded.Cabin,
                selected = selectedSubTab == MemorySubTab.VILLAGES,
            ) { selectedSubTab = MemorySubTab.VILLAGES }
            MemoryNavButton(
                label = stringResource(R.string.tab_all_patients),
                icon = Icons.Rounded.People,
                selected = selectedSubTab == MemorySubTab.ALL_PATIENTS,
            ) { selectedSubTab = MemorySubTab.ALL_PATIENTS }
            MemoryNavButton(
                label = stringResource(R.string.tab_family_groups_mem),
                icon = Icons.Rounded.Groups,
                selected = selectedSubTab == MemorySubTab.FAMILIES,
            ) { selectedSubTab = MemorySubTab.FAMILIES }
            MemoryNavButton(
                label = stringResource(R.string.tab_individuals),
                icon = Icons.Rounded.Person,
                selected = selectedSubTab == MemorySubTab.INDIVIDUALS,
            ) { selectedSubTab = MemorySubTab.INDIVIDUALS }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

        if (selectedSubTab != MemorySubTab.VILLAGES) {
            UnifiedSearchFilterBar(
                query = query,
                onQueryChange = { query = it },
                selectedVillageId = selectedVillageId,
                onVillageChange = { selectedVillageId = it },
                villages = villages,
                sortOrder = sortOrder,
                onSortChange = { sortOrder = it },
                sortOptions = sortOptions
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            when (selectedSubTab) {
                MemorySubTab.VILLAGES -> VillageManagementContent(modifier = Modifier.fillMaxSize())
                MemorySubTab.ALL_PATIENTS -> AllPatientsSection(
                    patients = allPatients,
                    query = query,
                    selectedVillageId = selectedVillageId,
                    sortOrder = sortOrder,
                    onNavigateToDetail = onNavigateToDetail,
                )
                MemorySubTab.FAMILIES -> FamilyGroupSection(
                    villages = villages,
                    allPatients = allPatients,
                    familyGroups = familyGroups,
                    villageMap = villageMap,
                    query = query,
                    selectedVillageId = selectedVillageId,
                    sortOrder = sortOrder,
                    onNavigateToDetail = onNavigateToDetail,
                    scope = scope,
                    repository = repository,
                    onRefresh = {
                        scope.launch { allPatients = repository.getAllPatientsSync() }
                    }
                )
                MemorySubTab.INDIVIDUALS -> IndividualPatientsSection(
                    patients = allPatients.filter { it.familyGroupId == null },
                    query = query,
                    selectedVillageId = selectedVillageId,
                    sortOrder = sortOrder,
                    onNavigateToDetail = onNavigateToDetail,
                )
            }
        }
    }
}

/**
 * Specialized chip-style button for memory hub sub-navigation.
 */
@Composable
fun MemoryNavButton(
    /** UI label text */
    label: String,
    /** Leading icon */
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    /** Selection state */
    selected: Boolean,
    /** Click handler */
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontWeight = FontWeight.Bold, maxLines = 1) },
        leadingIcon = { Icon(icon, null, modifier = Modifier.size(18.dp)) },
        shape = RoundedCornerShape(16.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
        ),
    )
}

/**
 * List section for family group management.
 */
@Composable
fun FamilyGroupSection(
    /** List of villages for filtering */
    villages: List<Village>,
    /** Full patient list */
    allPatients: List<Patient>,
    /** Full family group list */
    familyGroups: List<FamilyGroup>,
    /** Mapping of village IDs to names */
    villageMap: Map<Long, String>,
    /** Search text */
    query: String,
    /** Village filter */
    selectedVillageId: Long?,
    /** Sort field */
    sortOrder: String,
    /** Navigation handler */
    onNavigateToDetail: (Long) -> Unit,
    /** Coroutine scope */
    scope: kotlinx.coroutines.CoroutineScope,
    /** Repository */
    repository: PatientRepository,
    /** Callback for data refresh */
    onRefresh: () -> Unit,
) {
    var expandedFamilyGroupId by remember { mutableStateOf<Long?>(null) }
    var activeTreeFamilyGroup by remember { mutableStateOf<FamilyGroup?>(null) }
    var activeTreeMembers by remember { mutableStateOf<List<Patient>>(emptyList()) }
    var showCreateFamilyDialog by remember { mutableStateOf(value = false) }
    var familyToAssociate by remember { mutableStateOf<FamilyGroup?>(null) }
    
    var editingFamily by remember { mutableStateOf<FamilyGroup?>(null) }
    var familyToDelete by remember { mutableStateOf<FamilyGroup?>(null) }

    familyToAssociate?.let { family ->
        val unlinkedInVillage = allPatients.filter { it.familyGroupId == null && it.villageId == family.villageId }
        AlertDialog(
            onDismissRequest = { familyToAssociate = null },
            title = { Text("Add Member to ${LocaleManager.getLocalizedText(family.name)}") },
            text = {
                if (unlinkedInVillage.isEmpty()) {
                    Text("No unlinked patients in this village.")
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(unlinkedInVillage) { p ->
                            ListItem(
                                headlineContent = { Text(p.name) },
                                modifier = Modifier.clickable { 
                                    scope.launch {
                                        withContext(Dispatchers.IO) {
                                            repository.updatePatient(p.copy(familyGroupId = family.id))
                                        }
                                        onRefresh()
                                        familyToAssociate = null
                                    }
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { familyToAssociate = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    activeTreeFamilyGroup?.let { /** family */ family ->
        FamilyTreeDialog(
            familyName = LocaleManager.getLocalizedText(family.name),
            members = activeTreeMembers,
            onDismiss = { activeTreeFamilyGroup = null },
            onNavigateToPatient = onNavigateToDetail,
        )
    }

    val filteredFamilies = familyGroups.asSequence()
        .filter { selectedVillageId == null || it.villageId == selectedVillageId }
        .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) || it.familyHeadName?.contains(query, ignoreCase = true) == true }
        .toList()
        .let { list ->
            when (sortOrder) {
                "dues" -> list.sortedByDescending { fg -> allPatients.filter { it.familyGroupId == fg.id }.sumOf { it.currentBalance } }
                else -> list.sortedBy { it.name }
            }
        }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(
                        R.string.family_groups_section_title, 
                        filteredFamilies.size,
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                TextButton(onClick = { showCreateFamilyDialog = true }) {
                    Text(stringResource(R.string.new_family_label), maxLines = 1)
                }
            }
        }

        items(filteredFamilies, key = { it.id }) { family ->
            val members = allPatients.filter { it.familyGroupId == family.id }
            FamilyCard(
                family = family,
                members = members,
                isExpanded = expandedFamilyGroupId == family.id,
                villageName = villageMap[family.villageId] ?: stringResource(R.string.no_village),
                onToggle = { expandedFamilyGroupId = if (expandedFamilyGroupId == family.id) null else family.id },
                onShowTree = {
                    activeTreeFamilyGroup = family
                    activeTreeMembers = members
                },
                onNavigateToDetail = onNavigateToDetail,
                onAddMember = {
                    familyToAssociate = family
                },
                onEdit = { editingFamily = family },
                onDelete = { familyToDelete = family }
            )
        }
        
        item { Spacer(modifier = Modifier.height(100.dp)) }
    }

    if (showCreateFamilyDialog) {
        CreateFamilyGroupDialog(
            villages = villages,
            onDismiss = { showCreateFamilyDialog = false },
        ) { /** ignore */ _, _, _, _ ->
            onRefresh()
            showCreateFamilyDialog = false
        }
    }
    
    editingFamily?.let { family ->
        CreateFamilyGroupDialog(
            villages = villages,
            onDismiss = { editingFamily = null },
            existingFamily = family,
        ) { name, caste, headName, villageId ->
            scope.launch {
                withContext(Dispatchers.IO) {
                    repository.updateFamilyGroup(
                        family.copy(
                            name = name,
                            caste = caste,
                            familyHeadName = headName,
                            villageId = villageId,
                        ),
                    )
                }
                onRefresh()
                editingFamily = null
            }
        }
    }

    familyToDelete?.let { family ->
        AlertDialog(
            onDismissRequest = { familyToDelete = null },
            title = { Text(stringResource(R.string.delete_family_title)) },
            text = { Text(stringResource(R.string.delete_family_confirm, family.name)) },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                repository.deleteFamilyGroup(family)
                            }
                            onRefresh()
                            familyToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { familyToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

/**
 * Interactive card displaying family group summary and members.
 */
@Composable
fun FamilyCard(
    /** The family group model */
    family: FamilyGroup,
    /** Linked patient members */
    members: List<Patient>,
    /** Expansion state */
    isExpanded: Boolean,
    /** Resolved village name */
    villageName: String,
    /** Click handler for expansion */
    onToggle: () -> Unit,
    /** Click handler for tree view */
    onShowTree: () -> Unit,
    /** Navigation handler for members */
    onNavigateToDetail: (Long) -> Unit,
    /** Click handler for adding a new member */
    onAddMember: () -> Unit,
    /** Edit handler */
    onEdit: () -> Unit,
    /** Delete handler */
    onDelete: () -> Unit,
) {
    val totalDue = members.sumOf { it.currentBalance }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = LocaleManager.getLocalizedText(family.name),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                    Text(
                        text = villageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = LocaleManager.formatCurrency(totalDue),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = if (totalDue > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = onAddMember, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Rounded.PersonAdd, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }
                            IconButton(onClick = onShowTree, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Rounded.AccountTree, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }
                            IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Rounded.Edit, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }
                            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Rounded.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
            if (isExpanded) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
                members.forEach { /** member */ m ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToDetail(m.id) }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = LocaleManager.formatPatientName(m.name),
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                        )
                        Text(
                            text = LocaleManager.formatCurrency(m.currentBalance),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (m.currentBalance > 0) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                        )
                    }
                }
            }
        }
    }
}

/**
 * List section for unlinked individual patients.
 */
@Composable
fun IndividualPatientsSection(
    /** Patients with no family group */
    patients: List<Patient>,
    /** Search text */
    query: String,
    /** Village filter */
    selectedVillageId: Long?,
    /** Sort field */
    sortOrder: String,
    /** Navigation callback */
    onNavigateToDetail: (Long) -> Unit,
) {
    val filtered = patients.asSequence()
        .filter { selectedVillageId == null || it.villageId == selectedVillageId }
        .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) || it.phone.contains(query, ignoreCase = true) }
        .toList()
        .let { list ->
            when (sortOrder) {
                "dues" -> list.sortedByDescending { it.currentBalance }
                "visited" -> list.sortedByDescending { it.updatedAt }
                else -> list.sortedBy { it.name }
            }
        }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.individual_records_header, filtered.size),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
        items(filtered) { /** patient */ p ->
            PatientListItem(patient = p, isHindi = LocaleManager.LocalIsHindi.current) {
                onNavigateToDetail(p.id)
            }
        }
        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

/**
 * Searchable list section for all clinic patients.
 */
@Composable
fun AllPatientsSection(
    /** Full patient database */
    patients: List<Patient>,
    /** Search text */
    query: String,
    /** Village filter */
    selectedVillageId: Long?,
    /** Sort field */
    sortOrder: String,
    /** Navigation callback */
    onNavigateToDetail: (Long) -> Unit,
) {
    val filtered = patients.asSequence()
        .filter { selectedVillageId == null || it.villageId == selectedVillageId }
        .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) || it.phone.contains(query, ignoreCase = true) }
        .toList()
        .let { list ->
            when (sortOrder) {
                "dues" -> list.sortedByDescending { it.currentBalance }
                "visited" -> list.sortedByDescending { it.updatedAt }
                else -> list.sortedBy { it.name }
            }
        }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(filtered) { /** patient */ p ->
            PatientListItem(patient = p, isHindi = LocaleManager.LocalIsHindi.current) {
                onNavigateToDetail(p.id)
            }
        }
        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}
