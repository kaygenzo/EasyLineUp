/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.repository.TournamentRepository
import io.reactivex.rxjava3.core.Single

/**
 * @property dao
 */
internal class GetTournaments(val dao: TournamentRepository) :
    UseCase<GetTournaments.RequestValues, GetTournaments.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return dao.getTournaments().map { ResponseValue(it) }
    }

    /**
     * @property tournaments
     */
    class ResponseValue(val tournaments: List<Tournament>) : UseCase.ResponseValue
    class RequestValues : UseCase.RequestValues
}
