/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.usecases.exceptions.NotExistingPlayerException
import io.reactivex.rxjava3.core.Single

/**
 * @property dao
 */
internal class GetPlayer(val dao: PlayerRepository) :
    UseCase<GetPlayer.RequestValues, GetPlayer.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return requestValues.playerId?.let { id ->
            if (id == 0L) {
                Single.error(NotExistingPlayerException())
            } else {
                dao.getPlayerByIdAsSingle(id).map { ResponseValue(it) }
            }
        } ?: Single.error(IllegalArgumentException())
    }

    /**
     * @property player
     */
    class ResponseValue(val player: Player) : UseCase.ResponseValue

    /**
     * @property playerId
     */
    class RequestValues(val playerId: Long?) : UseCase.RequestValues
}
