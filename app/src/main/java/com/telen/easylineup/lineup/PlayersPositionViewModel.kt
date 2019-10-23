package com.telen.easylineup.lineup

import android.graphics.PointF
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.telen.easylineup.App
import com.telen.easylineup.FieldPosition
import com.telen.easylineup.data.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.filter
import kotlin.collections.first
import kotlin.collections.forEach
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.set

data class LineupStatusDefense(val players: Map<Player, FieldPosition?>, val lineupMode: Int)
data class LineupStatusAttack(val players: Map<Player, FieldPosition?>, val lineupMode: Int)

const val ORDER_PITCHER_WHEN_DH = 10

class PlayersPositionViewModel: ViewModel() {

    var lineupID: Long? = 0
    var lineupTitle: String? = null
    var lineupMode = MODE_NONE
    var editable = false
    val listPlayersWithPosition: MutableList<PlayerWithPosition> = mutableListOf()

    val teams: LiveData<List<Team>> = App.database.teamDao().getTeams()

    private fun getNextAvailableOrder(): Int {
        var availableOrder = 1
        listPlayersWithPosition
                .filter { it.fieldPositionID > 0 }
                .sortedBy { it.order }
                .forEach {
                    if(it.order == availableOrder)
                        availableOrder++
                    else
                        return availableOrder
                }
        return availableOrder
    }

    fun savePlayerFieldPosition(player: Player, point: PointF, position: FieldPosition, isNewObject: Boolean): Completable {

        lineupID?.let { lineupID ->
            val playerPosition: PlayerFieldPosition = if(isNewObject) {
                val order = when(position) {
                    FieldPosition.SUBSTITUTE -> 200
                    FieldPosition.PITCHER -> {
                        if(lineupMode == MODE_NONE)
                            getNextAvailableOrder()
                        else
                            ORDER_PITCHER_WHEN_DH
                    }
                    else -> getNextAvailableOrder()
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

    fun getTeamPlayerWithPositions(lineupID: Long): LiveData<LineupStatusDefense> {

        val getLineup = App.database.lineupDao().getLineupById(lineupID)

        val getListPlayersLiveData = Transformations.switchMap(getLineup) {
            this.lineupMode = it.mode
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
            LineupStatusDefense(result, lineupMode)
        }
    }

    fun deleteLineup(): Completable {
        lineupID?.let { id ->
            return App.database.lineupDao().getLineupByIdSingle(id)
                    .flatMapCompletable { lineup -> App.database.lineupDao().deleteLineup(lineup) }
        } ?: return Completable.complete()
    }

    private fun saveMode(): Completable {
        lineupID?.let { id ->
            return App.database.lineupDao().getLineupByIdSingle(id)
                    .flatMapCompletable { lineup ->
                        lineup.mode = lineupMode
                        App.database.lineupDao().updateLineup(lineup)
                    }
        } ?: return Completable.complete()
    }

    fun registerLineupChange(): LiveData<Lineup> {
        return App.database.lineupDao().getLineupById(lineupID ?: 0)
    }

    fun onDesignatedPlayerChanged(isEnabled: Boolean) {
        val task = Single.just(isEnabled)
                .flatMapCompletable { isDesignatedPlayerEnabled ->
                    this.lineupMode = if(isEnabled) MODE_DH else MODE_NONE
                    val playerTask: Completable = when(isDesignatedPlayerEnabled) {
                        true -> {
                            listPlayersWithPosition.filter { it.position == FieldPosition.PITCHER.position }.firstOrNull()?.let {
                                val playerFieldPosition = it.toPlayerFieldPosition()
                                playerFieldPosition.order = ORDER_PITCHER_WHEN_DH
                                App.database.lineupDao().updatePlayerFieldPosition(playerFieldPosition)
                            } ?: Completable.complete()
                        }
                        false -> {
                            listPlayersWithPosition.filter {
                                it.position == FieldPosition.DH.position || it.position == FieldPosition.PITCHER.position
                            }.let { list ->
                                Observable.fromIterable(list).flatMapCompletable { playerPosition ->
                                    FieldPosition.getFieldPosition(playerPosition.position)?.let {
                                        deletePosition(it)
                                    }
                                }
                            } ?: Completable.complete()
                        }
                    }
                    saveMode().andThen(playerTask)
                }
                .subscribeOn(Schedulers.io())
                .subscribe({

                }, {
                    Timber.e(it)
                })
    }
}