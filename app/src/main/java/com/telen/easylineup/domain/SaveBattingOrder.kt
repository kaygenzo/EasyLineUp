package com.telen.easylineup.domain

import com.telen.easylineup.UseCase
import com.telen.easylineup.data.LineupDao
import com.telen.easylineup.data.PlayerWithPosition
import com.telen.easylineup.utils.Constants
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers

class SaveBattingOrder(private val lineupDao: LineupDao): UseCase<SaveBattingOrder.RequestValues, SaveBattingOrder.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues?) {
        requestValues?.let { req ->
            val listOperations: MutableList<Completable> = mutableListOf()
            req.players.filter { it.order > 0 && it.order < Constants.SUBSTITUTE_ORDER_VALUE }.forEach { player ->
                val playerPosition = player.toPlayerFieldPosition()
                listOperations.add(lineupDao.updatePlayerFieldPosition(playerPosition))
            }
            Completable.concat(listOperations)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe({
                        mUseCaseCallback?.onSuccess(ResponseValue())
                    }, {
                        mUseCaseCallback?.onError()
                    })
        }
    }

    class RequestValues(val players: List<PlayerWithPosition>): UseCase.RequestValues
    class ResponseValue: UseCase.ResponseValue
}