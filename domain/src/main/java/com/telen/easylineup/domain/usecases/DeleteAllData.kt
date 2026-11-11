/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.ports.TransactionRunner
import com.telen.easylineup.domain.ports.repository.TeamRepository
import com.telen.easylineup.domain.ports.repository.TournamentRepository
import kotlinx.coroutines.withContext

class DeleteAllData(
    private val teamDao: TeamRepository,
    private val tournamentDao: TournamentRepository,
    private val transactionRunner: TransactionRunner,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(): Result<Unit> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            transactionRunner.inTransaction {
                tournamentDao.deleteTournaments(tournamentDao.getTournaments())
                teamDao.deleteTeams(teamDao.getTeams())
            }
        }
    }
}
