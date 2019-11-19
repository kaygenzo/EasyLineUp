package com.telen.easylineup.domain

import com.telen.easylineup.FieldPosition
import com.telen.easylineup.UseCase
import com.telen.easylineup.data.LineupDao
import com.telen.easylineup.data.PlayerWithPosition
import com.telen.easylineup.lineup.ORDER_PITCHER_WHEN_DH
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class UpdatePlayersWithLineupMode(private val lineupDao: LineupDao): UseCase<UpdatePlayersWithLineupMode.RequestValues, UpdatePlayersWithLineupMode.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues?) {
        requestValues?.let { req ->
            val playerTask: Completable = when(req.isDesignatedPlayerEnabled) {
                true -> {
                    req.players.firstOrNull { it.position == FieldPosition.PITCHER.position }?.let {
                        val playerFieldPosition = it.toPlayerFieldPosition()
                        playerFieldPosition.order = ORDER_PITCHER_WHEN_DH
                        lineupDao.updatePlayerFieldPosition(playerFieldPosition)
                    } ?: Completable.complete()
                }
                false -> {
                    req.players.filter {
                        it.position == FieldPosition.DH.position || it.position == FieldPosition.PITCHER.position
                    }.let { list ->
                        Observable.fromIterable(list).flatMapCompletable { playerPosition ->
                            lineupDao.deletePosition(playerPosition.toPlayerFieldPosition())
                        }
                    } ?: Completable.complete()
                }
            }
            playerTask
                    .subscribeOn(Schedulers.io())
                    .subscribe({
                        mUseCaseCallback?.onSuccess(ResponseValue())
                    }, {
                        mUseCaseCallback?.onError()
                    })
        }
    }

    class RequestValues(val players: List<PlayerWithPosition>, val isDesignatedPlayerEnabled: Boolean): UseCase.RequestValues
    class ResponseValue: UseCase.ResponseValue
}