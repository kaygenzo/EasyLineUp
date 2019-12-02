package com.telen.easylineup.domain

import com.telen.easylineup.repository.model.Constants
import com.telen.easylineup.repository.data.PlayerFieldPositionsDao
import com.telen.easylineup.repository.model.PlayerFieldPosition
import com.telen.easylineup.repository.model.PlayerWithPosition
import io.reactivex.Single

class SaveBattingOrder(private val lineupDao: PlayerFieldPositionsDao): UseCase<SaveBattingOrder.RequestValues, SaveBattingOrder.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        val listOperations: MutableList<PlayerFieldPosition> = mutableListOf()
        requestValues.players.filter { it.order > 0 && it.order < Constants.SUBSTITUTE_ORDER_VALUE }.forEach { player ->
            val playerPosition = player.toPlayerFieldPosition()
            listOperations.add(playerPosition)
        }
        return lineupDao.updatePlayerFieldPositions(listOperations).andThen(Single.just(ResponseValue()))
    }

    class RequestValues(val players: List<PlayerWithPosition>): UseCase.RequestValues
    class ResponseValue: UseCase.ResponseValue
}