package com.telen.easylineup.domain

import com.telen.easylineup.UseCase
import com.telen.easylineup.data.LineupDao
import com.telen.easylineup.data.PlayerWithPosition
import com.telen.easylineup.utils.Constants
import io.reactivex.Completable
import io.reactivex.Single

class SaveBattingOrder(private val lineupDao: LineupDao): UseCase<SaveBattingOrder.RequestValues, SaveBattingOrder.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        val listOperations: MutableList<Completable> = mutableListOf()
        requestValues.players.filter { it.order > 0 && it.order < Constants.SUBSTITUTE_ORDER_VALUE }.forEach { player ->
            val playerPosition = player.toPlayerFieldPosition()
            listOperations.add(lineupDao.updatePlayerFieldPosition(playerPosition))
        }
        return Completable.concat(listOperations).andThen(Single.just(ResponseValue()))
    }

    class RequestValues(val players: List<PlayerWithPosition>): UseCase.RequestValues
    class ResponseValue: UseCase.ResponseValue
}