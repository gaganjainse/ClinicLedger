package com.clinicledger.ui.compose

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.withTransaction
import com.google.gson.GsonBuilder
import com.clinicledger.R
import com.clinicledger.data.local.ClinicLedgerDatabase
import com.clinicledger.data.models.SystemLog
import com.clinicledger.service.BackupService
import com.clinicledger.ui.backup.BackupData
import com.clinicledger.ui.util.LocaleManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

/**
 * Settings Screen Content.
 * Includes Agent Tuning, Database Management, and Backup & Restore.
 */
@Composable
fun SettingsScreen(
    /** Callback for switching between English and Hindi */
    onToggleLanguage: () -> Unit,
    /** Voice assistant configuration state */
    voiceViewModel: VoiceAssistantViewModel,
    /** Navigation callback for diagnostics */
    onNavigateToDiagnostics: () -> Unit,
    /** Navigation callback for app info */
    onNavigateToAbout: () -> Unit,
    /** UI customizer */
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { ClinicLedgerDatabase.getDatabase(context) }
    val gson = remember { GsonBuilder().setPrettyPrinting().create() }
    
    // Observe state from ViewModel
    val voiceSpeed by voiceViewModel.voiceSpeed.collectAsState()
    val activeLearningEnabled by voiceViewModel.activeLearningEnabled.collectAsState()

    // Backup State
    var autoBackupEnabled by remember { mutableStateOf(value = false) }
    var lastBackupTime by remember { mutableStateOf<String?>(null) }
    var operationProgress by remember { mutableStateOf(value = false) }
    var showRestoreConfirmDialog by remember { mutableStateOf(value = false) }
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }
    var backupLogs by remember { mutableStateOf<List<SystemLog>>(emptyList()) }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let {
            pendingRestoreUri = it
            showRestoreConfirmDialog = true
        }
    }

    fun refreshBackupStatus() {
        autoBackupEnabled = BackupService.isBackupScheduled(context)
        lastBackupTime = BackupService.getLastBackupTime(context)
        scope.launch(Dispatchers.IO) {
            backupLogs = database.systemLogDao().getLogsByComponent("BACKUP")
        }
    }

    LaunchedEffect(Unit) {
        refreshBackupStatus()
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // Section: General
            item {
                SettingsSectionHeader(stringResource(R.string.settings_section_general))
                SettingsItem(
                    title = stringResource(R.string.settings_language),
                    subtitle = if (LocaleManager.getSavedLocale(context) == "hi") "हिंदी (Hindi)" else "English",
                    icon = Icons.Rounded.Language,
                    onClick = onToggleLanguage,
                )
            }

            // Section: Agentic OS Tuning
            item {
                SettingsSectionHeader(stringResource(R.string.settings_section_agent))
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(stringResource(R.string.settings_voice_speed), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Slider(
                        value = voiceSpeed,
                        onValueChange = { 
                            voiceViewModel.setVoiceSpeed(it)
                        },
                        valueRange = 0.5f..1.5f,
                        steps = 10,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        val speedText = String.format(java.util.Locale.US, "%.1f", voiceSpeed)
                        Text(stringResource(R.string.voice_speed_slower), style = MaterialTheme.typography.labelSmall)
                        Text(stringResource(R.string.voice_speed_normal_format, speedText), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Text(stringResource(R.string.voice_speed_faster), style = MaterialTheme.typography.labelSmall)
                    }
                }
                
                ListItem(
                    headlineContent = { Text(stringResource(R.string.active_learning_mode_label), fontWeight = FontWeight.Bold) },
                    supportingContent = { Text(stringResource(R.string.active_learning_mode_desc)) },
                    leadingContent = { Icon(Icons.Rounded.AutoFixHigh, contentDescription = null) },
                    trailingContent = {
                        Switch(
                            checked = activeLearningEnabled, 
                            onCheckedChange = { 
                                voiceViewModel.setActiveLearningEnabled(it)
                            },
                        )
                    },
                )
            }

            // Section: Backup & Restore
            item {
                SettingsSectionHeader(stringResource(R.string.backup_title))
                
                ListItem(
                    headlineContent = { Text(stringResource(R.string.auto_backup_label), fontWeight = FontWeight.Bold) },
                    supportingContent = { 
                        Text(if (lastBackupTime != null) stringResource(R.string.last_auto_backup_status, lastBackupTime!!) else stringResource(R.string.no_auto_backup_yet))
                    },
                    leadingContent = { Icon(Icons.Rounded.CloudSync, contentDescription = null) },
                    trailingContent = {
                        Switch(
                            checked = autoBackupEnabled,
                            onCheckedChange = { isChecked ->
                                if (isChecked) BackupService.scheduleBackup(context) else BackupService.cancelBackup(context)
                                autoBackupEnabled = isChecked
                                refreshBackupStatus()
                            },
                        )
                    },
                )

                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            operationProgress = true
                            scope.launch(Dispatchers.IO) {
                                try {
                                    val vils = database.villageDao().getAllVillagesSync()
                                    val pts = database.patientDao().getAllPatientsSync()
                                    val als = database.aliasDao().getAllAliasesSync()
                                    val txs = database.transactionDao().getAllTransactionsSync()
                                    val fgs = database.familyGroupDao().getAllFamilyGroupsSync()

                                    val backupData = BackupData(villages = vils, patients = pts, aliases = als, transactions = txs, familyGroups = fgs)
                                    val json = gson.toJson(backupData)
                                    val fileName = "clinic_backup_${System.currentTimeMillis()}.json"
                                    val file = File(context.filesDir, fileName)
                                    file.writeText(json)

                                    database.systemLogDao().insertLog(SystemLog(
                                        level = "INFO", component = "BACKUP",
                                        message = "Manual backup created", metadata = fileName,
                                    ))

                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Backup saved: $fileName", Toast.LENGTH_SHORT).show()
                                        operationProgress = false
                                        refreshBackupStatus()
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                                        operationProgress = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(Icons.Rounded.Download, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.export_btn))
                    }

                    OutlinedButton(
                        onClick = { importLauncher.launch("application/json") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(Icons.Rounded.Upload, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.import_btn))
                    }
                }
            }

            // Section: Backup Logs
            item {
                SettingsSectionHeader(stringResource(R.string.backup_logs_title))
                if (backupLogs.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_backup_logs_msg),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        backupLogs.forEach { log ->
                            ListItem(
                                headlineContent = { Text(log.message, style = MaterialTheme.typography.bodyMedium) },
                                supportingContent = { 
                                    Text(
                                        text = "${LocaleManager.formatDateTime(log.timestamp)} - ${log.level}",
                                        style = MaterialTheme.typography.labelSmall
                                    ) 
                                },
                                leadingContent = { 
                                    Icon(
                                        imageVector = if (log.level == "ERROR") Icons.Rounded.ErrorOutline else Icons.Rounded.History,
                                        contentDescription = null,
                                        tint = if (log.level == "ERROR") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Section: System
            item {
                SettingsSectionHeader(stringResource(R.string.settings_section_system))
                SettingsItem(
                    title = stringResource(R.string.settings_system_health),
                    subtitle = stringResource(R.string.interactive_architecture_desc),
                    icon = Icons.Rounded.AccountTree,
                    onClick = onNavigateToDiagnostics,
                )
                SettingsItem(
                    title = stringResource(R.string.settings_app_info),
                    subtitle = stringResource(R.string.precision_version_desc),
                    icon = Icons.Rounded.Info,
                    onClick = onNavigateToAbout,
                )
            }
            
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }

        if (operationProgress) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }

    // Confirmation Dialogs
    if (showRestoreConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirmDialog = false },
            title = { Text(stringResource(R.string.confirm_restoration_title), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.confirm_restoration_msg)) },
            confirmButton = {
                Button(onClick = { 
                    showRestoreConfirmDialog = false
                    pendingRestoreUri?.let { uri ->
                        operationProgress = true
                        scope.launch(Dispatchers.IO) {
                            try {
                                val inputStream = context.contentResolver.openInputStream(uri)
                                if (inputStream != null) {
                                    val json = BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
                                    val backupData = gson.fromJson(json, BackupData::class.java)
                                    
                                    database.withTransaction {
                                        for (v in backupData.villages) database.villageDao().insertVillage(v)
                                        backupData.familyGroups?.let { for (fg in it) database.familyGroupDao().insertFamilyGroup(fg) }
                                        for (p in backupData.patients) database.patientDao().insertPatient(p)
                                        for (a in backupData.aliases) database.aliasDao().insertAlias(a)
                                        for (t in backupData.transactions) database.transactionDao().insertTransaction(t)
                                    }
                                    
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Import successful", Toast.LENGTH_SHORT).show()
                                        operationProgress = false
                                    }
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
                                    operationProgress = false
                                }
                            }
                        }
                    }
                }) { Text(stringResource(R.string.import_btn)) }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirmDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

/**
 * Visual header for settings sections.
 */
@Composable
fun SettingsSectionHeader(/** Section title text */ title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp),
        letterSpacing = 1.sp,
    )
}

/**
 * Interactive list item for a single setting.
 */
@Composable
fun SettingsItem(
    /** Primary label */
    title: String,
    /** Supporting description */
    subtitle: String,
    /** Icon vector */
    icon: ImageVector,
    /** Optional tint color for icon and text */
    color: Color = MaterialTheme.colorScheme.onSurface,
    /** Click handler */
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(title, fontWeight = FontWeight.Bold, color = color) },
        supportingContent = { Text(subtitle, style = MaterialTheme.typography.bodySmall) },
        leadingContent = { Icon(icon, contentDescription = null, tint = color) },
        modifier = Modifier.clickable { onClick() },
    )
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
}
