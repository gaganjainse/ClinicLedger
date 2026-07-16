package com.clinicledger.ui.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LocalPharmacy
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.clinicledger.data.models.Transaction
import com.clinicledger.ui.util.LocaleManager
import com.clinicledger.ui.util.LocaleManager.LocalIsHindi

/**
 * Displays an individual transaction in a vertical timeline.
 * Differentiates between medicine charges and payments with icons and colors.
 */
@Composable
fun TransactionTimelineItem(transaction: Transaction) {
    val isHindi = LocalIsHindi.current
    val isPayment = transaction.type == "payment"
    val accentColor = if (isPayment) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val icon = if (isPayment) Icons.Rounded.Payments else Icons.Rounded.LocalPharmacy
    val prefix = if (isPayment) "-" else "+"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Timeline stem and dot
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(60.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    val formattedDate = LocaleManager.formatDateTime(transaction.createdAt)
                    Text(
                        text = LocaleManager.getLocalizedTransactionType(transaction.type, isHindi),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    if (transaction.notes.isNotBlank()) {
                        Text(
                            text = transaction.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Text(
                    text = "$prefix${LocaleManager.formatCurrency(kotlin.math.abs(transaction.amount))}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = accentColor
                )
            }
        }
    }
}

/**
 * Header text for the transaction history section.
 */
@Composable
fun TransactionHistoryHeader() {
    val isHindi = LocalIsHindi.current
    Text(
        text = if (isHindi) "लेनदेन का इतिहास" else "Transaction History",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

/**
 * Placeholder view for when a patient has no transactions recorded.
 */
@Composable
fun EmptyTransactionsView(isHindi: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Rounded.History, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (isHindi) "अभी तक कोई लेनदेन नहीं हुआ है।" else "No transactions recorded yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
