/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.repository.LineupRepository
import io.reactivex.rxjava3.core.Single

internal class DeleteTournamentLineups(private val lineupDao: LineupRepository) :
    UseCase<DeleteTournamentLineups.RequestValues, DeleteTournamentLineups.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return lineupDao.getLineupsForTournamentRx(
            requestValues.tournament.id,
            requestValues.team.id
        )
            .flatMapCompletable { lineupDao.deleteLineups(it) }
            .andThen(Single.just(ResponseValue()))
    }

    /**
     * @property tournament
     * @property team
     */
    class RequestValues(val tournament: Tournament, val team: Team) : UseCase.RequestValues
    class ResponseValue : UseCase.ResponseValue
}
