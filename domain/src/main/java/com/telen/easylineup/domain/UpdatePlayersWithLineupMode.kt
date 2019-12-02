package com.telen.easylineup.domain

import com.telen.easylineup.repository.model.Constants
import com.telen.easylineup.repository.model.FieldPosition
import com.telen.easylineup.repository.data.PlayerFieldPositionsDao
import com.telen.easylineup.repository.model.PlayerWithPosition
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class UpdatePlayersWithLineupMode(private val lineupDao: PlayerFieldPositionsDao): UseCase<UpdatePlayersWithLineupMode.RequestValues, UpdatePlayersWithLineupMode.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        val playerTask: Completable = when(requestValues.isDesignatedPlayerEnabled) {
            true -> {
                requestValues.players.firstOrNull { it.position == FieldPosition.PITCHER.position }?.let {
                    val playerFieldPosition = it.toPlayerFieldPosition()
                    playerFieldPosition.order = Constants.ORDER_PITCHER_WHEN_DH
                    lineupDao.updatePlayerFieldPosition(playerFieldPosition)
                } ?: Completable.complete()
            }
            false -> {
                requestValues.players.filter {
                    it.position == FieldPosition.DH.position || it.position == FieldPosition.PITCHER.position
                }.let { list ->
                    Observable.fromIterable(list).flatMapCompletable { playerPosition ->
                        lineupDao.deletePosition(playerPosition.toPlayerFieldPosition())
                    }
                }
            }
        }
        return playerTask.andThen(Single.just(ResponseValue()))
    }

    class RequestValues(val players: List<PlayerWithPosition>, val isDesignatedPlayerEnabled: Boolean): UseCase.RequestValues
    class ResponseValue: UseCase.ResponseValue
}