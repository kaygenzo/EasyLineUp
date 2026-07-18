/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.repository.PlayerRepository
import io.reactivex.rxjava3.core.Single

class InsertPlayerNumberOverlays(private val dao: PlayerRepository) :
    UseCase<InsertPlayerNumberOverlays.RequestValues, InsertPlayerNumberOverlays.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return dao.createPlayerNumberOverlays(requestValues.overlays)
            .andThen(Single.just(ResponseValue()))
    }

    class RequestValues(val overlays: List<PlayerNumberOverlay>) : UseCase.RequestValues
    class ResponseValue : UseCase.ResponseValue
}
