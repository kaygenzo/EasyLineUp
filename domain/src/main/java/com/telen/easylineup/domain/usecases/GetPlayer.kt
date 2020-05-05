package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.usecases.exceptions.NotExistingPlayer
import io.reactivex.Single
import java.lang.Exception

internal class GetPlayer(val dao: PlayerRepository): UseCase<GetPlayer.RequestValues, GetPlayer.ResponseValue>() {

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