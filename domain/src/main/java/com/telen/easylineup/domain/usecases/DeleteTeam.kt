/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.ports.TransactionRunner
import com.telen.easylineup.domain.ports.repository.TeamRepository
import com.telen.easylineup.domain.usecases.exceptions.NoTeamsLeftException
import kotlinx.coroutines.withContext

class DeleteTeam(
    private val dao: TeamRepository,
    private val transactionRunner: TransactionRunner,
    private val dispatcherProvider: DispatcherProvider
) {
    /**
     * Fails with [NoTeamsLeftException] when [team] was the last one - the delete itself is
     * still committed then, there is just no other team left to promote to main.
     */
    suspend operator fun invoke(team: Team): Result<Unit> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            // Throwing inside the transaction would roll the delete back, so the transaction
            // only reports whether a team is left, and the exception comes after it committed.
            val teamsLeft = transactionRunner.inTransaction {
                dao.deleteTeam(team)
                if (!team.main) {
                    // the main team was not the one we deleted, no need to designate another one
                    return@inTransaction true
                }
                // we have deleted the main team, let's choose another as main
                val teams = dao.getTeams()
                if (teams.isEmpty()) {
                    return@inTransaction false
                }
                dao.updateTeam(teams.first().copy(main = true))
                true
            }
            if (!teamsLeft) {
                throw NoTeamsLeftException()
            }
        }
    }
}
