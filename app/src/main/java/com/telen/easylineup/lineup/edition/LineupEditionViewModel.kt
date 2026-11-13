/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup.edition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.TeamRosterSummary
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.usecases.ClearRoster
import com.telen.easylineup.domain.usecases.GetLineupById
import com.telen.easylineup.domain.usecases.GetRoster
import com.telen.easylineup.domain.usecases.HasUnsavedLineupEdits
import com.telen.easylineup.domain.usecases.ObserveTournaments
import com.telen.easylineup.domain.usecases.OverrideRosterPlayerNumber
import com.telen.easylineup.domain.usecases.SaveLineupChanges
import com.telen.easylineup.domain.usecases.ToggleRosterPlayer
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

/** Saving failed for a reason the sheet can't point at: nothing was written. */
data object LineupUpdateFailed : LineupEditionEvent()

/** The lineup to edit couldn't be read: the sheet stays closed. */
data object LineupLoadFailed : LineupEditionEvent()

/**
 * The Edit Lineup sheet's form, from the moment a lineup is loaded into it until it is saved or
 * dismissed.
 *
 * @property loaded the lineup as it was loaded, which the save applies the edits to
 * @property loadedRoster the roster as it was loaded, to tell an edit from no edit
 * @property nameError the save was refused for a blank name
 * @property tournamentError the save was refused for a missing tournament
 */
data class EditLineupForm(
    val loaded: Lineup,
    val loadedRoster: TeamRosterSummary,
    val name: String,
    val nameError: Boolean = false,
    val tournament: Tournament?,
    val tournamentError: Boolean = false,
    val roster: TeamRosterSummary,
    val step: EditLineupStep = EditLineupStep.FIELDS,
    val showTournamentSheet: Boolean = false,
    val showDiscardSheet: Boolean = false
)

/**
 * The Tournaments list's Edit Lineup sheet, which saves straight to the database. (The Lineup
 * screen's own roster sheet edits the screen's unsaved draft instead, through LineupViewModel.)
 *
 * @property form the sheet's form, null while the sheet is closed
 */
data class LineupEditionUiState(
    val tournaments: ImmutableList<Tournament> = persistentListOf(),
    val form: EditLineupForm? = null
)

class LineupEditionViewModel(
    private val getLineupById: GetLineupById,
    private val getRoster: GetRoster,
    private val saveLineupChanges: SaveLineupChanges,
    private val observeTournaments: ObserveTournaments,
    private val toggleRosterPlayer: ToggleRosterPlayer,
    private val clearRoster: ClearRoster,
    private val overrideRosterPlayerNumber: OverrideRosterPlayerNumber,
    private val hasUnsavedLineupEdits: HasUnsavedLineupEdits
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

    /** Loads [lineupId] and opens the sheet on it; on failure the sheet stays closed. */
    fun onEditLineup(lineupId: Long) {
        viewModelScope.launch {
            getLineupById(lineupId)
                .mapCatching { lineup -> lineup to getRoster(lineupId).getOrThrow() }
                .onSuccess { (lineup, roster) ->
                    _uiState.update { state ->
                        state.copy(
                            form = EditLineupForm(
                                loaded = lineup,
                                loadedRoster = roster,
                                name = lineup.name,
                                tournament = state.tournaments.find {
                                    it.id == lineup.tournamentId
                                },
                                roster = roster
                            )
                        )
                    }
                }
                .onFailure {
                    Timber.e(it)
                    _events.send(LineupLoadFailed)
                }
        }
    }

    fun onNameChanged(name: String) = updateForm { it.copy(name = name, nameError = false) }

    fun onTournamentClick() = updateForm { it.copy(showTournamentSheet = true) }

    fun onTournamentSheetDismiss() = updateForm { it.copy(showTournamentSheet = false) }

    fun onTournamentSelected(tournament: Tournament) =
        updateForm { it.copy(tournament = tournament, tournamentError = false) }

    fun onRosterClick() = updateForm { it.copy(step = EditLineupStep.ROSTER) }

    fun onRosterBackClick() = updateForm { it.copy(step = EditLineupStep.FIELDS) }

    fun onPlayerToggled(position: Int, checked: Boolean) =
        updateForm { it.copy(roster = toggleRosterPlayer(it.roster, position, checked)) }

    fun onNumberChanged(position: Int, number: Int) = updateForm {
        it.copy(roster = overrideRosterPlayerNumber(it.roster, position, number, it.loaded.id))
    }

    fun onClearAll() = updateForm { it.copy(roster = clearRoster(it.roster)) }

    /** Closes the sheet, unless that would throw away an edit: then asks first. */
    fun onCancel() {
        val form = _uiState.value.form ?: return
        val edited = hasUnsavedLineupEdits(
            form.loaded,
            form.loadedRoster,
            form.name,
            form.tournament?.id,
            form.roster
        )
        if (edited) {
            updateForm { it.copy(showDiscardSheet = true) }
        } else {
            closeForm()
        }
    }

    fun onDiscardDismiss() = updateForm { it.copy(showDiscardSheet = false) }

    fun onDiscardConfirm() = closeForm()

    fun onSave() {
        val form = _uiState.value.form ?: return
        val updated = form.loaded.copy(name = form.name, tournamentId = form.tournament?.id ?: 0)
        viewModelScope.launch {
            saveLineupChanges(updated, form.roster.players)
                .onSuccess { closeForm() }
                .onFailure { throwable ->
                    when (throwable) {
                        is LineupNameEmptyException -> updateForm { it.copy(nameError = true) }
                        is TournamentNameEmptyException ->
                            updateForm { it.copy(tournamentError = true) }
                        else -> {
                            Timber.e(throwable)
                            _events.send(LineupUpdateFailed)
                        }
                    }
                }
        }
    }

    private fun closeForm() {
        _uiState.update { it.copy(form = null) }
    }

    private fun updateForm(transform: (EditLineupForm) -> EditLineupForm) {
        _uiState.update { state -> state.copy(form = state.form?.let(transform)) }
    }
}
