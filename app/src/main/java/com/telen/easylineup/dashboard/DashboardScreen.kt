/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.dashboard

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.telen.easylineup.R
import com.telen.easylineup.compose.DugoutAvatar
import com.telen.easylineup.compose.DugoutPreviews
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.compose.dashedBorder
import com.telen.easylineup.domain.model.ShirtNumberEntry
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.utils.titleRes
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dashboardCardShape = RoundedCornerShape(16.dp)

/**
 * The one card look every box on this screen shares (hero, mini-tiles, lookup row) - same
 * border/radius/background everywhere, unlike the shared [com.telen.easylineup.compose.DugoutCard]
 * which bakes in its own bottom margin (fine for stacked screens, but fights this screen's own
 * explicit, uniform Arrangement.spacedBy gaps).
 */
private fun Modifier.dashboardCard(borderColor: Color, background: Color): Modifier = this
    .clip(dashboardCardShape)
    .background(background)
    .border(BorderStroke(1.dp, borderColor), dashboardCardShape)

data class MostUsedPlayerUi(val name: String, val shirtNumber: Int, val gamesPlayed: Int)

data class LastLineupUi(
    val lineupId: Long,
    val name: String,
    val eventTimeInMillis: Long,
    val tournamentName: String?
)

data class DashboardState(
    val teamName: String,
    val teamImageUri: Uri?,
    val teamSize: Int?,
    val teamType: TeamType,
    val mostUsedPlayer: MostUsedPlayerUi?,
    val lastLineup: LastLineupUi?,
    val shirtNumberQuery: String,
    val shirtNumberResult: String?,
    val shirtNumberHistory: List<ShirtNumberEntry>
)

private fun formatLineupDate(eventTimeInMillis: Long, pattern: String): String? =
    eventTimeInMillis.takeIf { it > 0 }
        ?.let { SimpleDateFormat(pattern, Locale.getDefault()).format(Date(it)) }

@Composable
fun DashboardScreen(
    state: DashboardState,
    onSwitchTeamClick: () -> Unit,
    onRosterClick: () -> Unit,
    onLastLineupClick: (Long) -> Unit,
    onShirtNumberQueryChanged: (String) -> Unit,
    onShirtNumberSearch: () -> Unit,
    onShirtNumberHistoryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TeamSwitchHeader(
            teamName = state.teamName,
            teamImageUri = state.teamImageUri,
            onClick = onSwitchTeamClick
        )

        HeroLastLineupCard(lastLineup = state.lastLineup, onClick = onLastLineupClick)

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MiniStatCell(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.dashboard_mini_roster_size_label),
                value = state.teamSize?.toString() ?: "—",
                footer = if (state.teamType.titleRes() != 0) {
                    stringResource(state.teamType.titleRes())
                } else {
                    null
                },
                onClick = onRosterClick
            )
            MiniStatCell(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.dashboard_mini_most_used_label),
                value = state.mostUsedPlayer?.let {
                    "#${it.shirtNumber.toString().padStart(2, '0')}"
                } ?: "—",
                footer = state.mostUsedPlayer?.name
            )
        }

        ShirtNumberLookupRow(
            query = state.shirtNumberQuery,
            result = state.shirtNumberResult,
            history = state.shirtNumberHistory,
            onQueryChanged = onShirtNumberQueryChanged,
            onSearch = onShirtNumberSearch,
            onHistoryClick = onShirtNumberHistoryClick
        )
    }
}

/**
 * The mockup's own app bar (team avatar + name + chevron, tappable to switch team) - built now
 * as ordinary screen content, coexisting with the real drawer/toolbar that still owns team
 * switching today. Once the drawer is retired (a later, separate pass), this row is already
 * done and just needs to move up into the real app bar.
 */
@Composable
private fun TeamSwitchHeader(teamName: String, teamImageUri: Uri?, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        DugoutAvatar(name = teamName, imageUri = teamImageUri, size = 32.dp)
        Text(
            text = teamName,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1
        )
        Icon(
            painter = painterResource(R.drawable.ic_arrow_right_white_24dp),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(16.dp)
                .graphicsLayer(rotationZ = 90f)
        )
    }
}

@Composable
private fun HeroLastLineupCard(lastLineup: LastLineupUi?, onClick: (Long) -> Unit) {
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    val background = MaterialTheme.colorScheme.surface

    if (lastLineup == null) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .dashboardCard(borderColor, background)
                .padding(20.dp)
        ) {
            Text(
                text = stringResource(R.string.dashboard_hero_empty_title),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }

    val date = formatLineupDate(lastLineup.eventTimeInMillis, "MMM d, yyyy")
    val subtitle = listOfNotNull(date, lastLineup.tournamentName).joinToString(" · ")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .dashboardCard(borderColor, background)
            .clickable { onClick(lastLineup.lineupId) }
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.tile_last_lineup_header).uppercase(),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = lastLineup.name,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge
        )
        if (subtitle.isNotEmpty()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun MiniStatCell(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    footer: String?,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .dashboardCard(
                MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                MaterialTheme.colorScheme.surface
            )
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            )
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Text(
            text = label.uppercase(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.displaySmall,
            fontFamily = FontFamily.Monospace
        )
        if (footer != null) {
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = footer,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ShirtNumberLookupRow(
    query: String,
    result: String?,
    history: List<ShirtNumberEntry>,
    onQueryChanged: (String) -> Unit,
    onSearch: () -> Unit,
    onHistoryClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .dashboardCard(
                MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                MaterialTheme.colorScheme.surface
            )
            .clickable(enabled = history.isNotEmpty(), onClick = onHistoryClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ShirtNumberInputBox(query = query, onQueryChanged = onQueryChanged, onSearch = onSearch)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.dashboard_lookup_hint).uppercase(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium
            )
            if (result != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = result,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }

        if (history.isNotEmpty()) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_right_white_24dp),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ShirtNumberInputBox(
    query: String,
    onQueryChanged: (String) -> Unit,
    onSearch: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .dashedBorder(MaterialTheme.colorScheme.outline.copy(alpha = 0.8f), 12.dp),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = query,
            onValueChange = onQueryChanged,
            singleLine = true,
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                textAlign = TextAlign.Center
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onSearch() }),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ShirtNumberHistoryDialog(history: List<ShirtNumberEntry>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.ok))
            }
        },
        title = { Text(stringResource(R.string.tile_last_player_number_message)) },
        text = {
            Column {
                history.forEach { entry ->
                    val dateInMillis = entry.eventTime.takeIf { it > 0 } ?: entry.createdAt
                    val date = DateFormat.getDateInstance().format(Date(dateInMillis))
                    Text(
                        text = "${entry.playerName} | $date | ${entry.lineupName}",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    )
}

@DugoutPreviews
@Composable
private fun DashboardScreenPreview() {
    DugoutTheme {
        DashboardScreen(
            state = DashboardState(
                teamName = "Riverside Hawks",
                teamImageUri = null,
                teamSize = 14,
                teamType = TeamType.BASEBALL,
                mostUsedPlayer = MostUsedPlayerUi("Jonas Alvarez", 7, 15),
                lastLineup = LastLineupUi(
                    1L,
                    "Fall Classic — Game 3",
                    1760000000000L,
                    "Fall Classic"
                ),
                shirtNumberQuery = "07",
                shirtNumberResult = "Jonas Alvarez",
                shirtNumberHistory = emptyList()
            ),
            onSwitchTeamClick = {},
            onRosterClick = {},
            onLastLineupClick = {},
            onShirtNumberQueryChanged = {},
            onShirtNumberSearch = {},
            onShirtNumberHistoryClick = {}
        )
    }
}

@DugoutPreviews
@Composable
private fun DashboardScreenEmptyPreview() {
    DugoutTheme {
        DashboardScreen(
            state = DashboardState(
                teamName = "Riverside Storm",
                teamImageUri = null,
                teamSize = 3,
                teamType = TeamType.SOFTBALL,
                mostUsedPlayer = null,
                lastLineup = null,
                shirtNumberQuery = "",
                shirtNumberResult = null,
                shirtNumberHistory = emptyList()
            ),
            onSwitchTeamClick = {},
            onRosterClick = {},
            onLastLineupClick = {},
            onShirtNumberQueryChanged = {},
            onShirtNumberSearch = {},
            onShirtNumberHistoryClick = {}
        )
    }
}
