package com.clinicledger.ui.compose

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clinicledger.R
import com.clinicledger.data.util.DataSeeder
import com.clinicledger.ui.util.LocaleManager
import kotlinx.coroutines.launch

/**
 * Settings Screen Content.
 * Includes Agent Tuning and Database Management.
 */
@Composable
fun SettingsScreen(
    onToggleLanguage: () -> Unit,
    voiceViewModel: VoiceAssistantViewModel,
    onNavigateToDiagnostics: () -> Unit,
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Observe state from ViewModel
    val voiceSpeed by voiceViewModel.voiceSpeed.collectAsState()
    val activeLearningEnabled by voiceViewModel.activeLearningEnabled.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
    ) {
        // Section: General
        item {
            SettingsSectionHeader(stringResource(R.string.settings_section_general))
            SettingsItem(
                title = stringResource(R.string.settings_language),
                subtitle = if (LocaleManager.getSavedLocale(context) == "hi") "हिंदी (Hindi)" else "English",
                icon = Icons.Rounded.Language,
                onClick = onToggleLanguage
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
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Slower", style = MaterialTheme.typography.labelSmall)
                    Text("Normal (${String.format(java.util.Locale.US, "%.1f", voiceSpeed)}x)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text("Faster", style = MaterialTheme.typography.labelSmall)
                }
            }
            
            ListItem(
                headlineContent = { Text("Active Learning Mode", fontWeight = FontWeight.Bold) },
                supportingContent = { Text("Agent evolves skills based on your clinical workflow.") },
                leadingContent = { Icon(Icons.Rounded.AutoFixHigh, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = activeLearningEnabled, 
                        onCheckedChange = { 
                            voiceViewModel.setActiveLearningEnabled(it)
                        }
                    )
                }
            )
        }

        // Section: Database
        item {
            SettingsSectionHeader(stringResource(R.string.settings_section_database))
            SettingsItem(
                title = stringResource(R.string.settings_clear_data),
                subtitle = "Wipe all records (Permanent)",
                icon = Icons.Rounded.DeleteForever,
                color = MaterialTheme.colorScheme.error,
                onClick = {
                    scope.launch {
                        DataSeeder.clearAllData(context)
                        Toast.makeText(context, "Database cleared", Toast.LENGTH_SHORT).show()
                    }
                }
            )
            SettingsItem(
                title = stringResource(R.string.settings_reseed_demo),
                subtitle = "Load realistic medical data",
                icon = Icons.Rounded.RestartAlt,
                onClick = {
                    scope.launch {
                        DataSeeder.seedDemoDataForce(context)
                        Toast.makeText(context, "Demo data loaded", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        // Section: System
        item {
            SettingsSectionHeader(stringResource(R.string.settings_section_system))
            SettingsItem(
                title = stringResource(R.string.settings_system_health),
                subtitle = "Interactive Architecture Map",
                icon = Icons.Rounded.AccountTree,
                onClick = onNavigateToDiagnostics
            )
            SettingsItem(
                title = stringResource(R.string.settings_app_info),
                subtitle = "Clinic Ledger OS v2.0 (Ultra-Performance)",
                icon = Icons.Rounded.Info,
                onClick = onNavigateToAbout
            )
        }
        
        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp),
        letterSpacing = 1.sp
    )
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title, fontWeight = FontWeight.Bold, color = color) },
        supportingContent = { Text(subtitle, style = MaterialTheme.typography.bodySmall) },
        leadingContent = { Icon(icon, contentDescription = null, tint = color) },
        modifier = Modifier.clickable { onClick() }
    )
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
}
