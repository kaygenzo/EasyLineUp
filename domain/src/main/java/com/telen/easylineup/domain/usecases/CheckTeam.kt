/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.usecases.exceptions.NameEmptyException
import kotlinx.coroutines.withContext

class CheckTeam(private val dispatcherProvider: DispatcherProvider) {
    suspend operator fun invoke(team: Team): Result<Unit> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            if ("" == team.name.trim()) {
                throw NameEmptyException()
            }
        }
    }
}
