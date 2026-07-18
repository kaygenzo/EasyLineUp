/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.repository.LineupRepository
import io.reactivex.rxjava3.core.Single

class DeleteTournamentLineups(
    private val lineupDao: LineupRepository,
    private val getTeam: GetTeam
) : UseCase<DeleteTournamentLineups.RequestValues, DeleteTournamentLineups.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return getTeam.executeUseCase(GetTeam.RequestValues())
            .flatMap { teamResponse ->
                lineupDao.getLineupsForTournamentRx(requestValues.tournament.id, teamResponse.team.id)
                    .flatMapCompletable { lineupDao.deleteLineups(it) }
                    .andThen(Single.just(ResponseValue()))
            }
    }

    /**
     * @property tournament
     */
    class RequestValues(val tournament: Tournament) : UseCase.RequestValues
    class ResponseValue : UseCase.ResponseValue
}
