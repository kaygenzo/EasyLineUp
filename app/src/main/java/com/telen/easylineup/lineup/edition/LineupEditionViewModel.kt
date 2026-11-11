/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup.edition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.TeamRosterSummary
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.usecases.GetLineupById
import com.telen.easylineup.domain.usecases.GetRoster
import com.telen.easylineup.domain.usecases.ObserveTournaments
import com.telen.easylineup.domain.usecases.SaveLineupChanges
import com.telen.easylineup.domain.usecases.exceptions.LineupNameEmptyException
import com.telen.easylineup.domain.usecases.exceptions.TournamentNameEmptyException
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

sealed class LineupEditionEvent
data object LineupUpdated : LineupEditionEvent()
data object InvalidLineupName : LineupEditionEvent()
data object InvalidTournament : LineupEditionEvent()

/**
 * "Edit an existing lineup's fields/roster" service behind the Tournaments list's Edit Lineup
 * sheet, which saves straight to the database. (The Lineup screen's own roster sheet edits the
 * screen's unsaved draft instead, through LineupViewModel.) It has no screen of its own, so
 * [uiState] only carries its one true reactive source (the tournament picker options); the
 * sheet's own in-progress form fields stay local to the Route hosting it.
 */
data class LineupEditionUiState(val tournaments: ImmutableList<Tournament> = persistentListOf())

class LineupEditionViewModel(
    private val getLineupById: GetLineupById,
    private val getRoster: GetRoster,
    private val saveLineupChanges: SaveLineupChanges,
    private val observeTournaments: ObserveTournaments
) : ViewModel() {
    private val _uiState = MutableStateFlow(LineupEditionUiState())
    val uiState: StateFlow<LineupEditionUiState> = _uiState.asStateFlow()

    private val _events = Channel<LineupEditionEvent>(Channel.BUFFERED)
    val events: Flow<LineupEditionEvent> = _events.receiveAsFlow()

    init {
        observeTournaments().catch { Timber.e(it) }
            .onEach { tournaments ->
                _uiState.update { it.copy(tournaments = tournaments.toImmutableList()) }
            }
            .launchIn(viewModelScope)
    }

    suspend fun load(lineupId: Long): Result<Pair<Lineup, TeamRosterSummary>> {
        return getLineupById(lineupId).mapCatching { lineup ->
            lineup to getRoster(lineupId).getOrThrow()
        }
    }

    fun save(lineup: Lineup, roster: TeamRosterSummary) {
        viewModelScope.launch {
            saveLineupChanges(lineup, roster.players)
                .onSuccess { _events.send(LineupUpdated) }
                .onFailure { throwable ->
                    when (throwable) {
                        is LineupNameEmptyException -> _events.send(InvalidLineupName)
                        is TournamentNameEmptyException -> _events.send(InvalidTournament)
                        else -> Timber.e(throwable)
                    }
                }
        }
    }
}
