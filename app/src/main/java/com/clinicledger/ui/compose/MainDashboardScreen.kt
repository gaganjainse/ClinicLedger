package com.clinicledger.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.clinicledger.R
import com.clinicledger.data.repository.PatientRepository
import com.clinicledger.ui.analytics.viewmodel.AnalyticsViewModel
import com.clinicledger.ui.compose.components.ClinicTopAppBar
import com.clinicledger.ui.compose.tabs.ClinicMemoryTab
import com.clinicledger.ui.search.viewmodel.SearchViewModel
import com.clinicledger.ui.util.LocaleManager.LocalIsHindi

/**
 * Main navigation destinations for the app.
 * Diagnostics removed from bottom bar as per physical body sync plan.
 */
enum class NavItem(val icon: androidx.compose.ui.graphics.vector.ImageVector, val labelRes: Int, val titleRes: Int) {
    LEDGER(Icons.AutoMirrored.Rounded.ListAlt, R.string.nav_ledger, R.string.app_name),
    ANALYTICS(Icons.Rounded.BarChart, R.string.nav_analytics, R.string.analytics_title),
    MEMORY(Icons.Rounded.Psychology, R.string.tab_memory, R.string.clinic_memory_os),
    VILLAGES(Icons.Rounded.Cabin, R.string.nav_villages, R.string.villages_title),
    SETTINGS(Icons.Rounded.Settings, R.string.nav_settings, R.string.settings_title)
}

/**
 * The root container for the primary application hubs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(
    viewModel: SearchViewModel,
    analyticsViewModel: AnalyticsViewModel,
    voiceViewModel: VoiceAssistantViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAddPatient: () -> Unit,
    onNavigateToDiagnostics: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onOpenVoiceSheet: () -> Unit,
    onToggleLanguage: () -> Unit,
) {
    val context = LocalContext.current
    var selectedItem by rememberSaveable { mutableStateOf(NavItem.LEDGER) }
    val isHindi = LocalIsHindi.current

    // Observe data from ViewModel for the dashboard
    val recentPatients by viewModel.recentPatients.observeAsState(emptyList())
    val searchResults by viewModel.searchResults.observeAsState(emptyList())
    val totalCollectedToday by viewModel.totalCollectedToday.observeAsState(0.0)
    val allPatients by viewModel.allPatients.observeAsState(emptyList())
    val familyGroups by viewModel.familyGroups.observeAsState(emptyList())
    val villages by viewModel.villages.observeAsState(emptyList())
    
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            ClinicTopAppBar(
                title = stringResource(selectedItem.titleRes),
                subtitle = if (selectedItem == NavItem.LEDGER) "Clinic Ledger OS v2.0" else null
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavItem.entries.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(stringResource(item.labelRes), fontWeight = FontWeight.Bold) },
                        selected = selectedItem == item,
                        onClick = { 
                            selectedItem = item 
                            searchQuery = "" 
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedItem == NavItem.LEDGER) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SmallFloatingActionButton(
                        onClick = onOpenVoiceSheet,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        Icon(Icons.Rounded.Mic, contentDescription = "Voice")
                    }
                    FloatingActionButton(
                        onClick = onNavigateToAddPatient,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add Patient")
                    }
                }
            }
        }
    ) { paddingValues ->
        val contentModifier = Modifier.padding(paddingValues)
        
        Box(modifier = contentModifier) {
            when (selectedItem) {
                NavItem.LEDGER -> LedgerDashboardScreen(
                    searchQuery = searchQuery,
                    onSearchChange = { 
                        searchQuery = it
                        viewModel.searchPatients(it)
                    },
                    recentPatients = recentPatients,
                    searchResults = searchResults,
                    totalCollectedToday = totalCollectedToday,
                    allPatients = allPatients,
                    familyGroups = familyGroups,
                    onNavigateToDetail = onNavigateToDetail,
                    isHindi = isHindi
                )
                NavItem.ANALYTICS -> AnalyticsContent(
                    viewModel = analyticsViewModel,
                    onNavigateToPatientDetail = onNavigateToDetail
                )
                NavItem.MEMORY -> {
                    val repository = remember { PatientRepository(context) }
                    val villageMap = remember(villages) { villages.associate { it.id to it.name } }
                    
                    ClinicMemoryTab(
                        repository = repository,
                        onNavigateToDetail = onNavigateToDetail,
                        villages = villages,
                        familyGroups = familyGroups,
                        villageMap = villageMap
                    )
                }
                NavItem.VILLAGES -> VillageManagementContent()
                NavItem.SETTINGS -> SettingsScreen(
                    onToggleLanguage = onToggleLanguage,
                    voiceViewModel = voiceViewModel,
                    onNavigateToDiagnostics = onNavigateToDiagnostics,
                    onNavigateToAbout = onNavigateToAbout
                )
            }
        }
    }
}
