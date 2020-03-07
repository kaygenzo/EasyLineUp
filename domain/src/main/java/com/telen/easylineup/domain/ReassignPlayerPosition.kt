package com.telen.easylineup.domain

import android.annotation.SuppressLint
import com.telen.easylineup.repository.data.PlayerFieldPositionsDao
import com.telen.easylineup.repository.model.FieldPosition
import com.telen.easylineup.repository.model.PlayerWithPosition
import io.reactivex.Single

class ReassignPlayerPosition(val dao: PlayerFieldPositionsDao): UseCase<ReassignPlayerPosition.RequestValues, ReassignPlayerPosition.ResponseValue>() {

    @SuppressLint("ApplySharedPref")
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return Single.just(requestValues.player.toPlayerFieldPosition())
                .flatMapCompletable {
                    it.position = requestValues.newPosition.position
                    dao.updatePlayerFieldPosition(it)
                }
                .andThen(Single.just(ResponseValue()))
    }

    class ResponseValue: UseCase.ResponseValue
    class RequestValues(val player: PlayerWithPosition, val newPosition: FieldPosition): UseCase.RequestValues
}