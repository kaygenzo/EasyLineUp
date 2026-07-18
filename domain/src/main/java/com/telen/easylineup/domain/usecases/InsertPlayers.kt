/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.repository.PlayerRepository
import io.reactivex.rxjava3.core.Single

class InsertPlayers(private val dao: PlayerRepository) :
    UseCase<InsertPlayers.RequestValues, InsertPlayers.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return dao.insertPlayers(requestValues.players).andThen(Single.just(ResponseValue()))
    }

    class RequestValues(val players: List<Player>) : UseCase.RequestValues
    class ResponseValue : UseCase.ResponseValue
}
