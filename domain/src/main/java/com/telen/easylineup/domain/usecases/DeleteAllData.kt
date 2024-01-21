/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.repository.TournamentRepository
import io.reactivex.rxjava3.core.Single

internal class DeleteAllData(
    private val teamDao: TeamRepository,
    private val tournamentDao: TournamentRepository
) : UseCase<DeleteAllData.RequestValues, DeleteAllData.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return tournamentDao.getTournaments()
            .flatMapCompletable { tournamentDao.deleteTournaments(it) }
            .andThen(teamDao.getTeamsRx().flatMapCompletable { teamDao.deleteTeams(it) })
            .andThen(Single.just(ResponseValue()))
    }

    class RequestValues : UseCase.RequestValues
    class ResponseValue : UseCase.ResponseValue
}
