/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.repository.TeamRepository
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.withContext

class SaveCurrentTeam(
    private val dao: TeamRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(team: Team): Result<Unit> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            val teams = dao.getTeamsRx().await().map {
                it.main = it.id == team.id
                it
            }
            dao.updateTeams(teams).await()
        }
    }
}
