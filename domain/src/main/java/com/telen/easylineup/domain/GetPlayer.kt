package com.telen.easylineup.domain

import com.telen.easylineup.repository.data.PlayerDao
import com.telen.easylineup.repository.data.TeamDao
import com.telen.easylineup.repository.model.Player
import com.telen.easylineup.repository.model.Team
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class GetPlayer(val dao: PlayerDao): UseCase<GetPlayer.RequestValues, GetPlayer.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        requestValues.playerID?.let { id ->
            return dao.getPlayerByIdAsSingle(id).map { ResponseValue(it) }
        } ?: throw IllegalArgumentException()
    }

    class ResponseValue(val player: Player): UseCase.ResponseValue
    class RequestValues(val playerID: Long?): UseCase.RequestValues
}