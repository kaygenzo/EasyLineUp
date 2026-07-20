/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.repository.TeamRepository
import kotlinx.coroutines.withContext

/**
 * Validates the team name, inserts or updates it, then marks it as the current team.
 */
class SaveTeam(
    private val dao: TeamRepository,
    private val checkTeam: CheckTeam,
    private val saveCurrentTeam: SaveCurrentTeam,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(team: Team): Result<Team> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            checkTeam(team).getOrThrow()

            if (team.type == TeamType.UNKNOWN.id) {
                team.type = TeamType.BASEBALL.id
            }

            if (team.id == 0L) {
                team.id = dao.insertTeam(team)
            } else {
                dao.updateTeam(team)
            }

            saveCurrentTeam(team).getOrThrow()
            team
        }
    }
}
