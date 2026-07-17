package com.clinicledger.ui.compose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.clinicledger.R
import com.clinicledger.ui.util.LocaleManager.LocalIsHindi

/**
 * Standard TopAppBar for the Clinic Ledger app.
 * Displays the app icon, a title (localized), and optional navigation/action icons.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicTopAppBar(
    /** English primary title */
    title: String,
    /** Optional Hindi primary title */
    titleHindi: String? = null,
    /** Optional supporting subtitle */
    subtitle: String? = null,
    /** Optional leading navigation icon */
    navigationIcon: @Composable (() -> Unit)? = null,
    /** Optional trailing action buttons */
    actions: @Composable RowScope.() -> Unit = {},
    /** Visual theme overrides */
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.background,
        titleContentColor = MaterialTheme.colorScheme.primary,
    ),
) {
    val isHindi = LocalIsHindi.current
    val displayTitle = if (isHindi && !titleHindi.isNullOrBlank()) titleHindi else title

    CenterAlignedTopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.app_icon),
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = displayTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        },
        navigationIcon = navigationIcon ?: {},
        actions = actions,
        colors = colors,
        // Reduced window insets to prevent excessive top white space
        windowInsets = WindowInsets.statusBars.only(
            WindowInsetsSides.Horizontal + WindowInsetsSides.Top,
        ),
    )
}

/**
 * Standard back button icon for top bars.
 */
@Composable
fun BackNavigationIcon(/** Click handler */ onBack: () -> Unit) {
    IconButton(onClick = onBack) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = stringResource(R.string.cancel),
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

/**
 * Standard menu (hamburger) icon for top bars.
 */
@Composable
fun MenuNavigationIcon(/** Click handler */ onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Rounded.Menu,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

/**
 * Higher-level Scaffold wrapper that pre-configures the ClinicTopAppBar.
 */
@Composable
fun ClinicScaffold(
    /** Screen title */
    title: String,
    /** Localized title */
    titleHindi: String? = null,
    /** Sub-title string */
    subtitle: String? = null,
    /** Back button handler */
    onBack: (() -> Unit)? = null,
    /** Side menu handler */
    onMenu: (() -> Unit)? = null,
    /** Top bar actions */
    actions: @Composable RowScope.() -> Unit = {},
    /** Action button */
    floatingActionButton: @Composable () -> Unit = {},
    /** Main screen content */
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        topBar = {
            ClinicTopAppBar(
                title = title,
                titleHindi = titleHindi,
                subtitle = subtitle,
                navigationIcon = when {
                    onBack != null -> {
                        { BackNavigationIcon(onBack) }
                    }
                    onMenu != null -> {
                        { MenuNavigationIcon(onMenu) }
                    }
                    else -> null
                },
                actions = actions,
            )
        },
        floatingActionButton = floatingActionButton,
        content = content,
    )
}

/**
 * Full-screen loading indicator centered on the page.
 */
@Composable
fun ClinicLoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

/**
 * Displays a placeholder message when a list is empty or search yields no results.
 */
@Composable
fun ClinicEmptyState(
    /** English message */
    message: String,
    /** Optional Hindi message */
    messageHindi: String? = null,
) {
    val isHindi = LocalIsHindi.current
    val displayMessage = if (isHindi && !messageHindi.isNullOrBlank()) messageHindi else message
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(
            text = displayMessage,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}
