package com.clinicledger.ui.compose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cabin
import androidx.compose.material.icons.rounded.LocalPharmacy
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.clinicledger.data.models.Transaction
import com.clinicledger.ui.util.LocaleManager

/**
 * A card representing a single ledger transaction (Medicine, Payment, or Adjustment).
 */
@Composable
fun ClinicTransactionItem(
    transaction: Transaction,
    patientName: String? = null,
    villageName: String? = null,
    isHindi: Boolean,
    onClick: () -> Unit
) {
    val accentColor = if (transaction.type == "payment") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val icon = if (transaction.type == "payment") Icons.Rounded.Payments else Icons.Rounded.LocalPharmacy
    val prefix = if (transaction.type == "payment") "-" else "+"

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.08f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                if (!patientName.isNullOrBlank()) {
                    Text(
                        text = LocaleManager.formatPatientName(patientName),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                if (!villageName.isNullOrBlank()) {
                    Text(
                        text = villageName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                val typeText = LocaleManager.getLocalizedTransactionType(transaction.type, isHindi)
                Text(
                    text = typeText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "$prefix${LocaleManager.formatCurrency(transaction.amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = accentColor
            )
        }
    }
}
