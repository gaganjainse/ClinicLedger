package com.clinicledger.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.clinicledger.data.models.Patient
import com.clinicledger.domain.usecase.GetFamilyTreeUseCase
import com.clinicledger.ui.util.LocaleManager

/**
 * Dialog displaying a hierarchical family tree for a group.
 * Uses a vertical generation-based layout with connecting lines.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyTreeDialog(
    familyName: String,
    members: List<Patient>,
    onDismiss: () -> Unit,
    onNavigateToPatient: (Long) -> Unit
) {
    val isHindi = LocaleManager.LocalIsHindi.current
    val getFamilyTreeUseCase = remember { GetFamilyTreeUseCase() }
    
    // Process flat member list into hierarchical generation tiers
    val generations = remember(members) { getFamilyTreeUseCase(members) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Toolbar
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isHindi) "$familyName - वंशावली" else "$familyName Family Tree",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = if (isHindi) "कुल सदस्य: ${members.size}" else "Total Members: ${members.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Scrollable Tree Layout
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    
                    Text(
                        text = if (isHindi) "पारस्परिक संबंधों को देखने के लिए किसी भी कार्ड पर टैप करें।" else "Tap any member card to view their medical profile ledger.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (generations.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isHindi) "कोई सदस्य नहीं मिला" else "No family members listed",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // Render each generation tier
                        generations.forEachIndexed { index, gen ->
                            GenerationLayer(
                                title = if (isHindi) gen.titleHindi else gen.title,
                                badgeColor = when(gen.level) {
                                    1 -> MaterialTheme.colorScheme.primary
                                    2 -> MaterialTheme.colorScheme.secondary
                                    3 -> MaterialTheme.colorScheme.tertiary
                                    else -> MaterialTheme.colorScheme.error
                                },
                                members = gen.members,
                                onMemberClick = {
                                    onDismiss()
                                    onNavigateToPatient(it.id)
                                }
                            )
                            // Draw connecting stem between tiers
                            if (index < generations.size - 1) {
                                ConnectorLine()
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * A horizontal row containing members of a single generation.
 */
@Composable
fun GenerationLayer(
    title: String,
    badgeColor: Color,
    members: List<Patient>,
    onMemberClick: (Patient) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Generation identifier badge
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = badgeColor.copy(alpha = 0.15f),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = badgeColor,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }

        // Horizontal scrollable list of members
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            members.forEachIndexed { index, member ->
                FamilyMemberCard(member = member, onClick = { onMemberClick(member) })
                if (index < members.size - 1) {
                    // Visual branch between members of the same tier
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(2.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    )
                }
            }
        }
    }
}

/**
 * Individual member card within the tree.
 */
@Composable
fun FamilyMemberCard(
    member: Patient,
    onClick: () -> Unit
) {
    val localizedName = LocaleManager.formatPatientName(member.name)
    val initials = localizedName.split(" ").filter { it.isNotEmpty() }.take(2)
        .joinToString("") { it.take(1).uppercase() }

    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Text(
                text = localizedName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            val relDisplay = member.relationship.ifBlank { "Member" }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = LocaleManager.getLocalizedText(relDisplay),
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = LocaleManager.formatCurrency(member.currentBalance),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                color = if (member.currentBalance > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Vertical line connector between generations.
 */
@Composable
fun ConnectorLine() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .fillMaxHeight(0.7f)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        )
        Icon(
            imageVector = Icons.Rounded.KeyboardArrowDown,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            modifier = Modifier.size(16.dp)
        )
    }
}
