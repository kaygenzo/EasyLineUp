/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.ports.Clock
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.ports.TransactionRunner
import com.telen.easylineup.domain.ports.repository.TeamRepository
import kotlinx.coroutines.withContext

/**
 * Validates the team name, inserts or updates it, then marks it as the current team.
 */
class SaveTeam(
    private val dao: TeamRepository,
    private val checkTeam: CheckTeam,
    private val saveCurrentTeam: SaveCurrentTeam,
    private val transactionRunner: TransactionRunner,
    private val clock: Clock,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(team: Team): Result<Team> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            transactionRunner.inTransaction {
                checkTeam(team).getOrThrow()

                val typedTeam = if (team.type == TeamType.UNKNOWN.id) {
                    team.copy(type = TeamType.BASEBALL.id)
                } else {
                    team
                }

                val savedTeam = if (typedTeam.id == 0L) {
                    val withCreatedAt = typedTeam.copy(createdAt = clock.nowMillis())
                    withCreatedAt.copy(id = dao.insertTeam(withCreatedAt))
                } else {
                    dao.updateTeam(typedTeam)
                    typedTeam
                }

                saveCurrentTeam(savedTeam).getOrThrow()
                savedTeam
            }
        }
    }
}
