package com.telen.easylineup.lineup

import android.graphics.PointF
import androidx.arch.core.util.Function
import androidx.lifecycle.*
import com.telen.easylineup.App
import com.telen.easylineup.FieldPosition
import com.telen.easylineup.data.Player
import com.telen.easylineup.data.PlayerFieldPosition
import com.telen.easylineup.data.PlayerWithPosition
import com.telen.easylineup.data.Team
import io.reactivex.Completable
import io.reactivex.Maybe
import timber.log.Timber
import java.lang.Exception

class PlayersPositionViewModel: ViewModel() {

    var lineupID: Long? = 0
    var lineupTitle: String? = null
    var editable = false
    val listPlayersWithPosition: MutableList<PlayerWithPosition> = mutableListOf()

    val teams: LiveData<List<Team>> = App.database.teamDao().getTeams()

    fun savePlayerFieldPosition(player: Player, point: PointF, position: FieldPosition, isNewObject: Boolean): Completable {

        lineupID?.let { lineupID ->
            val playerPosition: PlayerFieldPosition = if(isNewObject) {
                PlayerFieldPosition(
                        playerId = player.id,
                        lineupId = lineupID,
                        order = listPlayersWithPosition.filter { it.fieldPositionID > 0 }.count() + 1)
            } else {
                listPlayersWithPosition.first { it.playerID == player.id }.toPlayerFieldPosition()
            }

            playerPosition.apply {
                this.position = position.position
                x = point.x
                y = point.y
            }

            Timber.d("before playerFieldPosition=$playerPosition")

            return if(playerPosition.id > 0) {
                App.database.lineupDao().updatePlayerFieldPosition(playerPosition)
            } else {
                App.database.lineupDao().insertPlayerFieldPosition(playerPosition).ignoreElement()
            }
        } ?: return Completable.error(IllegalStateException("LineupID is null"))
    }

    fun saveNewBattingOrder(playerPosition: MutableList<PlayerWithPosition>): Completable {
        val listOperations: MutableList<Completable> = mutableListOf()
        playerPosition.forEach {player ->
            listOperations.add(App.database.lineupDao().getPlayerFieldPosition(player.fieldPositionID)
                    .flatMapCompletable { playerPosition ->
                        playerPosition.order = player.order
                        App.database.lineupDao().updatePlayerFieldPosition(playerPosition)
                    }
            )
        }
        return Completable.concat(listOperations)
    }

    fun getPlayersWithPositions(lineupID: Long): LiveData<List<PlayerWithPosition>> {
        return Transformations.map(App.database.lineupDao().getAllPlayersWithPositionsForLineup(lineupID)) {
            listPlayersWithPosition.clear()
            listPlayersWithPosition.addAll(it)
            it
        }

    }

    fun getTeamPlayerWithPositions(lineupID: Long): LiveData<Map<Player, PointF?>> {

        val getTeamLiveData = Transformations.map(App.database.teamDao().getTeams()) {
            it.first()
        }

        val getListPlayersLiveData: LiveData<List<PlayerWithPosition>> = Transformations.switchMap(getTeamLiveData) {
            App.database.playerDao().getTeamPlayersWithPositions(lineupID)
        }

        return Transformations.map(getListPlayersLiveData) { players ->

            listPlayersWithPosition.clear()
            listPlayersWithPosition.addAll(players)

            val result: MutableMap<Player, PointF?> = mutableMapOf()
            players.forEach {
                var point: PointF? = null
                if(it.fieldPositionID > 0) {
                    point = PointF(it.x, it.y)
                }
                val player = it.toPlayer()
                result[player] = point
            }
            result
        }
    }
}