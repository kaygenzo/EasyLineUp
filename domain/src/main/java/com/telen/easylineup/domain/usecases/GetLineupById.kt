/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.repository.LineupRepository
import kotlinx.coroutines.withContext

class GetLineupById(
    private val dao: LineupRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(lineupId: Long): Result<Lineup> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            dao.getLineupByIdSingle(lineupId)
        }
    }
}
