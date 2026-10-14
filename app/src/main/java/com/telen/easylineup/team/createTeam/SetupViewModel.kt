/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.team.createTeam

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.usecases.SaveTeam
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map

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
class SetupViewModel(private val saveTeam: SaveTeam) : ViewModel() {
    private val _team: MutableSharedFlow<Team> =
        MutableSharedFlow(replay = 1, extraBufferCapacity = 1)
    // Defaults to BASEBALL, not UNKNOWN, so a new team's type picker shows a selection
    // from the start - SaveTeam falls back to BASEBALL anyway when type is left UNKNOWN,
    // this just makes that same default visible instead of implicit.
    private var currentTeam = Team(0, "", null, TeamType.BASEBALL.id, true)

    fun observeTeamName(): Flow<String> {
        return _team.map { it.name }
    }

    fun observeTeamType(): Flow<TeamType> {
        return _team.map { TeamType.getTypeById(it.type) }
    }

    fun observeTeamImage(): Flow<Uri?> {
        return _team.map { team -> team.image?.let { Uri.parse(it) } }
    }

    fun setTeamName(name: String) {
        currentTeam.name = name
        _team.tryEmit(currentTeam)
    }

    fun setTeamImage(image: String?) {
        currentTeam.image = image
        _team.tryEmit(currentTeam)
    }

    fun setTeamType(teamType: TeamType) {
        currentTeam.type = teamType.id
        _team.tryEmit(currentTeam)
    }

    suspend fun onSaveClicked(): Result<Team> {
        return saveTeam(currentTeam)
    }

    fun setTeam(team: Team?) {
        team?.let { currentTeam = it }
        _team.tryEmit(currentTeam)
    }
}
