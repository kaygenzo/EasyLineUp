package com.telen.easylineup.lineup

import androidx.lifecycle.*
import com.telen.easylineup.App
import com.telen.easylineup.data.Player
import com.telen.easylineup.data.PlayerFieldPosition
import com.telen.easylineup.data.PlayerWithPosition
import com.telen.easylineup.data.Team
import io.reactivex.Completable
import io.reactivex.Maybe

class PlayersPositionViewModel: ViewModel() {

    var lineupID: Long? = 0
    var teamID: Long? = 0
    var lineupTitle: String? = null
    var editable = false

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

    fun saveNewBattingOrder(playerBattingOrder: Map<Long, Int>): Completable {
        val listOperations: MutableList<Completable> = mutableListOf()
        playerBattingOrder.forEach {
            listOperations.add(App.database.lineupDao().getPlayerFieldPosition(it.key)
                    .flatMapCompletable { playerPosition ->
                        playerPosition.order = it.value
                        App.database.lineupDao().updatePlayerFieldPosition(playerPosition)
                    }
            )
        }
        return Completable.concat(listOperations)
    }

    fun getPlayersWithPositions(lineupID: Long): LiveData<List<PlayerWithPosition>> {
        return App.database.lineupDao().getAllPlayersWithPositionsForLineup(lineupID)
    }
}