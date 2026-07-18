/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import io.reactivex.rxjava3.core.Single

class InsertPlayerFieldPositions(private val dao: PlayerFieldPositionRepository) :
    UseCase<InsertPlayerFieldPositions.RequestValues, InsertPlayerFieldPositions.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return dao.insertPlayerFieldPositions(requestValues.positions)
            .andThen(Single.just(ResponseValue()))
    }

    class RequestValues(val positions: List<PlayerFieldPosition>) : UseCase.RequestValues
    class ResponseValue : UseCase.ResponseValue
}
