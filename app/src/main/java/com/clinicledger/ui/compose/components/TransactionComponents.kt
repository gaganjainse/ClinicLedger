package com.clinicledger.ui.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.clinicledger.data.models.Transaction
import com.clinicledger.ui.util.LocaleManager

/**
 * Standard list item for displaying a single transaction entry.
 */
@Composable
fun ClinicTransactionItem(
    /** Transaction model */
    transaction: Transaction,
    /** Patient display name */
    patientName: String? = null,
    /** Village name */
    villageName: String? = null,
    /** Current locale */
    isHindi: Boolean,
    /** Click handler */
    onClick: () -> Unit,
) {
    val accentColor = if (transaction.type == "payment") {
        MaterialTheme.colorScheme.primary 
    } else {
        MaterialTheme.colorScheme.error
    }
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.1f),
                modifier = Modifier.size(40.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (transaction.type == "payment") {
                            Icons.Rounded.Payments 
                        } else {
                            Icons.Rounded.MedicalServices
                        },
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                if (patientName != null) {
                    Text(
                        text = LocaleManager.getLocalizedText(patientName), 
                        style = MaterialTheme.typography.bodyLarge, 
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(
                    text = LocaleManager.getLocalizedTransactionType(transaction.type, isHindi),
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (transaction.notes.isNotBlank()) {
                    Text(
                        text = transaction.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = LocaleManager.formatCurrency(transaction.amount),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Black,
                    color = accentColor,
                )
                Text(
                    text = LocaleManager.formatDateTime(transaction.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}
