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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
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
import com.telen.easylineup.compose.uppercaseLocalized
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayingTimeSummary
import com.telen.easylineup.domain.model.TeamType
import kotlin.math.roundToInt
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

/**
 * One row's worth of UI-ready data - [com.telen.easylineup.domain.model.PlayerTournamentStats]
 * merged with the roster's own [com.telen.easylineup.domain.model.Player] for the photo/shirt
 * number the domain-layer stats alone don't carry. Rows arrive from the ViewModel already ranked
 * by playing time (most-played first) and flagged against the tournament's own average.
 */
/**
 * The selected-player field shows whichever name the playing-time ranking puts first -
 * unpredictable at test-time since ties in
 * [com.telen.easylineup.domain.usecases.SortPlayersByPlayingTime] preserve raw query order - so
 * it needs a stable tag rather than relying on its displayed text.
 */
object TournamentStatisticsTestTags {
    const val PLAYER_SELECT_FIELD = "tournament_stats_player_select_field"
}

data class PlayerStatsRow(
    val playerId: Long,
    val name: String,
    val shirtNumber: Int,
    val imageUri: Uri?,
    val totalGames: Int,
    val positionCounts: ImmutableMap<FieldPosition, Int>,
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
    var showPlayerPicker by rememberSaveable { mutableStateOf(false) }

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
                // No player is in any of this tournament's lineups yet, so the single-player
                // wording ("once this player is in a lineup") would point at nobody.
                PositionUsageEmptyState(
                    message = stringResource(R.string.tournament_stats_empty_message)
                )
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
                    positions = state.playedPositions,
                    teamType = state.teamType,
                    onPlayerFieldClick = { showPlayerPicker = true }
                )
            }
        }
    }

    if (showPlayerPicker) {
        PlayerPickerSheet(
            players = state.playersByName,
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
                text = stringResource(
                    R.string.tournament_stats_playing_time_label
                ).uppercaseLocalized(),
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
                text = stringResource(
                    R.string.player_profile_position_usage_label
                ).uppercaseLocalized(),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(10.dp))
            SelectField(
                label = "",
                value = selectedPlayer?.name,
                placeholder = stringResource(R.string.tournament_stats_select_player_placeholder),
                subLabel = null,
                onClick = onPlayerFieldClick,
                modifier = Modifier.testTag(TournamentStatisticsTestTags.PLAYER_SELECT_FIELD)
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
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            text = stringResource(R.string.tournament_stats_select_player_placeholder),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 18.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
            items(players, key = { it.playerId }) { player ->
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

private val PreviewPlayerStats = persistentListOf(
    PlayerStatsRow(
        1L, "Jonas Alvarez", 7, null, 15, persistentMapOf(FieldPosition.SHORT_STOP to 11)
    ),
    PlayerStatsRow(
        2L, "Devon Ruiz", 23, null, 14, persistentMapOf(FieldPosition.CENTER_FIELD to 14)
    ),
    PlayerStatsRow(
        3L, "Casey Ford", 31, null, 13, persistentMapOf(FieldPosition.CATCHER to 13)
    ),
    PlayerStatsRow(
        4L, "Marcus Bell", 14, null, 12, persistentMapOf(FieldPosition.FIRST_BASE to 12)
    ),
    PlayerStatsRow(
        5L, "Theo Nakamura", 5, null, 4, persistentMapOf(FieldPosition.PITCHER to 4),
        isUnderused = true
    )
)
private val PreviewPlayersByName = persistentListOf(
    PreviewPlayerStats[2],
    PreviewPlayerStats[1],
    PreviewPlayerStats[0],
    PreviewPlayerStats[3],
    PreviewPlayerStats[4]
)
private val PreviewPlayedPositions = persistentListOf(
    FieldPosition.PITCHER,
    FieldPosition.CATCHER,
    FieldPosition.FIRST_BASE,
    FieldPosition.SHORT_STOP,
    FieldPosition.CENTER_FIELD
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
                playersByName = PreviewPlayersByName,
                playedPositions = PreviewPlayedPositions,
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

@DugoutPreviews
@Composable
private fun PlayingTimeCardPreview() {
    DugoutTheme {
        PlayingTimeCard(playerStats = PreviewPlayerStats, summary = PreviewPlayingTimeSummary)
    }
}

@DugoutPreviews
@Composable
private fun PlayingTimeRowPreview() {
    DugoutTheme {
        PlayingTimeRow(player = PreviewPlayerStats.last(), maxGames = 15)
    }
}

@DugoutPreviews
@Composable
private fun PositionUsageCardPreview() {
    DugoutTheme {
        PositionUsageCard(
            selectedPlayer = PreviewPlayerStats.first(),
            positions = listOf(FieldPosition.SHORT_STOP, FieldPosition.CENTER_FIELD),
            teamType = TeamType.BASEBALL,
            onPlayerFieldClick = {}
        )
    }
}

@DugoutPreviews
@Composable
private fun PlayerPickerSheetPreview() {
    DugoutTheme {
        PlayerPickerSheet(players = PreviewPlayersByName, onPlayerSelected = {}, onDismiss = {})
    }
}
