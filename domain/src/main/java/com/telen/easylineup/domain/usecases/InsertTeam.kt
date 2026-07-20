/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.repository.TeamRepository
import kotlinx.coroutines.withContext

class InsertTeam(
    private val dao: TeamRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(team: Team): Result<Long> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            dao.insertTeam(team)
        }
    }
}
