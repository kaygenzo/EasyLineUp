/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.ports.SchedulersProvider
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.repository.TournamentRepository
import io.reactivex.rxjava3.core.Completable

class DeleteAllData(
    private val teamDao: TeamRepository,
    private val tournamentDao: TournamentRepository,
    private val schedulersProvider: SchedulersProvider
) {
    operator fun invoke(): Completable {
        return tournamentDao.getTournaments()
            .flatMapCompletable { tournamentDao.deleteTournaments(it) }
            .andThen(teamDao.getTeamsRx().flatMapCompletable { teamDao.deleteTeams(it) })
            .subscribeOn(schedulersProvider.io())
    }
}
