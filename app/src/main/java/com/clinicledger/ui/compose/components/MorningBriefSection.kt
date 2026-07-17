package com.clinicledger.ui.compose.components

import androidx.compose.animation.*
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.clinicledger.R
import com.clinicledger.data.models.FamilyGroup
import com.clinicledger.data.models.Patient
import com.clinicledger.ui.util.LocaleManager
import com.clinicledger.ui.util.LocaleManager.LocalIsHindi
import java.util.*

/**
 * Diagnostic and summary card for the start of the day.
 */
@Composable
fun MorningBriefSection(
    /** Data set */
    allPatients: List<Patient>,
    /** Data set */
    familyGroups: List<FamilyGroup>,
    /** recovery total */
    totalCollectedToday: Long,
) {
    var isExpanded by remember { mutableStateOf(value = false) }
    val isHindi = LocalIsHindi.current

    val calendar = Calendar.getInstance()
    val dayEng = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US)
    val dayHindi = when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.SUNDAY -> stringResource(R.string.day_sunday)
        Calendar.MONDAY -> stringResource(R.string.day_monday)
        Calendar.TUESDAY -> stringResource(R.string.day_tuesday)
        Calendar.WEDNESDAY -> stringResource(R.string.day_wednesday)
        Calendar.THURSDAY -> stringResource(R.string.day_thursday)
        Calendar.FRIDAY -> stringResource(R.string.day_friday)
        else -> stringResource(R.string.day_saturday)
    }

    val totalOutstanding = allPatients.asSequence()
        .filter { it.currentBalance > 0 }
        .sumOf { it.currentBalance }
    val activeFamiliesCount = familyGroups.size
    val highestDuePatient = allPatients.maxByOrNull { it.currentBalance }
    val unlinkedPatientsCount = allPatients.count { it.familyGroupId == null }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                        modifier = Modifier.size(40.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.LightMode, 
                                contentDescription = null, 
                                tint = MaterialTheme.colorScheme.tertiary,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.welcome_doctor_msg),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(
                                R.string.today_snapshots_msg, 
                                if (isHindi) dayHindi else (dayEng ?: ""),
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                    }
                }
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) {
                            Icons.Rounded.ExpandLess
                        } else {
                            Icons.Rounded.ExpandMore
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.1f))
                    
                    BriefItem(
                        icon = Icons.Rounded.CurrencyRupee,
                        label = stringResource(R.string.recovery_today_title),
                        value = stringResource(
                            R.string.recovery_today_val, 
                            LocaleManager.formatCurrency(totalCollectedToday),
                        ),
                    )

                    BriefItem(
                        icon = Icons.Rounded.AccountBalance,
                        label = stringResource(R.string.outstanding_ledger_title),
                        value = stringResource(
                            R.string.outstanding_ledger_val, 
                            LocaleManager.formatCurrency(totalOutstanding), 
                            activeFamiliesCount,
                        ),
                    )

                    if ((highestDuePatient != null) && (highestDuePatient.currentBalance > 0)) {
                        BriefItem(
                            icon = Icons.Rounded.Error,
                            label = stringResource(R.string.highest_pending_title),
                            value = "${highestDuePatient.name} " +
                                "(${LocaleManager.formatCurrency(highestDuePatient.currentBalance)})",
                            isError = true,
                        )
                    }

                    if (unlinkedPatientsCount > 0) {
                        BriefItem(
                            icon = Icons.Rounded.LinkOff,
                            label = stringResource(R.string.unlinked_memories_title),
                            value = stringResource(
                                R.string.unlinked_warning_format, 
                                unlinkedPatientsCount,
                            ),
                        )
                    }
                }
            }

            if (!isExpanded) {
                Text(
                    text = stringResource(R.string.tap_expand_msg),
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f),
                )
            }
        }
    }
}

@Composable
private fun BriefItem(
    /** Icon */
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    /** label text */
    label: String,
    /** val text */
    value: String,
    /** error color flag */
    isError: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon, 
            contentDescription = null, 
            modifier = Modifier.size(16.dp),
            tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                label, 
                style = MaterialTheme.typography.labelSmall, 
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
            )
            Text(
                value, 
                style = MaterialTheme.typography.bodySmall, 
                fontWeight = FontWeight.Bold,
                color = if (isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onTertiaryContainer
                },
            )
        }
    }
}
