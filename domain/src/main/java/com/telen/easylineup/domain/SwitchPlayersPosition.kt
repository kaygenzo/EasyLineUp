package com.telen.easylineup.domain

import android.annotation.SuppressLint
import com.telen.easylineup.repository.data.PlayerFieldPositionsDao
import com.telen.easylineup.repository.model.PlayerWithPosition
import io.reactivex.Single

class SamePlayerException: Exception()

class SwitchPlayersPosition(val dao: PlayerFieldPositionsDao): UseCase<SwitchPlayersPosition.RequestValues, SwitchPlayersPosition.ResponseValue>() {

    @SuppressLint("ApplySharedPref")
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        if(requestValues.player1.playerID == requestValues.player2.playerID)
            return Single.error(SamePlayerException())
        val player1FieldPosition = requestValues.player1.toPlayerFieldPosition()
        val player2FieldPosition = requestValues.player2.toPlayerFieldPosition()
        val tmp = player1FieldPosition.position
        player1FieldPosition.position = player2FieldPosition.position
        player2FieldPosition.position = tmp

        return dao.updatePlayerFieldPositions(listOf(player1FieldPosition, player2FieldPosition))
                .andThen(Single.just(ResponseValue()))
    }

    class ResponseValue: UseCase.ResponseValue
    class RequestValues(val player1: PlayerWithPosition, val player2: PlayerWithPosition): UseCase.RequestValues
}