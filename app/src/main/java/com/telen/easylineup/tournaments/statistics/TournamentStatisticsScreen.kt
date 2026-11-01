/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.tournaments.statistics

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.telen.easylineup.R
import com.telen.easylineup.compose.DugoutAvatar
import com.telen.easylineup.compose.DugoutCard
import com.telen.easylineup.compose.DugoutPreviews
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.compose.PlayerPhotoTile
import com.telen.easylineup.compose.PositionUsageChart
import com.telen.easylineup.compose.PositionUsageEmptyState
import com.telen.easylineup.compose.ScreenHeader
import com.telen.easylineup.compose.SelectField
import com.telen.easylineup.compose.StatChip
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayingTimeSummary
import com.telen.easylineup.domain.model.TeamType
import kotlin.math.roundToInt

/**
 * One row's worth of UI-ready data - [com.telen.easylineup.domain.model.PlayerTournamentStats]
 * merged with the roster's own [com.telen.easylineup.domain.model.Player] for the photo/shirt
 * number the domain-layer stats alone don't carry. Rows arrive from the ViewModel already ranked
 * by playing time (most-played first) and flagged against the tournament's own average.
 */
data class PlayerStatsRow(
    val playerId: Long,
    val name: String,
    val shirtNumber: Int,
    val imageUri: Uri?,
    val totalGames: Int,
    val positionCounts: Map<FieldPosition, Int>,
    val isUnderused: Boolean = false
)

/**
 * The fairness-check screen a coach actually needs: total playing time per player, sorted so an
 * imbalance is visible at a glance, with per-position detail demoted to a one-tap drill-down -
 * replacing the old `tableMultiScroll` cross-tab that buried both behind a grid full of zeros.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentStatisticsScreen(state: TournamentStatisticsUiState, onBackClick: () -> Unit) {
    val playerStats = state.playerStats
    var selectedPlayerId by remember(playerStats) {
        mutableStateOf(playerStats.firstOrNull()?.playerId)
    }
    var showPlayerPicker by remember { mutableStateOf(false) }
    // The positions actually played anywhere in this tournament, not one strategy's theoretical
    // column set - a team that played under more than one strategy (e.g. softball's Slowpitch,
    // with 4 outfielders instead of 3) would otherwise silently lose real data off the chart.
    val playedPositions = remember(playerStats) {
        playerStats.flatMap { it.positionCounts.keys }.distinct().sortedBy { it.id }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScreenHeader(
            icon = R.drawable.ic_arrow_left_white_24dp,
            onIconClick = onBackClick,
            iconContentDescription = stringResource(R.string.generic_back),
            eyebrow = state.tournamentName,
            title = stringResource(R.string.title_tournament_statistics),
            modifier = Modifier.padding(16.dp)
        )

        if (playerStats.isEmpty()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                PositionUsageEmptyState()
            }
        } else {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                PlayingTimeCard(playerStats, state.playingTimeSummary)
                Spacer(modifier = Modifier.height(4.dp))
                PositionUsageCard(
                    selectedPlayer = playerStats.firstOrNull { it.playerId == selectedPlayerId },
                    positions = playedPositions,
                    teamType = state.teamType,
                    onPlayerFieldClick = { showPlayerPicker = true }
                )
            }
        }
    }

    if (showPlayerPicker) {
        PlayerPickerSheet(
            players = playerStats,
            onPlayerSelected = {
                selectedPlayerId = it
                showPlayerPicker = false
            },
            onDismiss = { showPlayerPicker = false }
        )
    }
}

@Composable
private fun PlayingTimeCard(playerStats: List<PlayerStatsRow>, summary: PlayingTimeSummary) {
    DugoutCard {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = stringResource(R.string.tournament_stats_playing_time_label).uppercase(),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatChip(
                    modifier = Modifier.weight(1f),
                    value = summary.average.roundToInt(),
                    label = stringResource(R.string.tournament_stats_avg_games_label)
                )
                StatChip(
                    modifier = Modifier.weight(1f),
                    value = summary.lowest,
                    label = stringResource(R.string.tournament_stats_low_games_label),
                    color = MaterialTheme.colorScheme.error
                )
                StatChip(
                    modifier = Modifier.weight(1f),
                    value = summary.highest,
                    label = stringResource(R.string.tournament_stats_high_games_label),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            playerStats.forEach { player ->
                PlayingTimeRow(player = player, maxGames = summary.highest)
            }
        }
    }
}

@Composable
private fun PlayingTimeRow(player: PlayerStatsRow, maxGames: Int) {
    val barColor = if (player.isUnderused) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }
    val widthFraction = if (maxGames > 0) player.totalGames / maxGames.toFloat() else 0f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlayerPhotoTile(imageUri = player.imageUri, shirtNumber = player.shirtNumber, size = 40.dp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = player.name,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = player.totalGames.toString(),
                    color = barColor,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(5.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .weight(widthFraction.coerceAtLeast(0.03f))
                        .fillMaxSize()
                        .clip(RoundedCornerShape(4.dp))
                        .background(barColor)
                ) {}
                if (widthFraction < 1f) {
                    Row(modifier = Modifier.weight(1f - widthFraction)) {}
                }
            }
        }
    }
}

@Composable
private fun PositionUsageCard(
    selectedPlayer: PlayerStatsRow?,
    positions: List<FieldPosition>,
    teamType: TeamType,
    onPlayerFieldClick: () -> Unit
) {
    DugoutCard {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = stringResource(R.string.player_profile_position_usage_label).uppercase(),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(10.dp))
            SelectField(
                label = "",
                value = selectedPlayer?.name,
                placeholder = stringResource(R.string.tournament_stats_select_player_placeholder),
                subLabel = null,
                onClick = onPlayerFieldClick
            )
            Spacer(modifier = Modifier.height(14.dp))
            if (selectedPlayer == null || selectedPlayer.positionCounts.values.sum() == 0) {
                PositionUsageEmptyState()
            } else {
                PositionUsageChart(
                    positionsData = selectedPlayer.positionCounts,
                    positions = positions,
                    teamType = teamType
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerPickerSheet(
    players: List<PlayerStatsRow>,
    onPlayerSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val sortedByName = remember(players) { players.sortedBy { it.name } }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            text = stringResource(R.string.tournament_stats_select_player_placeholder),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 18.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
            items(sortedByName, key = { it.playerId }) { player ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPlayerSelected(player.playerId) }
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DugoutAvatar(name = player.name, imageUri = player.imageUri, size = 36.dp)
                    Text(
                        text = player.name,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
    }
}

private val PreviewPlayerStats = listOf(
    PlayerStatsRow(1L, "Jonas Alvarez", 7, null, 15, mapOf(FieldPosition.SHORT_STOP to 11)),
    PlayerStatsRow(2L, "Devon Ruiz", 23, null, 14, mapOf(FieldPosition.CENTER_FIELD to 14)),
    PlayerStatsRow(3L, "Casey Ford", 31, null, 13, mapOf(FieldPosition.CATCHER to 13)),
    PlayerStatsRow(4L, "Marcus Bell", 14, null, 12, mapOf(FieldPosition.FIRST_BASE to 12)),
    PlayerStatsRow(
        5L, "Theo Nakamura", 5, null, 4, mapOf(FieldPosition.PITCHER to 4), isUnderused = true
    )
)
private val PreviewPlayingTimeSummary =
    PlayingTimeSummary(average = 11.6f, lowest = 4, highest = 15)

@DugoutPreviews
@Composable
private fun TournamentStatisticsScreenPreview() {
    DugoutTheme {
        TournamentStatisticsScreen(
            state = TournamentStatisticsUiState(
                isLoading = false,
                tournamentName = "Fall Classic",
                playerStats = PreviewPlayerStats,
                playingTimeSummary = PreviewPlayingTimeSummary,
                teamType = TeamType.BASEBALL
            ),
            onBackClick = {}
        )
    }
}

@DugoutPreviews
@Composable
private fun TournamentStatisticsScreenEmptyPreview() {
    DugoutTheme {
        TournamentStatisticsScreen(
            state = TournamentStatisticsUiState(
                isLoading = false,
                tournamentName = "Fall Classic",
                teamType = TeamType.BASEBALL
            ),
            onBackClick = {}
        )
    }
}
