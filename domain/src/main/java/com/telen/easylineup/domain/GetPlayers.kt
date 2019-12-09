package com.telen.easylineup.domain

import com.telen.easylineup.repository.data.PlayerDao
import com.telen.easylineup.repository.data.TeamDao
import com.telen.easylineup.repository.model.Player
import com.telen.easylineup.repository.model.Team
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class GetPlayers(val dao: PlayerDao): UseCase<GetPlayers.RequestValues, GetPlayers.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return dao.getPlayers(requestValues.teamID).map { ResponseValue(it) }
    }

    class ResponseValue(val players: List<Player>): UseCase.ResponseValue
    class RequestValues(val teamID: Long): UseCase.RequestValues
}