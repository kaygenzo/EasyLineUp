/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.repository.LineupRepository
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.withContext

class UpdateLineup(
    private val lineupRepo: LineupRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(lineup: Lineup): Result<Unit> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            lineupRepo.updateLineup(lineup).await()
        }
    }
}
