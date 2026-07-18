/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.repository.TournamentRepository
import io.reactivex.rxjava3.core.Single

class InsertTournaments(private val dao: TournamentRepository) :
    UseCase<InsertTournaments.RequestValues, InsertTournaments.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return dao.insertTournaments(requestValues.tournaments)
            .andThen(Single.just(ResponseValue()))
    }

    class RequestValues(val tournaments: List<Tournament>) : UseCase.RequestValues
    class ResponseValue : UseCase.ResponseValue
}
