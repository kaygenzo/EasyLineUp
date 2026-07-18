/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.repository.LineupRepository
import io.reactivex.rxjava3.core.Single

class InsertLineups(private val dao: LineupRepository) :
    UseCase<InsertLineups.RequestValues, InsertLineups.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return dao.insertLineups(requestValues.lineups)
            .andThen(Single.just(ResponseValue()))
    }

    class RequestValues(val lineups: List<Lineup>) : UseCase.RequestValues
    class ResponseValue : UseCase.ResponseValue
}
