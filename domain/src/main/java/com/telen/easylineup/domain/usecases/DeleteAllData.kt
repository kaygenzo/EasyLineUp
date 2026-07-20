/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.repository.TournamentRepository
import kotlinx.coroutines.withContext

class DeleteAllData(
    private val teamDao: TeamRepository,
    private val tournamentDao: TournamentRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(): Result<Unit> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            tournamentDao.deleteTournaments(tournamentDao.getTournaments())
            teamDao.deleteTeams(teamDao.getTeamsRx())
        }
    }
}
