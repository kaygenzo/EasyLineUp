/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.withContext

class InsertPlayerFieldPositions(
    private val dao: PlayerFieldPositionRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(positions: List<PlayerFieldPosition>): Result<Unit> =
        runCatchingCancellable {
            withContext(dispatcherProvider.io()) {
                dao.insertPlayerFieldPositions(positions).await()
            }
        }
}
