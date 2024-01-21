/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.repository.PlayerRepository
import io.reactivex.rxjava3.core.Single

/**
 * @property dao
 */
internal class GetPlayers(val dao: PlayerRepository) :
    UseCase<GetPlayers.RequestValues, GetPlayers.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return dao.getPlayersByTeamId(requestValues.teamId).map { ResponseValue(it) }
    }

    /**
     * @property players
     */
    class ResponseValue(val players: List<Player>) : UseCase.ResponseValue

    /**
     * @property teamId
     */
    class RequestValues(val teamId: Long) : UseCase.RequestValues
}
