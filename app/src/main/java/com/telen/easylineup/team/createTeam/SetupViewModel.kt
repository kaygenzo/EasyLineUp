/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.team.createTeam

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.usecases.SaveTeam
import com.telen.easylineup.domain.usecases.exceptions.NameEmptyException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/** One-shot outcomes of [SetupViewModel.onSaveClicked], for the Activity to finish or warn. */
sealed interface SetupEvent {
    data object TeamSaved : SetupEvent
    data object TeamNameEmpty : SetupEvent

    /** Saving failed for a reason the form can't point at: nothing was written. */
    data object TeamSaveFailed : SetupEvent
}

/**
 * Backs both team creation and team edition - [setTeam] receives `null` for a new team, or the
 * real [Team] being edited, and that's the only distinction the rest of the flow needs: the same
 * screen, the same [onSaveClicked] call ([SaveTeam] itself branches on `Team.id` to insert or
 * update).
 *
 * Every setter re-emits [currentTeam] through [_team] - the Compose screen is fully state-driven
 * (unlike the old View-based form, whose widgets held their own visual state locally), so a
 * mutation that isn't re-emitted never becomes visible.
 */
class SetupViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val saveTeam: SaveTeam
) : ViewModel() {
    private val _team: MutableSharedFlow<Team> =
        MutableSharedFlow(replay = 1, extraBufferCapacity = 1)

    private val _events = Channel<SetupEvent>(Channel.BUFFERED)
    val events: Flow<SetupEvent> = _events.receiveAsFlow()
    // Defaults to BASEBALL, not UNKNOWN, so a new team's type picker shows a selection
    // from the start - SaveTeam falls back to BASEBALL anyway when type is left UNKNOWN,
    // this just makes that same default visible instead of implicit.
    // Kept in SavedStateHandle so the form survives Android killing the app in the background.
    private var currentTeam: Team
        get() = savedStateHandle[KEY_TEAM] ?: Team(0, "", null, TeamType.BASEBALL.id, true)
        set(value) {
            savedStateHandle[KEY_TEAM] = value
        }

    fun observeTeamName(): Flow<String> {
        return _team.map { it.name }
    }

    fun observeTeamType(): Flow<TeamType> {
        return _team.map { TeamType.getTypeById(it.type) }
    }

    fun observeTeamImage(): Flow<Uri?> {
        return _team.map { team -> team.image?.toUri() }
    }

    fun setTeamName(name: String) {
        currentTeam = currentTeam.copy(name = name)
        _team.tryEmit(currentTeam)
    }

    fun setTeamImage(image: String?) {
        currentTeam = currentTeam.copy(image = image)
        _team.tryEmit(currentTeam)
    }

    fun setTeamType(teamType: TeamType) {
        currentTeam = currentTeam.copy(type = teamType.id)
        _team.tryEmit(currentTeam)
    }

    /** Saves in this ViewModel's scope, so a rotation or leaving can't cancel it halfway. */
    fun onSaveClicked() {
        val team = currentTeam
        viewModelScope.launch {
            saveTeam(team)
                .onSuccess { _events.send(SetupEvent.TeamSaved) }
                .onFailure { throwable ->
                    if (throwable is NameEmptyException) {
                        _events.send(SetupEvent.TeamNameEmpty)
                    } else {
                        Timber.e(throwable)
                        _events.send(SetupEvent.TeamSaveFailed)
                    }
                }
        }
    }

    /**
     * Called on every Activity creation: ignored once the form has started, so neither a
     * rotation nor a return after the process was killed resets what the user typed.
     */
    fun setTeam(team: Team?) {
        if (!savedStateHandle.contains(KEY_TEAM)) {
            team?.let { currentTeam = it }
        }
        _team.tryEmit(currentTeam)
    }

    private companion object {
        const val KEY_TEAM = "team"
    }
}
