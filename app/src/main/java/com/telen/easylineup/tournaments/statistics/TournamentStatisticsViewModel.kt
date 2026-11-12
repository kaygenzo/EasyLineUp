/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.tournaments.statistics

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayerTournamentStatsProfile
import com.telen.easylineup.domain.model.PlayingTimeSummary
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.model.playedPositions
import com.telen.easylineup.domain.model.sortedByName
import com.telen.easylineup.domain.usecases.GetPlayingTimeSummary
import com.telen.easylineup.domain.usecases.GetTournamentPlayerStatsWithProfiles
import com.telen.easylineup.domain.usecases.IsPlayingTimeUnderused
import com.telen.easylineup.domain.usecases.SortPlayersByPlayingTime
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/** Everything [TournamentStatisticsScreen] needs to render. */
data class TournamentStatisticsUiState(
    val isLoading: Boolean = true,

    /** Reading the stats failed: the Route shows a retry state, not "no stats yet". */
    val loadFailed: Boolean = false,
    val tournamentName: String = "",
    val teamType: TeamType = TeamType.UNKNOWN,
    val playerStats: ImmutableList<PlayerStatsRow> = persistentListOf(),

    /** The same rows alphabetically, for the player picker. */
    val playersByName: ImmutableList<PlayerStatsRow> = persistentListOf(),

    /** Every position someone played in this tournament, in field order: the chart's columns. */
    val playedPositions: ImmutableList<FieldPosition> = persistentListOf(),
    val playingTimeSummary: PlayingTimeSummary = PlayingTimeSummary(0f, 0, 0)
)

private class LoadedStats(
    val byPlayingTime: List<PlayerStatsRow>,
    val byName: List<PlayerStatsRow>,
    val playedPositions: List<FieldPosition>,
    val summary: PlayingTimeSummary
)

class TournamentStatisticsViewModel(
    private val getTournamentPlayerStatsWithProfiles: GetTournamentPlayerStatsWithProfiles,
    private val sortPlayersByPlayingTime: SortPlayersByPlayingTime,
    private val getPlayingTimeSummary: GetPlayingTimeSummary,
    private val isPlayingTimeUnderused: IsPlayingTimeUnderused
) : ViewModel() {
    private val _uiState = MutableStateFlow(TournamentStatisticsUiState())
    val uiState: StateFlow<TournamentStatisticsUiState> = _uiState.asStateFlow()

    /** What [load] was last asked for, so [onRetryLoad] can ask again. */
    private var lastLoad: (() -> Unit)? = null

    fun load(tournamentId: Long, tournamentName: String, teamTypeId: Int) {
        lastLoad = { load(tournamentId, tournamentName, teamTypeId) }
        _uiState.update {
            it.copy(
                isLoading = true,
                loadFailed = false,
                tournamentName = tournamentName,
                teamType = TeamType.getTypeById(teamTypeId)
            )
        }
        viewModelScope.launch {
            // Only `.id` is actually read by GetTournamentPlayerStats - the other fields are
            // unused placeholders, avoiding the need for a custom NavType for the full Tournament.
            val tournament = Tournament(
                id = tournamentId,
                name = tournamentName,
                createdAt = 0,
                startTime = 0,
                endTime = 0
            )
            getTournamentPlayerStatsWithProfiles(tournament)
                .mapCatching { profiles ->
                    val summary = getPlayingTimeSummary(profiles)
                    fun PlayerTournamentStatsProfile.toRow() = PlayerStatsRow(
                        playerId = playerId,
                        name = playerName,
                        shirtNumber = shirtNumber,
                        imageUri = image?.let { uri -> Uri.parse(uri) },
                        totalGames = totalGames,
                        positionCounts = positionCounts.toImmutableMap(),
                        isUnderused = isPlayingTimeUnderused(totalGames, summary.average)
                    )
                    LoadedStats(
                        byPlayingTime = sortPlayersByPlayingTime(profiles).map { it.toRow() },
                        byName = profiles.sortedByName().map { it.toRow() },
                        playedPositions = profiles.playedPositions(),
                        summary = summary
                    )
                }
                .onSuccess { loaded ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            playerStats = loaded.byPlayingTime.toImmutableList(),
                            playersByName = loaded.byName.toImmutableList(),
                            playedPositions = loaded.playedPositions.toImmutableList(),
                            playingTimeSummary = loaded.summary
                        )
                    }
                }
                .onFailure {
                    Timber.e(it)
                    _uiState.update { state -> state.copy(isLoading = false, loadFailed = true) }
                }
        }
    }

    fun onRetryLoad() {
        lastLoad?.invoke()
    }
}
