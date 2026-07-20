/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.repository.TeamRepository
import kotlinx.coroutines.withContext

class DeleteTeam(
    private val dao: TeamRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(team: Team): Result<Unit> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            val isMain = team.main
            dao.deleteTeam(team)
            if (isMain) {
                // we have deleted the main team, let's choose another as main
                val teams = dao.getTeamsRx()
                if (teams.isEmpty()) {
                    throw NoSuchElementException()
                }
                val newMain = teams.first().apply { main = true }
                dao.updateTeam(newMain)
            }
            // the main team was not the one we deleted, no need to designate another one
        }
    }
}
