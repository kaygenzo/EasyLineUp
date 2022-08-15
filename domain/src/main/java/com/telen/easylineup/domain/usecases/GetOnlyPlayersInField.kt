package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.toPlayer
import io.reactivex.rxjava3.core.Single

internal class GetOnlyPlayersInField: UseCase<GetOnlyPlayersInField.RequestValues, GetOnlyPlayersInField.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return Single.just(requestValues.playersInLineup)
                .map { list -> list.filter { it.position >= FieldPosition.PITCHER.id && it.position <= FieldPosition.RIGHT_FIELD.id } }
                .map { ResponseValue(it.map { it.toPlayer() }) }
    }

    class ResponseValue(val playersInField: List<Player>): UseCase.ResponseValue
    class RequestValues(val playersInLineup: List<PlayerWithPosition>): UseCase.RequestValues
}