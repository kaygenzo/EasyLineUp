package com.telen.easylineup.domain

import com.telen.easylineup.repository.data.TournamentDao
import com.telen.easylineup.repository.model.Tournament
import io.reactivex.Single

class DeleteTournament(private val dao: TournamentDao): UseCase<DeleteTournament.RequestValues, DeleteTournament.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return dao.deleteTournament(requestValues.tournament).andThen(Single.just(ResponseValue()))
    }

    class RequestValues(val tournament: Tournament): UseCase.RequestValues
    class ResponseValue: UseCase.ResponseValue
}