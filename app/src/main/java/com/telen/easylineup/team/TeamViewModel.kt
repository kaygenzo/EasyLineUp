package com.telen.easylineup.team

import androidx.arch.core.util.Function
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.telen.easylineup.App
import com.telen.easylineup.data.Player
import com.telen.easylineup.data.Team

class TeamViewModel: ViewModel() {

    var teamID: Long? = null

    val teams: LiveData<List<Team>> = App.database.teamDao().getTeams()

    fun getPlayersForTeam(teamID: Long): LiveData<List<Player>> {
        return App.database.playerDao().getPlayersForTeam(teamID)
    }

    fun getPlayers(): LiveData<List<Player>> {
        val getTeam : LiveData<Team> = Transformations.map(App.database.teamDao().getTeams()) {
            it.first()
        }

        val getTeamID = Transformations.map(getTeam) {
            teamID = it.id
            it.id
        }

        return Transformations.switchMap(getTeamID) {
            getPlayersForTeam(it)
        }
    }
}