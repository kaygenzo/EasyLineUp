package com.telen.easylineup.domain

import com.telen.easylineup.repository.data.PlayerDao
import com.telen.easylineup.repository.model.Player
import io.reactivex.Single
import java.lang.Exception

class NotExistingPlayer: Exception()

class GetPlayer(val dao: PlayerDao): UseCase<GetPlayer.RequestValues, GetPlayer.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return requestValues.playerID?.let { id ->
            if(id == 0L) {
                Single.error(NotExistingPlayer())
            }
            else {
                dao.getPlayerByIdAsSingle(id).map { ResponseValue(it) }
            }
        } ?: Single.error(IllegalArgumentException())
    }

    class ResponseValue(val player: Player): UseCase.ResponseValue
    class RequestValues(val playerID: Long?): UseCase.RequestValues
}