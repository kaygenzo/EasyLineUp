package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.repository.TournamentRepository
import com.telen.easylineup.domain.model.Tournament
import io.reactivex.Single

internal class GetTournaments(val dao: TournamentRepository): UseCase<GetTournaments.RequestValues, GetTournaments.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return dao.getTournaments().map { ResponseValue(it) }
    }

    class ResponseValue(val tournaments: List<Tournament>): UseCase.ResponseValue
    class RequestValues: UseCase.RequestValues
}