package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.repository.TournamentRepository
import com.telen.easylineup.domain.model.Tournament
import io.reactivex.Single

internal class DeleteTournament(private val dao: TournamentRepository): UseCase<DeleteTournament.RequestValues, DeleteTournament.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return dao.deleteTournament(requestValues.tournament).andThen(Single.just(ResponseValue()))
    }

    class RequestValues(val tournament: Tournament): UseCase.RequestValues
    class ResponseValue: UseCase.ResponseValue
}