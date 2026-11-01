/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.tournaments.statistics

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.usecases.GetTournamentPlayerStatsWithProfiles
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/** Everything [TournamentStatisticsScreen] needs to render. */
data class TournamentStatisticsUiState(
    val isLoading: Boolean = true,
    val tournamentName: String = "",
    val teamType: TeamType = TeamType.UNKNOWN,
    val playerStats: List<PlayerStatsRow> = emptyList()
)

class TournamentStatisticsViewModel(
    private val getTournamentPlayerStatsWithProfiles: GetTournamentPlayerStatsWithProfiles
) : ViewModel() {
    private val _uiState = MutableStateFlow(TournamentStatisticsUiState())
    val uiState: StateFlow<TournamentStatisticsUiState> = _uiState.asStateFlow()

    fun load(tournamentId: Long, tournamentName: String, teamTypeId: Int) {
        _uiState.update {
            it.copy(tournamentName = tournamentName, teamType = TeamType.getTypeById(teamTypeId))
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
                    profiles.map { profile ->
                        PlayerStatsRow(
                            playerId = profile.playerId,
                            name = profile.playerName,
                            shirtNumber = profile.shirtNumber,
                            imageUri = profile.image?.let { uri -> Uri.parse(uri) },
                            totalGames = profile.totalGames,
                            positionCounts = profile.positionCounts
                        )
                    }
                }
                .onSuccess { rows ->
                    _uiState.update { it.copy(isLoading = false, playerStats = rows) }
                }
                .onFailure {
                    Timber.e(it)
                    _uiState.update { state -> state.copy(isLoading = false) }
                }
        }
    }
}
