package com.clinicledger.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.clinicledger.BuildConfig
import com.clinicledger.R
import com.clinicledger.ui.compose.components.ClinicScaffold

/**
 * About screen displaying application metadata and version info.
 */
@Suppress("HardcodedStringLiteral")
@Composable
fun AboutScreen(
    /** Callback to return to previous screen */
    onNavigateBack: () -> Unit,
) {
    ClinicScaffold(
        title = stringResource(R.string.settings_app_info),
        onBack = onNavigateBack,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            Image(
                painter = painterResource(id = R.drawable.app_icon),
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
            )
            
            Text(
                text = stringResource(R.string.precision_version_desc),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    MetadataRow("Target SDK", "37")
                    MetadataRow("Build Engine", "AGP 9.3.0")
                    MetadataRow("Kotlin", "2.4.10")
                    MetadataRow("State", "Stable / Verified")
                    MetadataRow("Version", "v${BuildConfig.VERSION_NAME}")
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = "© 2026 Clinic Ledger Systems",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MetadataRow(/** Label */ label: String, /** Value */ value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label, 
            style = MaterialTheme.typography.bodySmall, 
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value, 
            style = MaterialTheme.typography.bodySmall, 
            fontWeight = FontWeight.Bold,
        )
    }
}
