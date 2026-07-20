/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.repository.TeamRepository
import kotlinx.coroutines.withContext

class GetAllTeams(
    private val dao: TeamRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(): Result<List<Team>> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            dao.getTeamsRx()
        }
    }
}
