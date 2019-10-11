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
                val order = when(position) {
                    FieldPosition.SUBSTITUTE -> 200
                    else -> listPlayersWithPosition.filter { !FieldPosition.isSubstitute(it.position) }.count() + 1
                }
                PlayerFieldPosition(
                        playerId = player.id,
                        lineupId = lineupID,
                        position = position.position,
                        order = order)
            } else {
                listPlayersWithPosition.first { it.position == position.position }.toPlayerFieldPosition()
            }

            playerPosition.apply {
                playerId = player.id
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

    fun deletePosition(position: FieldPosition): Completable {
        try {
            listPlayersWithPosition.first { it.position == position.position }.let {
                return App.database.lineupDao().deletePosition(it.toPlayerFieldPosition())
            }
        }
        catch (e: NoSuchElementException) {
            return Completable.error(e)
        }
    }

    fun deletePosition(player: Player, position: FieldPosition): Completable {
        try {
            listPlayersWithPosition.first { it.playerID == player.id && it.position == position.position }.let {
                return App.database.lineupDao().deletePosition(it.toPlayerFieldPosition())
            }
        }
        catch (e: NoSuchElementException) {
            return Completable.error(e)
        }
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

    fun getTeamPlayerWithPositions(lineupID: Long): LiveData<Map<Player, FieldPosition?>> {

        val getTeamLiveData = Transformations.map(App.database.teamDao().getTeams()) {
            it.first()
        }

        val getListPlayersLiveData: LiveData<List<PlayerWithPosition>> = Transformations.switchMap(getTeamLiveData) {
            App.database.playerDao().getTeamPlayersWithPositions(lineupID)
        }

        return Transformations.map(getListPlayersLiveData) { players ->

            listPlayersWithPosition.clear()
            listPlayersWithPosition.addAll(players)

            val result: MutableMap<Player, FieldPosition?> = mutableMapOf()
            players.forEach {
                var position: FieldPosition? = null
                if(it.fieldPositionID > 0) {
                    position = FieldPosition.getFieldPosition(it.position)
                }
                val player = it.toPlayer()
                result[player] = position
            }
            result
        }
    }

    fun deleteLineup(): Completable {
        lineupID?.let { id ->
            return App.database.lineupDao().getLineupByIdSingle(id)
                    .flatMapCompletable { lineup -> App.database.lineupDao().deleteLineup(lineup) }
        } ?: return Completable.complete()
    }
}