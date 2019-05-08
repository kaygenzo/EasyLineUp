package com.telen.easylineup.newLineup

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.telen.easylineup.App
import com.telen.easylineup.data.Player
import com.telen.easylineup.data.PlayerFieldPosition
import com.telen.easylineup.data.Team
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

class PlayersPositionViewModel: ViewModel() {

    var lineupID: Long = 0
    var teamID: Long = 0

    val teams: LiveData<List<Team>> = App.database.teamDao().getTeams()

    fun getPlayersForTeam(teamID: Long): LiveData<List<Player>> {
        return App.database.playerDao().getPlayersForTeam(teamID)
    }

    fun getPlayerPositionFor(lineupID: Long, playerID: Long): Maybe<PlayerFieldPosition> {
        return App.database.lineupDao().getPlayerPositionFor(lineupID, playerID)
    }

    fun savePlayerFieldPosition(playerFieldPosition: PlayerFieldPosition): Completable {
        return App.database.lineupDao().insertPlayerFieldPosition(playerFieldPosition)
                .flatMapCompletable {
                    playerFieldPosition.id = it
                    Completable.complete()
                }
    }

    fun getPlayerPositionsForLineup(lineupID: Long): LiveData<List<PlayerFieldPosition>> {
        return App.database.lineupDao().getAllPlayerFieldPositionsForLineup(lineupID)
    }
}