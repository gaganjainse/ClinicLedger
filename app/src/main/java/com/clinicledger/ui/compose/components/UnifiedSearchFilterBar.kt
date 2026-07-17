package com.clinicledger.ui.compose.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.clinicledger.R
import com.clinicledger.data.models.Village
import com.clinicledger.ui.util.LocaleManager

/**
 * Reusable Unified Search and Filter bar template.
 */
@Composable
fun UnifiedSearchFilterBar(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedVillageId: Long?,
    onVillageChange: (Long?) -> Unit,
    villages: List<Village>,
    sortOrder: String,
    onSortChange: (String) -> Unit,
    sortOptions: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    var showSortMenu by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Search Row
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 2.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.search_patients_placeholder)) },
                    leadingIcon = { Icon(Icons.Rounded.Search, null) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )

                Spacer(Modifier.width(12.dp))

                Box {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.AutoMirrored.Rounded.Sort, contentDescription = "Sort")
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        sortOptions.forEach { (id, labelRes) ->
                            DropdownMenuItem(
                                text = { Text(stringResource(labelRes)) },
                                onClick = {
                                    onSortChange(id)
                                    showSortMenu = false
                                },
                                trailingIcon = {
                                    if (sortOrder == id) Icon(Icons.Rounded.Check, null)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Village Filter Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedVillageId == null,
                onClick = { onVillageChange(null) },
                label = { Text(stringResource(R.string.all_villages)) },
                shape = RoundedCornerShape(12.dp)
            )
            villages.forEach { v ->
                FilterChip(
                    selected = selectedVillageId == v.id,
                    onClick = { onVillageChange(v.id) },
                    label = { Text(LocaleManager.getLocalizedVillage(v.name, v.nameHindi)) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}
