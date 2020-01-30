package com.telen.easylineup.domain

import com.telen.easylineup.repository.data.TeamDao
import com.telen.easylineup.repository.data.TournamentDao
import io.reactivex.Single

class DeleteAllData(private val teamDao: TeamDao, private val tournamentDao: TournamentDao): UseCase<DeleteAllData.RequestValues, DeleteAllData.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return tournamentDao.getTournaments().flatMapCompletable { tournamentDao.deleteTournaments(it) }
                .andThen(teamDao.getTeamsRx().flatMapCompletable { teamDao.deleteTeams(it) })
                .andThen(Single.just(ResponseValue()))
    }

    class RequestValues(): UseCase.RequestValues
    class ResponseValue: UseCase.ResponseValue
}