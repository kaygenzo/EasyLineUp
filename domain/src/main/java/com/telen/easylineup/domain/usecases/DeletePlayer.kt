/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.repository.PlayerRepository
import io.reactivex.rxjava3.core.Single

class DeletePlayer(
    private val dao: PlayerRepository,
    private val getPlayer: GetPlayer
) : UseCase<DeletePlayer.RequestValues, DeletePlayer.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return getPlayer.executeUseCase(GetPlayer.RequestValues(requestValues.playerId))
            .map { it.player }
            .flatMapCompletable { dao.deletePlayer(it) }
            .andThen(Single.just(ResponseValue()))
    }

    /**
     * @property playerId
     */
    class RequestValues(val playerId: Long?) : UseCase.RequestValues
    class ResponseValue : UseCase.ResponseValue
}
