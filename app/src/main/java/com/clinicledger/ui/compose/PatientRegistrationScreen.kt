package com.clinicledger.ui.compose

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.clinicledger.R
import com.clinicledger.data.models.Patient
import com.clinicledger.data.models.Village
import com.clinicledger.data.repository.PatientRepository
import com.clinicledger.ui.compose.components.ClinicScaffold
import com.clinicledger.ui.search.viewmodel.SearchViewModel
import com.clinicledger.ui.util.LocaleManager
import com.clinicledger.ui.util.LocaleManager.LocalIsHindi
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

/**
 * Modern Clinical Registration hub.
 * Redesigned for ergonomics and efficiency.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientRegistrationScreen(
    viewModel: SearchViewModel,
    onNavigateBack: () -> Unit,
    onPatientAdded: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { PatientRepository(context) }
    val villages by viewModel.villages.observeAsState(emptyList())
    val isHindi = LocalIsHindi.current

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedVillage by remember { mutableStateOf<Village?>(null) }
    var relationship by remember { mutableStateOf("Self") }
    var relationshipExpanded by remember { mutableStateOf(false) }
    var villageExpanded by remember { mutableStateOf(false) }

    ClinicScaffold(
        title = if (isHindi) "नया पंजीकरण" else "New Registration",
        onBack = onNavigateBack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Section 1: Basic Info
            ClinicalFormCard(title = if (isHindi) "बुनियादी जानकारी" else "Basic Information") {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.patient_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(stringResource(R.string.phone_number_optional)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Section 2: Clinical Context
            ClinicalFormCard(title = if (isHindi) "क्लीनिकल संदर्भ" else "Clinical Context") {
                // Village
                ExposedDropdownMenuBox(
                    expanded = villageExpanded,
                    onExpandedChange = { villageExpanded = !villageExpanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = selectedVillage?.let { LocaleManager.getLocalizedVillage(it.name, it.nameHindi) } ?: stringResource(R.string.select_village_label),
                        onValueChange = {},
                        label = { Text(stringResource(R.string.village_label)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = villageExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = villageExpanded, onDismissRequest = { villageExpanded = false }) {
                        villages.forEach { v ->
                            DropdownMenuItem(
                                text = { Text(LocaleManager.getLocalizedVillage(v.name, v.nameHindi)) },
                                onClick = {
                                    selectedVillage = v
                                    villageExpanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                // Relationship
                ExposedDropdownMenuBox(
                    expanded = relationshipExpanded,
                    onExpandedChange = { relationshipExpanded = !relationshipExpanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = relationship,
                        onValueChange = {},
                        label = { Text(stringResource(R.string.relationship_label)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = relationshipExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = relationshipExpanded, onDismissRequest = { relationshipExpanded = false }) {
                        listOf("Self", "Spouse", "Son", "Daughter", "Father", "Mother").forEach { rel ->
                            DropdownMenuItem(
                                text = { Text(rel) },
                                onClick = {
                                    relationship = rel
                                    relationshipExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Submit Button
            Button(
                onClick = {
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            repository.insertPatient(
                                Patient(
                                    name = name.trim(),
                                    phone = phone.trim(),
                                    villageId = selectedVillage?.id ?: 1,
                                    relationship = relationship
                                )
                            )
                        }
                        Toast.makeText(context, "Patient registered successfully", Toast.LENGTH_SHORT).show()
                        onPatientAdded()
                    }
                },
                enabled = name.isNotBlank() && selectedVillage != null,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(if (isHindi) "पंजीकरण सहेजें" else "Save Registration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun ClinicalFormCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            content()
        }
    }
}
