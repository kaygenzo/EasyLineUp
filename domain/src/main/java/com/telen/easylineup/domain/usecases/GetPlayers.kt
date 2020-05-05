package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.repository.PlayerRepository
import io.reactivex.Single

internal class GetPlayers(val dao: PlayerRepository): UseCase<GetPlayers.RequestValues, GetPlayers.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return dao.getPlayers(requestValues.teamID).map { ResponseValue(it) }
    }

    class ResponseValue(val players: List<Player>): UseCase.ResponseValue
    class RequestValues(val teamID: Long): UseCase.RequestValues
}