package com.clinicledger.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.clinicledger.R
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
import com.clinicledger.ui.compose.components.LandingOverlay
import com.clinicledger.ui.compose.components.TutorialOverlay
import com.clinicledger.ui.compose.components.VoiceAssistantBar
import com.clinicledger.ui.util.LocaleManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlin.time.Duration.Companion.milliseconds

/**
 * Screen route definitions for Navigation.
 */
sealed class Screen(/** route key */ val route: String) {
    /** Dashboard home screen */
    object Dashboard : Screen("dashboard")
    /** Patient detailed record screen */
    object PatientDetail : Screen("patient_detail/{patientId}") {
        /** Creates a route for a specific patient ID */
        fun createRoute(patientId: Long): String = "patient_detail/$patientId"
    }
    /** New patient registration screen */
    object AddPatient : Screen("add_patient")
    /** Financial analytics and reporting screen */
    object Analytics : Screen("analytics?villageId={villageId}") {
        /** Creates a route for analytics, optionally filtered by village */
        fun createRoute(villageId: Long? = null): String = "analytics?villageId=${villageId ?: ""}"
    }
}

/**
 * Entry point of the application.
 * Manages Top-level navigation and Voice Assistant integration.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var searchViewModel: SearchViewModel
    private lateinit var detailViewModel: PatientDetailViewModel
    private lateinit var analyticsViewModel: AnalyticsViewModel
    private lateinit var voiceViewModel: VoiceAssistantViewModel

    private var showVoiceAssistantState = mutableStateOf(value = false)
    private var showLandingOverlay = mutableStateOf(value = true)
    private var showTutorialOverlay = mutableStateOf(value = false)

    private val voicePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            startVoiceAssistant()
        } else {
            Toast.makeText(
                this, 
                getString(R.string.error_generic_format, -1), 
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    override fun onCreate(/** state */ savedInstanceState: Bundle?) {
        LocaleManager.applyLocaleLegacy(this)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        searchViewModel = ViewModelProvider(this)[SearchViewModel::class.java]
        detailViewModel = ViewModelProvider(this)[PatientDetailViewModel::class.java]
        analyticsViewModel = ViewModelProvider(this)[AnalyticsViewModel::class.java]
        voiceViewModel = ViewModelProvider(this)[VoiceAssistantViewModel::class.java]

        val prefs = getSharedPreferences("clinic_ledger_prefs", MODE_PRIVATE)
        if (!prefs.getBoolean("tutorial_shown", false)) {
            showTutorialOverlay.value = true
        }

        lifecycleScope.launch(Dispatchers.IO) {
            com.clinicledger.data.util.DataSeeder.seedDatabaseIfNeeded(this@MainActivity)
            SystemGuardian(this@MainActivity).performHealthCheck()
        }
        
        BackupService.scheduleBackup(this)

        setContent {
            val isHindi = remember { LocaleManager.getSavedLocale(this) == "hi" }
            var showLanding by remember { showLandingOverlay }
            var showTutorial by remember { showTutorialOverlay }
            
            CompositionLocalProvider(LocaleManager.LocalIsHindi provides isHindi) {
                ClinicLedgerTheme {
                    val navController = rememberNavController()
                    var showExitToast by remember { mutableStateOf(value = false) }
                    val scope = rememberCoroutineScope()
                    val exitMsg = stringResource(R.string.double_back_exit_msg)

                    BackHandler {
                        if (navController.previousBackStackEntry == null) {
                            if (showExitToast) {
                                finish()
                            } else {
                                showExitToast = true
                                Toast.makeText(this@MainActivity, exitMsg, Toast.LENGTH_SHORT).show()
                                scope.launch {
                                    delay(2000.milliseconds)
                                    showExitToast = false
                                }
                            }
                        } else {
                            navController.popBackStack()
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                        ) { innerPadding ->
                            NavHost(
                                navController = navController,
                                startDestination = Screen.Dashboard.route,
                                modifier = Modifier.padding(innerPadding),
                                enterTransition = { fadeIn() },
                                exitTransition = { fadeOut() },
                            ) {
                                composable(Screen.Dashboard.route) {
                                    MainDashboardScreen(
                                        viewModel = searchViewModel,
                                        analyticsViewModel = analyticsViewModel,
                                        voiceViewModel = voiceViewModel,
                                        onNavigateToDetail = { id ->
                                            navController.navigate(Screen.PatientDetail.createRoute(id))
                                        },
                                        onNavigateToAddPatient = {
                                            navController.navigate(Screen.AddPatient.route)
                                        },
                                        onNavigateToDiagnostics = {},
                                        onNavigateToAbout = {},
                                        onMicPress = {},
                                        onMicRelease = {},
                                        onMicTap = { state ->
                                            startVoiceAssistant(state)
                                            scope.launch {
                                                delay(300L)
                                                showVoiceAssistantState.value = true
                                            }
                                        },
                                        onToggleLanguage = {
                                            val currentLang = LocaleManager.getSavedLocale(this@MainActivity)
                                            val nextLang = if (currentLang == "hi") "en" else "hi"
                                            setLocale(nextLang)
                                        }
                                    )
                                }
                                
                                composable(Screen.AddPatient.route) {
                                    PatientRegistrationScreen(
                                        viewModel = searchViewModel,
                                        onNavigateBack = { navController.popBackStack() },
                                    ) {
                                        navController.popBackStack()
                                    }
                                }

                                composable(
                                    route = Screen.Analytics.route,
                                    arguments = listOf(
                                        navArgument("villageId") {
                                            type = NavType.StringType
                                            nullable = true
                                            defaultValue = null
                                        },
                                    ),
                                ) { backStackEntry ->
                                    val villageIdStr = backStackEntry.arguments?.getString("villageId")
                                    val villageId = villageIdStr?.toLongOrNull()
                                    
                                    AnalyticsScreen(
                                        viewModel = analyticsViewModel,
                                        onNavigateBack = { navController.popBackStack() },
                                        onNavigateToPatientDetail = { id ->
                                            navController.navigate(Screen.PatientDetail.createRoute(id))
                                        },
                                        initialVillageId = villageId,
                                    )
                                }
                                
                                composable(
                                    route = Screen.PatientDetail.route,
                                    arguments = listOf(
                                        navArgument("patientId") { type = NavType.LongType },
                                    ),
                                ) { backStackEntry ->
                                    val patientId = backStackEntry.arguments?.getLong("patientId") ?: 0L
                                    PatientDetailScreen(
                                        patientId = patientId,
                                        viewModel = detailViewModel,
                                        onNavigateBack = { navController.popBackStack() },
                                        onNavigateToPatientDetail = { id ->
                                            navController.navigate(Screen.PatientDetail.createRoute(id))
                                        },
                                    )
                                }
                            }
                        }

                        // Real-time Transcript Bar (Removed PTT Bar as per request)
                    }

                    if (showLanding) {
                        LandingOverlay(onDismiss = { showLanding = false })
                    }

                    if (!showLanding && showTutorial) {
                        TutorialOverlay(onDismiss = { 
                            showTutorial = false
                            val prefs = getSharedPreferences("clinic_ledger_prefs", MODE_PRIVATE)
                            prefs.edit().apply { 
                                putBoolean("tutorial_shown", true) 
                                apply()
                            }
                        })
                    }

                    if (showVoiceAssistantState.value) {
                        val repository = PatientRepository(this@MainActivity)
                        val transactionRepository = TransactionRepository(this@MainActivity)
                        val toolbox = remember { 
                            ClinicalActionToolbox(this@MainActivity, repository, transactionRepository) 
                        }
                        
                        VoiceInputSheetCompose(
                            onDismiss = { 
                                showVoiceAssistantState.value = false 
                            },
                            viewModel = voiceViewModel,
                            onNavigateToPatientDetail = { id ->
                                navController.navigate(Screen.PatientDetail.createRoute(id))
                            },
                            onNavigateToAnalytics = { villageId ->
                                analyticsViewModel.refreshAnalytics()
                                navController.navigate(Screen.Analytics.createRoute(villageId))
                            },
                            onRunRoutine = { protocolId ->
                                lifecycleScope.launch {
                                    toolbox.runRoutine(protocolId, navController)
                                }
                            },
                        )
                    }
                }
            }
        }
    }

    private fun startVoiceAssistant(state: ConversationState = ConversationState.LISTENING) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            voiceViewModel.setState(state)
        } else {
            voicePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun setLocale(/** code */ lang: String) {
        LocaleManager.saveLocale(this, lang)
        LocaleManager.applyLocaleLegacy(this)
        recreate()
    }
}
