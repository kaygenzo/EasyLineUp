/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.repository.LineupRepository
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.withContext

class DeleteLineup(
    private val lineupDao: LineupRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(lineupId: Long?): Result<Unit> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            val id = lineupId ?: throw Exception("Lineup id is null")
            val lineup = lineupDao.getLineupByIdSingle(id).await()
            lineupDao.deleteLineup(lineup).await()
        }
    }
}
