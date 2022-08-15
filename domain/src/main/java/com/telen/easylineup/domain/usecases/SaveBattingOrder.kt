package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.toPlayerFieldPosition
import io.reactivex.rxjava3.core.Single

internal class SaveBattingOrder(private val lineupDao: PlayerFieldPositionRepository): UseCase<SaveBattingOrder.RequestValues, SaveBattingOrder.ResponseValue>() {

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