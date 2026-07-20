/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.repository.PlayerRepository
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.withContext

class InsertPlayerNumberOverlays(
    private val dao: PlayerRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(overlays: List<PlayerNumberOverlay>): Result<Unit> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            dao.createPlayerNumberOverlays(overlays).await()
        }
    }
}
