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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.clinicledger.R
import com.clinicledger.data.models.Patient
import com.clinicledger.domain.usecase.GetFamilyTreeUseCase
import com.clinicledger.ui.util.LocaleManager

/**
 * Modern hierarchical family tree visualization.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyTreeDialog(
    /** Surname or group identifier */
    familyName: String,
    /** List of all members in the family group */
    members: List<Patient>,
    /** Callback to close the dialog */
    onDismiss: () -> Unit,
    /** Navigation callback for specific members */
    onNavigateToPatient: (Long) -> Unit,
) {
    val isHindi = LocaleManager.LocalIsHindi.current
    val getFamilyTreeUseCase = remember { GetFamilyTreeUseCase() }
    val generations = remember(members) { getFamilyTreeUseCase(members) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val titleText = if (isHindi) {
                                "$familyName - वंशावली"
                            } else {
                                "$familyName Family Tree"
                            }
                            Text(
                                text = titleText,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = stringResource(R.string.family_members_count_title, members.size),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close, 
                                contentDescription = stringResource(R.string.cancel),
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (generations.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(), 
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = if (isHindi) "कोई सदस्य नहीं मिला" else "No members found",
                            )
                        }
                    } else {
                        generations.forEachIndexed { index, gen ->
                            GenerationTier(
                                title = stringResource(gen.titleRes),
                                members = gen.members,
                                tierColor = when (gen.level) {
                                    1 -> Color(0xFF1976D2)
                                    2 -> Color(0xFF388E3C)
                                    3 -> Color(0xFFF57C00)
                                    else -> Color(0xFF7B1FA2)
                                },
                            ) { member ->
                                onDismiss()
                                onNavigateToPatient(member.id)
                            }
                            if (index < (generations.size - 1)) {
                                TierConnector()
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * A horizontal row of family members of the same generation level.
 */
@Composable
fun GenerationTier(
    /** Tier identifier (e.g. Generation 1) */
    title: String,
    /** Members in this tier */
    members: List<Patient>,
    /** Visual theme color for this generation */
    tierColor: Color,
    /** Click handler */
    onMemberClick: (Patient) -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = tierColor.copy(alpha = 0.1f),
            modifier = Modifier.padding(bottom = 12.dp),
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = tierColor,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                letterSpacing = 1.sp,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            members.forEachIndexed { index, member ->
                TreeMemberItem(member = member, color = tierColor) {
                    onMemberClick(member)
                }
                if (index < (members.size - 1)) {
                    Spacer(modifier = Modifier.width(16.dp))
                }
            }
        }
    }
}

/**
 * Individual member card within the tree layout.
 */
@Composable
fun TreeMemberItem(
    /** Patient data */
    member: Patient,
    /** Visual theme color */
    color: Color,
    /** Click handler */
    onClick: () -> Unit,
) {
    val name = LocaleManager.formatPatientName(member.name)
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(color, color.copy(alpha = 0.6f)),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Person, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = LocaleManager.formatCurrency(member.currentBalance),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = if (member.currentBalance > 0) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
            )
        }
    }
}

/**
 * Visual vertical connector between generation tiers.
 */
@Composable
fun TierConnector() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.height(40.dp)) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.outlineVariant),
        )
    }
}
