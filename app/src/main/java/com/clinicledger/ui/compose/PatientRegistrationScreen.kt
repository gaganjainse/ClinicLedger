package com.clinicledger.ui.compose

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.clinicledger.R
import com.clinicledger.data.models.FamilyGroup
import com.clinicledger.data.models.Patient
import com.clinicledger.data.models.Village
import com.clinicledger.data.repository.PatientRepository
import com.clinicledger.ui.compose.components.ClinicScaffold
import com.clinicledger.ui.compose.components.CreateFamilyGroupDialog
import com.clinicledger.ui.search.viewmodel.SearchViewModel
import com.clinicledger.ui.util.LocaleManager
import com.clinicledger.ui.util.LocaleManager.LocalIsHindi
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

/**
 * Modern Clinical Registration hub.
 * Refactored for clearer relationship roles and family integration.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientRegistrationScreen(
    /** ViewModel for village and family lookup */
    viewModel: SearchViewModel,
    /** Callback to exit registration */
    onNavigateBack: () -> Unit,
    /** Callback triggered after a patient is successfully added */
    onPatientAdded: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { PatientRepository(context) }
    val villages by viewModel.villages.observeAsState(emptyList())
    val familyGroups by viewModel.familyGroups.observeAsState(emptyList())
    val isHindi = LocalIsHindi.current

    var nameEng by remember { mutableStateOf("") }
    var nameHindi by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedVillage by remember { mutableStateOf<Village?>(null) }
    var selectedFamily by remember { mutableStateOf<FamilyGroup?>(null) }
    
    val relationshipRoles = listOf(
        stringResource(R.string.relationship_self),
        if (isHindi) "घर का मुखिया" else "Head of Household",
        if (isHindi) "परिवार का सदस्य" else "Family Member",
        if (isHindi) "अभिभावक" else "Guardian",
        if (isHindi) "आश्रित" else "Dependent",
    )
    
    var relationship by remember { mutableStateOf(relationshipRoles[0]) }
    var relationshipExpanded by remember { mutableStateOf(value = false) }
    var villageExpanded by remember { mutableStateOf(value = false) }
    var familyExpanded by remember { mutableStateOf(value = false) }
    var showCreateFamilyDialog by remember { mutableStateOf(value = false) }
    var photoPath by remember { mutableStateOf<String?>(null) }

    val photoStorage = remember { com.clinicledger.service.PhotoStorageService(context) }
    val cameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview(),
    ) { bitmap ->
        bitmap?.let {
            photoPath = photoStorage.savePatientPhoto(it)
        }
    }

    ClinicScaffold(
        title = if (isHindi) "नया पंजीकरण" else "New Registration",
        onBack = onNavigateBack,
        actions = {
            TextButton(
                onClick = {
                    if ((nameEng.isNotBlank() || nameHindi.isNotBlank()) && (selectedVillage != null)) {
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                val fullName = if (nameEng.isNotBlank() && nameHindi.isNotBlank()) {
                                    "$nameEng / $nameHindi"
                                } else {
                                    nameEng.ifBlank { nameHindi }
                                }
                                repository.insertPatient(Patient(
                                    name = fullName.trim(),
                                    phone = phone.trim(),
                                    villageId = selectedVillage!!.id,
                                    familyGroupId = selectedFamily?.id,
                                    relationship = relationship,
                                    photoPath = photoPath,
                                ))
                            }
                            Toast.makeText(context, if (isHindi) "सफलतापूर्वक सहेजा गया" else "Saved successfully", Toast.LENGTH_SHORT).show()
                            onPatientAdded()
                        }
                    }
                },
                enabled = (nameEng.isNotBlank() || nameHindi.isNotBlank()) && (selectedVillage != null),
            ) {
                Text(if (isHindi) "सहेजें" else "Save", fontWeight = FontWeight.Bold)
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Section 0: Photo
            ClinicalFormCard(title = if (isHindi) "फोटो" else "Profile Photo") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { cameraLauncher.launch(null) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoPath != null) {
                            val bitmap = remember(photoPath) { photoStorage.loadPhoto(photoPath!!) }
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.AddAPhoto,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Section 1: Identity
            ClinicalFormCard(title = if (isHindi) "मरीज की पहचान" else "Patient Identity") {
                OutlinedTextField(
                    value = nameEng,
                    onValueChange = { 
                        nameEng = it
                        if (nameHindi.isBlank()) {
                            nameHindi = LocaleManager.transliterate(it)
                        }
                    },
                    label = { Text(if (isHindi) "नाम (English)" else "Full Name (English)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Rounded.Badge, null) },
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = nameHindi,
                    onValueChange = { nameHindi = it },
                    label = { Text(if (isHindi) "नाम (हिन्दी)" else "Full Name (Hindi)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Rounded.Translate, null) },
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(if (isHindi) "फोन नंबर (वैकल्पिक)" else "Phone Number (Optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Rounded.Phone, null) },
                    singleLine = true,
                )
            }

            // Section 2: Clinical & Social Context
            ClinicalFormCard(title = if (isHindi) "सामाजिक और क्लीनिकल संदर्भ" else "Social & Clinical Context") {
                // Village Selection
                ExposedDropdownMenuBox(
                    expanded = villageExpanded,
                    onExpandedChange = { villageExpanded = it },
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = selectedVillage?.let { LocaleManager.getLocalizedVillage(it.name, it.nameHindi) } ?: (if (isHindi) "गाँव चुनें" else "Select Village"),
                        onValueChange = {},
                        label = { Text(if (isHindi) "गाँव" else "Village") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = villageExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(12.dp),
                    )
                    ExposedDropdownMenu(expanded = villageExpanded, onDismissRequest = { villageExpanded = false }) {
                        villages.forEach { v ->
                            DropdownMenuItem(
                                text = { Text(LocaleManager.getLocalizedVillage(v.name, v.nameHindi)) },
                                onClick = {
                                    selectedVillage = v
                                    villageExpanded = false
                                    selectedFamily = null // Reset family if village changes
                                },
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                // Family Group Selection
                val filteredFamilies = remember(selectedVillage, familyGroups) {
                    if (selectedVillage == null) emptyList() 
                    else familyGroups.filter { it.villageId == selectedVillage!!.id }
                }

                ExposedDropdownMenuBox(
                    expanded = familyExpanded,
                    onExpandedChange = { if (selectedVillage != null) familyExpanded = it },
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = selectedFamily?.let { LocaleManager.getLocalizedText(it.name) } ?: (if (isHindi) "परिवार चुनें (वैकल्पिक)" else "Select Family (Optional)"),
                        onValueChange = {},
                        label = { Text(if (isHindi) "परिवार समूह" else "Family Group") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = familyExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(12.dp),
                        enabled = selectedVillage != null,
                    )
                    ExposedDropdownMenu(expanded = familyExpanded, onDismissRequest = { familyExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text(if (isHindi) "+ नया परिवार बनाएँ" else "+ Create New Family", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                            onClick = {
                                familyExpanded = false
                                showCreateFamilyDialog = true
                            },
                        )
                        if (selectedFamily != null) {
                            DropdownMenuItem(
                                text = { Text(if (isHindi) "परिवार से हटाएँ" else "Unlink Family", color = MaterialTheme.colorScheme.error) },
                                onClick = { selectedFamily = null; familyExpanded = false },
                            )
                        }
                        filteredFamilies.forEach { fg ->
                            DropdownMenuItem(
                                text = { Text(LocaleManager.getLocalizedText(fg.name)) },
                                onClick = {
                                    selectedFamily = fg
                                    familyExpanded = false
                                },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Role/Relationship in Household
                ExposedDropdownMenuBox(
                    expanded = relationshipExpanded,
                    onExpandedChange = { relationshipExpanded = it },
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = relationship,
                        onValueChange = {},
                        label = { Text(if (isHindi) "परिवार में भूमिका" else "Role in Household") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = relationshipExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(12.dp),
                    )
                    ExposedDropdownMenu(expanded = relationshipExpanded, onDismissRequest = { relationshipExpanded = false }) {
                        relationshipRoles.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role) },
                                onClick = {
                                    relationship = role
                                    relationshipExpanded = false
                                },
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    if (showCreateFamilyDialog && (selectedVillage != null)) {
        val village = selectedVillage ?: return
        CreateFamilyGroupDialog(
            villages = listOf(village),
            onDismiss = { showCreateFamilyDialog = false },
        ) { name, caste, headName, villageId ->
            scope.launch {
                val newId = withContext(Dispatchers.IO) {
                    repository.insertFamilyGroup(FamilyGroup(name = name, caste = caste, familyHeadName = headName, villageId = villageId))
                }
                // Update local selection
                selectedFamily = FamilyGroup(id = newId, name = name, caste = caste, familyHeadName = headName, villageId = villageId)
                showCreateFamilyDialog = false
            }
        }
    }
}

/**
 * Standard container card for clinical forms.
 */
@Composable
private fun ClinicalFormCard(
    /** Section title */
    title: String,
    /** Card content */
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp),
            )
            content()
        }
    }
}
