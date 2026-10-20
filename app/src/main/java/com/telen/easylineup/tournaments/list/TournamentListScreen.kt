/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.tournaments.list

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.telen.easylineup.R
import com.telen.easylineup.compose.DugoutCard
import com.telen.easylineup.compose.DugoutPreviews
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.compose.HeaderActionIconChip
import com.telen.easylineup.compose.ScreenHeader
import com.telen.easylineup.compose.dashedBorder
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.GeoLocation
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.model.getStrategies
import com.telen.easylineup.utils.getStrategiesDisplayName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TournamentListState(
    val teamName: String,
    val teamType: TeamType,
    val items: List<TournamentItem>,
    val mapsByTournamentId: Map<Long, GeoLocation>,
    val searchQuery: String
)

@Composable
fun TournamentListScreen(
    state: TournamentListState,
    onBackClick: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onCreateLineupClick: () -> Unit,
    onLineupClick: (Lineup) -> Unit,
    onEditLineupClick: (Lineup) -> Unit,
    onStatsClick: (Tournament) -> Unit,
    onDeleteClick: (Tournament) -> Unit,
    onMapClick: (GeoLocation) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            ScreenHeader(
                icon = R.drawable.ic_arrow_left_white_24dp,
                onIconClick = onBackClick,
                iconContentDescription = stringResource(R.string.generic_back),
                eyebrow = state.teamName,
                title = stringResource(R.string.tournaments_list_header_title),
                modifier = Modifier.padding(bottom = 14.dp)
            )

            TournamentsSearchField(
                value = state.searchQuery,
                onValueChange = onSearchQueryChanged,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (state.items.isEmpty()) {
                TournamentsEmptyState(isSearching = state.searchQuery.isNotBlank())
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    items(items = state.items, key = { it.tournament.id }) { item ->
                        TournamentTicketCard(
                            item = item,
                            teamType = state.teamType,
                            mapLocation = state.mapsByTournamentId[item.tournament.id],
                            onLineupClick = onLineupClick,
                            onEditLineupClick = onEditLineupClick,
                            onStatsClick = { onStatsClick(item.tournament) },
                            onDeleteClick = { onDeleteClick(item.tournament) },
                            onMapClick = onMapClick
                        )
                    }
                    item { Spacer(modifier = Modifier.height(72.dp)) }
                }
            }
        }

        FloatingActionButton(
            onClick = onCreateLineupClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add_white_24dp),
                contentDescription = stringResource(
                    R.string.tournaments_list_create_lineup_content_description
                )
            )
        }
    }
}

@Composable
private fun TournamentsSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(stringResource(R.string.tournaments_list_search_placeholder)) },
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_baseline_search_24),
                contentDescription = null
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
    )
}

@Composable
private fun TournamentsEmptyState(isSearching: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .dashedBorder(MaterialTheme.colorScheme.outline, cornerRadius = 16.dp)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(
                if (isSearching) {
                    R.string.tournaments_list_empty_search_title
                } else {
                    R.string.tournaments_list_empty_title
                }
            ),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleSmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(
                if (isSearching) {
                    R.string.tournaments_list_empty_search_subtitle
                } else {
                    R.string.tournaments_list_empty_subtitle
                }
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun TournamentTicketCard(
    item: TournamentItem,
    teamType: TeamType,
    mapLocation: GeoLocation?,
    onLineupClick: (Lineup) -> Unit,
    onEditLineupClick: (Lineup) -> Unit,
    onStatsClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMapClick: (GeoLocation) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showAllLineups by remember { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "chevron-rotation"
    )

    DugoutCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TicketDateChip(item.getStart())

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.tournament.name,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = pluralStringResource(
                        R.plurals.tournaments_list_lineups_count,
                        item.lineups.size,
                        item.lineups.size
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                item.tournament.address?.let { address ->
                    Row(
                        modifier = Modifier
                            .clickable(enabled = mapLocation != null) {
                                mapLocation?.let(onMapClick)
                            }
                            .padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_place_outline_24dp),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = address,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            HeaderActionIconChip(
                icon = R.drawable.ic_show_chart_outline_24dp,
                contentDescription = stringResource(
                    R.string.tournaments_list_stats_content_description
                ),
                onClick = onStatsClick
            )
            HeaderActionIconChip(
                icon = R.drawable.ic_delete_outline_24dp,
                contentDescription = stringResource(R.string.action_delete),
                onClick = onDeleteClick
            )
            Icon(
                painter = painterResource(R.drawable.ic_chevron_down_24dp),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(16.dp)
                    .rotate(chevronRotation)
            )
        }

        if (expanded) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            val visibleLineups = if (showAllLineups || item.lineups.size <= 2) {
                item.lineups
            } else {
                item.lineups.take(2)
            }
            visibleLineups.forEach { lineup ->
                LineupRow(
                    lineup = lineup,
                    teamType = teamType,
                    onClick = { onLineupClick(lineup) },
                    onEditClick = { onEditLineupClick(lineup) }
                )
            }
            if (!showAllLineups && item.lineups.size > 2) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAllLineups = true }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = pluralStringResource(
                            R.plurals.tournaments_list_show_more,
                            item.lineups.size - 2,
                            item.lineups.size - 2
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_chevron_down_24dp),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TicketDateChip(startMillis: Long?) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (startMillis != null) {
            val date = Date(startMillis)
            Text(
                text = SimpleDateFormat("d", Locale.getDefault()).format(date),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = SimpleDateFormat("MMM", Locale.getDefault()).format(date).uppercase(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall
            )
        } else {
            Text(
                text = "—",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun LineupRow(
    lineup: Lineup,
    teamType: TeamType,
    onClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = lineup.name,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val eventDate = lineup.eventTimeInMillis.takeIf { it > 0L }
                    ?: lineup.createdTimeInMillis
                Text(
                    text = SimpleDateFormat("MMM d", Locale.getDefault())
                        .format(Date(eventDate)),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
                val context = LocalContext.current
                val strategyLabel = teamType.getStrategiesDisplayName(context)?.let { array ->
                    val strategies = teamType.getStrategies()
                    val strategy = TeamStrategy.getStrategyById(lineup.strategy)
                    strategies.indexOf(strategy).takeIf { it != -1 }?.let { array[it] }
                }
                strategyLabel?.let { LineupBadge(text = it) }
                lineup.extraHitters.takeIf { it > 0 }?.let { extraHitters ->
                    val value = if (extraHitters <= Constants.MAX_EXTRA_HITTERS_BEFORE_UNLIMITED) {
                        extraHitters.toString()
                    } else {
                        stringResource(R.string.generic_infinite)
                    }
                    LineupBadge(text = "+$value")
                }
            }
        }
        HeaderActionIconChip(
            icon = R.drawable.ic_edit_outline_24dp,
            contentDescription = stringResource(R.string.action_edit),
            onClick = onEditClick
        )
    }
}

@Composable
private fun LineupBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@DugoutPreviews
@Composable
private fun TournamentListScreenPreview() {
    DugoutTheme {
        TournamentListScreen(
            state = TournamentListState(
                teamName = "Riverside Hawks",
                teamType = TeamType.BASEBALL,
                items = listOf(
                    TournamentItem(
                        tournament = Tournament(
                            id = 1L,
                            name = "Fall Classic",
                            createdAt = 1760000000000L,
                            startTime = 1760000000000L,
                            endTime = 1760003600000L,
                            address = "Riverside Park"
                        ),
                        lineups = listOf(
                            Lineup(
                                id = 1L,
                                name = "Game 3 — Starters",
                                eventTimeInMillis = 1760000000000L,
                                extraHitters = 1
                            ),
                            Lineup(
                                id = 2L,
                                name = "Game 2 — Bench heavy",
                                eventTimeInMillis = 1759000000000L,
                                strategy = TeamStrategy.FIVE_MAN_STANDARD.id,
                                extraHitters = 99
                            )
                        )
                    ),
                    TournamentItem(
                        tournament = Tournament(
                            id = 2L,
                            name = "Elm Street Invitational",
                            createdAt = 1758000000000L,
                            startTime = 1758000000000L,
                            endTime = 1758003600000L,
                            address = null
                        ),
                        lineups = emptyList()
                    )
                ),
                mapsByTournamentId = mapOf(1L to GeoLocation(41.0, -74.0)),
                searchQuery = ""
            ),
            onBackClick = {},
            onSearchQueryChanged = {},
            onCreateLineupClick = {},
            onLineupClick = {},
            onEditLineupClick = {},
            onStatsClick = {},
            onDeleteClick = {},
            onMapClick = {}
        )
    }
}

@DugoutPreviews
@Composable
private fun TournamentListScreenEmptyPreview() {
    DugoutTheme {
        TournamentListScreen(
            state = TournamentListState(
                teamName = "Riverside Hawks",
                teamType = TeamType.BASEBALL,
                items = emptyList(),
                mapsByTournamentId = emptyMap(),
                searchQuery = ""
            ),
            onBackClick = {},
            onSearchQueryChanged = {},
            onCreateLineupClick = {},
            onLineupClick = {},
            onEditLineupClick = {},
            onStatsClick = {},
            onDeleteClick = {},
            onMapClick = {}
        )
    }
}
