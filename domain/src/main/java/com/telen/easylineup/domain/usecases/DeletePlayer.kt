/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.repository.PlayerRepository
import io.reactivex.rxjava3.core.Single

internal class DeletePlayer(private val dao: PlayerRepository) :
    UseCase<DeletePlayer.RequestValues, DeletePlayer.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return dao.deletePlayer(requestValues.player).andThen(Single.just(ResponseValue()))
    }

    /**
     * @property player
     */
    class RequestValues(val player: Player) : UseCase.RequestValues
    class ResponseValue : UseCase.ResponseValue
}
