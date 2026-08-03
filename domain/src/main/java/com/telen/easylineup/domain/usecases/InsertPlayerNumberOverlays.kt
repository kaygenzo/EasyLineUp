/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.ports.repository.PlayerNumberOverlayRepository
import kotlinx.coroutines.withContext

class InsertPlayerNumberOverlays(
    private val dao: PlayerNumberOverlayRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(overlays: List<PlayerNumberOverlay>): Result<Unit> =
        runCatchingCancellable {
            withContext(dispatcherProvider.io()) {
                dao.createPlayerNumberOverlays(overlays)
            }
        }
}
