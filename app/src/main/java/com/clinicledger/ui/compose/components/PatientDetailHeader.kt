package com.clinicledger.ui.compose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clinicledger.R
import com.clinicledger.data.models.Alias
import com.clinicledger.data.models.Patient
import com.clinicledger.service.PhotoStorageService
import com.clinicledger.ui.util.LocaleManager

/**
 * Header component for the patient detail screen.
 * Displays photo, name, village, and quick actions like family tree or alias addition.
 */
@Composable
fun PatientDetailHeader(
    /** The patient model */
    patient: Patient,
    /** Resolved village name */
    villageName: String,
    /** List of aliases */
    aliases: List<Alias>,
    /** Callback for adding an alias */
    onAddAlias: () -> Unit,
    /** Callback for deleting an alias */
    onDeleteAlias: (Alias) -> Unit,
    /** Callback for family tree view */
    onShowFamilyTree: () -> Unit,
    /** Callback for capturing a photo */
    onCapturePhoto: () -> Unit,
    /** Whether the patient belongs to a family */
    hasFamily: Boolean,
) {
    val context = LocalContext.current
    val photoStorage = remember { PhotoStorageService(context) }
    val photo = remember(patient.photoPath) { 
        patient.photoPath?.let { photoStorage.loadPhoto(it) } 
    }
    val isHindi = LocaleManager.LocalIsHindi.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Photo Section
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onCapturePhoto() },
            contentAlignment = Alignment.Center,
        ) {
            if (photo != null) {
                Image(
                    bitmap = photo.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
            }
            
            // Camera Overlay Icon
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(32.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                tonalElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Name & Village
        Text(
            text = LocaleManager.formatPatientName(patient.name),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Text(
            text = villageName,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Relationship Role Tag
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        ) {
            Text(
                text = patient.relationship,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (hasFamily) {
                AssistChip(
                    onClick = onShowFamilyTree,
                    label = { 
                        Text(
                            text = if (isHindi) "वंशावली" else "Family Tree",
                        ) 
                    },
                    leadingIcon = { 
                        Icon(
                            imageVector = Icons.Rounded.AccountTree, 
                            contentDescription = null, 
                            modifier = Modifier.size(16.dp),
                        ) 
                    },
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            aliases.forEach { alias ->
                InputChip(
                    selected = false,
                    onClick = { onDeleteAlias(alias) },
                    label = { Text(alias.alias) },
                    trailingIcon = { 
                        Icon(
                            imageVector = Icons.Rounded.Close, 
                            contentDescription = stringResource(R.string.delete), 
                            modifier = Modifier.size(14.dp),
                        ) 
                    },
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            IconButton(onClick = onAddAlias) {
                Icon(
                    imageVector = Icons.Rounded.AddCircle, 
                    contentDescription = stringResource(R.string.add_alias), 
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

/**
 * Prominent card displaying the current outstanding balance.
 */
@Composable
fun BalanceCard(/** Balance in Paise */ balance: Long) {
    val isDebt = balance > 0
    val isHindi = LocaleManager.LocalIsHindi.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDebt) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
            },
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.total_outstanding_balance_label).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = if (isDebt) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                },
            )
            Text(
                text = LocaleManager.formatCurrency(balance),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = if (isDebt) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
            )
            
            val statusText = if (isDebt) {
                stringResource(R.string.payment_pending_msg)
            } else {
                stringResource(R.string.account_settled_msg)
            }
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (isDebt) {
                    MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                },
            )
        }
    }
}
