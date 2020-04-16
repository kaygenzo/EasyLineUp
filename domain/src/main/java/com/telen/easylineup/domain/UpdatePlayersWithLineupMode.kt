package com.telen.easylineup.domain

import com.telen.easylineup.repository.data.PlayerFieldPositionsDao
import com.telen.easylineup.repository.model.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class UpdatePlayersWithLineupMode(private val lineupDao: PlayerFieldPositionsDao): UseCase<UpdatePlayersWithLineupMode.RequestValues, UpdatePlayersWithLineupMode.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        val playerTask: Completable = when(requestValues.isDesignatedPlayerEnabled) {
            true -> {
                when(requestValues.teamType) {
                    TeamType.SOFTBALL.id -> {
                        Completable.complete()
                    }
                    TeamType.BASEBALL.id -> {
                        // find the pitcher if exists and set him at position 10 in lineup
                        requestValues.players.firstOrNull { it.position == FieldPosition.PITCHER.position }?.let {
                            val playerFieldPosition = it.toPlayerFieldPosition()
                            playerFieldPosition.order = Constants.ORDER_PITCHER_WHEN_DH
                            playerFieldPosition.flags = PlayerFieldPosition.FLAG_FLEX
                            lineupDao.updatePlayerFieldPosition(playerFieldPosition)
                        } ?: Completable.complete()
                    }
                    else -> {
                        Completable.error(IllegalArgumentException())
                    }
                }
            }
            false -> {
                requestValues.players.filter {
                    it.position == FieldPosition.DP_DH.position || (it.flags and PlayerFieldPosition.FLAG_FLEX > 0)
                }.let { list ->
                    Observable.fromIterable(list).flatMapCompletable { playerPosition ->
                        lineupDao.deletePosition(playerPosition.toPlayerFieldPosition())
                    }
                }
            }
        }
        return playerTask.andThen(Single.just(ResponseValue()))
    }

    class RequestValues(val players: List<PlayerWithPosition>, val isDesignatedPlayerEnabled: Boolean, val teamType: Int): UseCase.RequestValues
    class ResponseValue: UseCase.ResponseValue
}