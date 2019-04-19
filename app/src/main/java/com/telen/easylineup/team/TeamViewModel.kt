package com.telen.easylineup.team

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.telen.easylineup.App
import com.telen.easylineup.data.DatabaseMockProvider
import com.telen.easylineup.data.Lineup
import com.telen.easylineup.data.Player
import com.telen.easylineup.data.Team

class TeamViewModel: ViewModel() {
    val teams: LiveData<List<Team>> = App.database.teamDao().getTeams()

    fun getPlayersForTeam(teamID: Long): LiveData<List<Player>> {
        return App.database.playerDao().getPlayersForTeam(teamID)
    }

    fun getTeam(): LiveData<Team> {
        return Transformations.map(teams) {
            it.first()
        }
    }
}