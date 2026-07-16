package com.clinicledger.ui.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clinicledger.data.models.Alias
import com.clinicledger.data.models.Patient
import com.clinicledger.ui.util.LocaleManager
import com.clinicledger.ui.util.LocaleManager.LocalIsHindi

@Composable
fun PatientDetailHeader(
    patient: Patient,
    villageName: String,
    aliases: List<Alias>,
    onAddAlias: () -> Unit,
    onDeleteAlias: (Alias) -> Unit,
    onShowFamilyTree: () -> Unit,
    hasFamily: Boolean
) {
    val isHindi = LocalIsHindi.current
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(bottom = 16.dp)
    ) {
        // Bio Header with Avatar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val initials = patient.name.split(" ")
                        .asSequence()
                        .filter { it.isNotEmpty() }
                        .take(2)
                        .joinToString("") { it.take(1).uppercase() }
                    Text(
                        text = initials.ifBlank { "?" },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = LocaleManager.formatPatientName(patient.name),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = villageName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (patient.phone.isNotBlank()) {
                    Text(
                        text = patient.phone,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Action Quick Bar (Aliases & Family)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Family Tree Button
            if (hasFamily) {
                AssistChip(
                    onClick = onShowFamilyTree,
                    label = { Text(if (isHindi) "वंशावली" else "Family Tree") },
                    leadingIcon = { Icon(Icons.Rounded.AccountTree, null, modifier = Modifier.size(16.dp)) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                )
            }

            // Aliases
            aliases.forEach { alias ->
                InputChip(
                    selected = true,
                    onClick = {},
                    label = { Text(alias.alias) },
                    trailingIcon = {
                        Icon(
                            Icons.Rounded.Cancel,
                            "Remove",
                            Modifier.size(16.dp).clickable { onDeleteAlias(alias) }
                        )
                    }
                )
            }
            
            IconButton(onClick = onAddAlias, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Rounded.AddCircle, contentDescription = "Add Alias", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun BalanceCard(balance: Double) {
    val isHindi = LocalIsHindi.current
    val isDebt = balance > 0
    val cardColor = if (isDebt) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
    val contentColor = if (isDebt) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(28.dp),
        color = cardColor,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = (if (isHindi) "कुल बकाया राशि" else "Total Outstanding Balance").uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                color = contentColor.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = LocaleManager.formatCurrency(balance),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = contentColor
            )
            
            val statusText = if (isDebt) (if (isHindi) "भुगतान लंबित है" else "Payment Pending") else (if (isHindi) "खाता साफ़ है" else "Account Settled")
            Surface(
                color = contentColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Text(
                    text = statusText,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }
        }
    }
}
