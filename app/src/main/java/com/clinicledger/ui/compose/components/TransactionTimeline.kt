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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.clinicledger.R
import com.clinicledger.data.models.Transaction
import com.clinicledger.ui.util.LocaleManager

/**
 * Visual timeline element representing a single clinical transaction.
 */
@Composable
fun TransactionTimelineItem(/** Transaction model */ transaction: Transaction) {
    val isPayment = transaction.type == "payment"
    val accentColor = if (isPayment) {
        MaterialTheme.colorScheme.primary 
    } else {
        MaterialTheme.colorScheme.error
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.Top,
    ) {
        // Timeline Dot & Line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(accentColor),
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(60.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Content Card
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val icon = if (isPayment) Icons.Rounded.Payments else Icons.Rounded.MedicalServices
                Icon(
                    imageVector = icon, 
                    contentDescription = null, 
                    tint = accentColor, 
                    modifier = Modifier.size(20.dp),
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isPayment) {
                            stringResource(R.string.payment_received_label)
                        } else {
                            stringResource(R.string.medicine_given_label)
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    if (transaction.notes.isNotBlank()) {
                        Text(
                            text = transaction.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = LocaleManager.formatDateTime(transaction.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
                
                Text(
                    text = LocaleManager.formatCurrency(transaction.amount),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Black,
                    color = accentColor,
                )
            }
        }
    }
}
