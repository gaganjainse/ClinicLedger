package com.clinicledger.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.clinicledger.data.repository.PatientRepository
import com.clinicledger.data.repository.TransactionRepository
import com.clinicledger.service.ClinicalActionToolbox
import com.clinicledger.service.BackupService
import com.clinicledger.service.SystemGuardian
import com.clinicledger.ui.compose.*
import com.clinicledger.ui.patientdetail.viewmodel.PatientDetailViewModel
import com.clinicledger.ui.search.viewmodel.SearchViewModel
import com.clinicledger.ui.analytics.viewmodel.AnalyticsViewModel
import com.clinicledger.ui.theme.ClinicLedgerTheme
import com.clinicledger.ui.util.LocaleManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

/**
 * Screen route definitions for Navigation.
 */
sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object PatientDetail : Screen("patient_detail/{patientId}") {
        fun createRoute(patientId: Long) = "patient_detail/$patientId"
    }
    object AddPatient : Screen("add_patient")
    object Analytics : Screen("analytics?villageId={villageId}") {
        fun createRoute(villageId: Long? = null) = "analytics?villageId=${villageId ?: ""}"
    }
    object Settings : Screen("settings")
    object Villages : Screen("villages")
    object Diagnostics : Screen("diagnostics")
    object About : Screen("about")
}

/**
 * Entry point of the application.
 * Manages the top-level Composable Navigation Host and system permissions.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var searchViewModel: SearchViewModel
    private lateinit var detailViewModel: PatientDetailViewModel
    private lateinit var analyticsViewModel: AnalyticsViewModel

    private var showVoiceAssistantState = mutableStateOf(value = false)

    /**
     * Request launcher for Audio Record permission required by Voice Assistant.
     */
    private val voicePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            showVoiceAssistantState.value = true
        } else {
            Toast.makeText(this, "Voice entry requires audio permission", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize locale before setting content
        LocaleManager.applyLocaleLegacy(this)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        searchViewModel = ViewModelProvider(this)[SearchViewModel::class.java]
        detailViewModel = ViewModelProvider(this)[PatientDetailViewModel::class.java]
        analyticsViewModel = ViewModelProvider(this)[AnalyticsViewModel::class.java]

        // Seed demo data if the DB is empty
        lifecycleScope.launch(Dispatchers.IO) {
            com.clinicledger.data.util.DataSeeder.seedDatabaseIfNeeded(this@MainActivity)
            // Activate Agentic System Guardian
            SystemGuardian(this@MainActivity).performHealthCheck()
        }
        
        // Start automatic backup scheduler
        BackupService.scheduleBackup(this)

        setContent {
            val isHindi = remember { LocaleManager.getSavedLocale(this) == "hi" }
            
            CompositionLocalProvider(LocaleManager.LocalIsHindi provides isHindi) {
                ClinicLedgerTheme {
                    val navController = rememberNavController()

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = Screen.Dashboard.route,
                            enterTransition = { fadeIn() },
                            exitTransition = { fadeOut() },
                        ) {
                            // Main Dashboard Hub
                            composable(Screen.Dashboard.route) {
                                MainDashboardScreen(
                                    viewModel = searchViewModel,
                                    analyticsViewModel = analyticsViewModel,
                                    onNavigateToDetail = { id ->
                                        navController.navigate(Screen.PatientDetail.createRoute(id))
                                    },
                                    onNavigateToAddPatient = {
                                        navController.navigate(Screen.AddPatient.route)
                                    },
                                    onNavigateToDiagnostics = {
                                        navController.navigate(Screen.Diagnostics.route)
                                    },
                                    onNavigateToAbout = {
                                        navController.navigate(Screen.About.route)
                                    },
                                    onOpenVoiceSheet = {
                                        triggerVoiceAssistant()
                                    },
                                    onToggleLanguage = {
                                        val currentLang = LocaleManager.getSavedLocale(this@MainActivity)
                                        val nextLang = if (currentLang == "hi") "en" else "hi"
                                        setLocale(nextLang)
                                    }
                                )
                            }
                            
                            // Registration Screen
                            composable(Screen.AddPatient.route) {
                                PatientRegistrationScreen(
                                    viewModel = searchViewModel,
                                    onNavigateBack = {
                                        navController.popBackStack()
                                    },
                                ) {
                                    navController.popBackStack()
                                }
                            }

                            // Analytics Screen (Deep Linkable)
                            composable(
                                route = Screen.Analytics.route,
                                arguments = listOf(
                                    navArgument("villageId") {
                                        type = NavType.StringType
                                        nullable = true
                                        defaultValue = null
                                    }
                                )
                            ) { backStackEntry ->
                                val villageIdStr = backStackEntry.arguments?.getString("villageId")
                                val villageId = villageIdStr?.toLongOrNull()
                                
                                AnalyticsScreen(
                                    viewModel = analyticsViewModel,
                                    initialVillageId = villageId,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateHome = { navController.navigate(Screen.Dashboard.route) },
                                    onNavigateToPatientDetail = { id: Long ->
                                        navController.navigate(Screen.PatientDetail.createRoute(id))
                                    }
                                )
                            }
                            
                            // Patient Record Screen
                            composable(
                                route = Screen.PatientDetail.route,
                                arguments = listOf(
                                    navArgument("patientId") { type = NavType.LongType }
                                ),
                            ) { backStackEntry ->
                                val patientId = backStackEntry.arguments?.getLong("patientId") ?: 0L

                                PatientDetailScreen(
                                    patientId = patientId,
                                    viewModel = detailViewModel,
                                    onNavigateBack = {
                                        navController.popBackStack()
                                    },
                                    onNavigateToPatientDetail = { id ->
                                        navController.navigate(Screen.PatientDetail.createRoute(id))
                                    }
                                )
                            }

                            // Diagnostics Screen
                            composable(Screen.Diagnostics.route) {
                                ArchitecturalDiagnosticHub(
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }

                            // About Screen
                            composable(Screen.About.route) {
                                AboutScreen(
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }

                    // Global Voice Assistant overlay
                    if (showVoiceAssistantState.value) {
                        val repository = PatientRepository(this@MainActivity)
                        val transactionRepository = TransactionRepository(this@MainActivity)
                        val toolbox = remember { ClinicalActionToolbox(this@MainActivity, repository, transactionRepository) }
                        
                        VoiceInputSheetCompose(
                            onDismiss = { showVoiceAssistantState.value = false },
                            onNavigateToPatientDetail = { id ->
                                navController.navigate(Screen.PatientDetail.createRoute(id))
                            },
                            onNavigateToAnalytics = { villageId ->
                                analyticsViewModel.refreshAnalytics()
                                navController.navigate(Screen.Analytics.createRoute(villageId))
                            },
                            onRunRoutine = { protocolId ->
                                lifecycleScope.launch {
                                    toolbox.runRoutine(protocolId, navController, isHindi)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    /**
     * Checks for audio permissions and displays the voice assistant sheet.
     */
    private fun triggerVoiceAssistant() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            showVoiceAssistantState.value = true
        } else {
            voicePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    /**
     * Updates the user's preferred language and restarts the activity while preserving navigation state.
     */
    private fun setLocale(lang: String) {
        LocaleManager.saveLocale(this, lang)
        LocaleManager.applyLocaleLegacy(this)
        
        // Activity will be recreated with new locale; NavHost will handle the rest if configured correctly
        recreate()
    }
}
