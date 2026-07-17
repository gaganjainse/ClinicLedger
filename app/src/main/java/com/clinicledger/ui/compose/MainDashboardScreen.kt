package com.clinicledger.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clinicledger.R
import com.clinicledger.data.repository.PatientRepository
import com.clinicledger.ui.analytics.viewmodel.AnalyticsViewModel
import com.clinicledger.ui.compose.tabs.ClinicMemoryTab
import com.clinicledger.ui.compose.tabs.TransactionsTab
import com.clinicledger.ui.search.viewmodel.SearchViewModel
import com.clinicledger.ui.util.LocaleManager.LocalIsHindi
import kotlinx.coroutines.launch

/**
 * Real clinical navigation items replacing template placeholders.
 */
enum class NavItem(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String,
    val route: String
) {
    MEMORY(Icons.Rounded.Psychology, "Memory", "MEMORY"),
    JOURNAL(Icons.AutoMirrored.Rounded.MenuBook, "Journal", "JOURNAL"),
    ANALYTICS(Icons.Rounded.BarChart, "Analytics", "ANALYTICS"),
    SETTINGS(Icons.Rounded.Settings, "Settings", "SETTINGS"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(
    /** Primary ViewModel */
    viewModel: SearchViewModel,
    /** Analytics data provider */
    analyticsViewModel: AnalyticsViewModel,
    /** Voice interaction logic */
    voiceViewModel: VoiceAssistantViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    /** Callback for navigating to patient details */
    onNavigateToDetail: (Long) -> Unit,
    /** Callback for opening the registration screen */
    onNavigateToAddPatient: () -> Unit,
    /** Callback for opening the diagnostics screen */
    onNavigateToDiagnostics: () -> Unit,
    /** Callback for opening the about screen */
    onNavigateToAbout: () -> Unit,
    /** Callback for PTT press */
    onMicPress: () -> Unit = {},
    /** Callback for PTT release */
    onMicRelease: () -> Unit = {},
    /** Callback for Mic tap */
    onMicTap: (ConversationState) -> Unit = {},
    /** Callback for switching between English and Hindi */
    onToggleLanguage: () -> Unit,
) {
    val context = LocalContext.current
    val isHindi = LocalIsHindi.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Observe data
    val totalCollectedToday by viewModel.totalCollectedToday.observeAsState(0L)
    val allPatients by viewModel.allPatients.observeAsState(emptyList())
    val allTransactions by viewModel.allTransactions.observeAsState(emptyList())
    val familyGroups by viewModel.familyGroups.observeAsState(emptyList())
    val villages by viewModel.villages.observeAsState(emptyList())
    
    var searchQuery by remember { mutableStateOf("") }
    var journalQuery by remember { mutableStateOf("") }
    
    var selectedRoute by remember { mutableStateOf("LEDGER") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ChatGPTDrawerContent(
                onSearchClick = { /* Implement global search if needed */ },
                onNewChatClick = { 
                    selectedRoute = "LEDGER"
                    searchQuery = ""
                    scope.launch { drawerState.close() }
                },
                onItemClick = { route ->
                    selectedRoute = route
                    scope.launch { drawerState.close() }
                },
                selectedRoute = selectedRoute,
                recents = allPatients.take(5).map { it.name }
            )
        }
    ) {
        Scaffold(
            topBar = {
                // Shared Minimal Top Bar from ChatGPT template
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { scope.launch { drawerState.open() } },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Menu,
                            contentDescription = "Menu",
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Optional: Show Title for sub-pages, or keep minimal
                    if (selectedRoute != "LEDGER") {
                        Text(
                            text = selectedRoute.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = { 
                            selectedRoute = "LEDGER"
                            searchQuery = ""
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ChatBubbleOutline,
                            contentDescription = "New Chat",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                when (selectedRoute) {
                    "LEDGER" -> LedgerDashboardScreen(
                        searchQuery = searchQuery,
                        onSearchChange = { 
                            searchQuery = it
                            viewModel.searchPatients(it)
                        },
                        totalCollectedToday = totalCollectedToday,
                        allPatients = allPatients,
                        familyGroups = familyGroups,
                        onNavigateToAddPatient = onNavigateToAddPatient,
                        onNavigateToAnalytics = { selectedRoute = "ANALYTICS" },
                        onScanPatient = { bitmap ->
                            viewModel.identifyPatientByPhoto(bitmap) { patient ->
                                patient?.let { onNavigateToDetail(it.id) }
                            }
                        },
                        onStandardMicTap = {
                            onMicTap(ConversationState.LISTENING)
                        },
                        onLiveModeTap = {
                            onMicTap(ConversationState.LISTENING_LIVE)
                        },
                        onSuggestionClick = { suggestion ->
                            if (suggestion.contains("Brief") || suggestion.contains("सारांश")) {
                                voiceViewModel.processTranscript("Morning Brief", isHindi)
                                onMicTap(ConversationState.LISTENING)
                            }
                        },
                        isHindi = isHindi,
                    )
                    "JOURNAL" -> TransactionsTab(
                        query = journalQuery,
                        onQueryChange = { journalQuery = it },
                        transactions = allTransactions,
                        allPatients = allPatients,
                        villages = villages,
                        onNavigateToDetail = onNavigateToDetail,
                        isHindi = isHindi,
                    )
                    "ANALYTICS" -> AnalyticsContent(
                        viewModel = analyticsViewModel,
                        onNavigateToPatientDetail = onNavigateToDetail,
                    )
                    "MEMORY" -> {
                        val repository = remember { PatientRepository(context) }
                        val villageMap = remember(villages) { 
                            villages.associateBy { it.id }.mapValues { it.value.name } 
                        }
                        ClinicMemoryTab(
                            repository = repository,
                            onNavigateToDetail = onNavigateToDetail,
                            villages = villages,
                            familyGroups = familyGroups,
                            villageMap = villageMap,
                        )
                    }
                    "SETTINGS" -> SettingsScreen(
                        onToggleLanguage = onToggleLanguage,
                        voiceViewModel = voiceViewModel,
                        onNavigateToDiagnostics = onNavigateToDiagnostics,
                        onNavigateToAbout = onNavigateToAbout,
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatGPTDrawerContent(
    onSearchClick: () -> Unit,
    onNewChatClick: () -> Unit,
    onItemClick: (String) -> Unit,
    selectedRoute: String,
    recents: List<String>
) {
    ModalDrawerSheet(
        modifier = Modifier.fillMaxHeight(),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerShape = RoundedCornerShape(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Clinic Ledger",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Icon(
                        Icons.Rounded.Search, 
                        contentDescription = "Search", 
                        modifier = Modifier.clickable(onClick = onSearchClick).size(24.dp)
                    )
                    Icon(
                        Icons.Rounded.ChatBubbleOutline, 
                        contentDescription = "New Chat",
                        modifier = Modifier.clickable(onClick = onNewChatClick).size(24.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Main Nav Items (Memory, Journal, Analytics, Settings)
            NavItem.entries.forEach { item ->
                DrawerItem(
                    icon = item.icon,
                    label = item.label,
                    selected = selectedRoute == item.route,
                    onClick = { onItemClick(item.route) }
                )
            }
            
            DrawerItem(
                icon = Icons.Rounded.MoreHoriz,
                label = "More",
                selected = false,
                onClick = {}
            )

            Spacer(Modifier.height(32.dp))

            // Recents Section
            Text(
                text = "Recents",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(recents) { recent ->
                    Text(
                        text = recent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { }
                            .padding(vertical = 12.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // Footer matching Nav Drawer Overlay.jpeg
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Blue Chat Pill Button
                Surface(
                    onClick = { onItemClick("LEDGER") },
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.height(48.dp).width(120.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Rounded.Message, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Chat", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                // User Initials Circle
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = Color(0xFFE67E22) // Orange circle
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("GJ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.width(16.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}
