/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.repository.PlayerRepository
import kotlinx.coroutines.withContext

class DeletePlayer(
    private val dao: PlayerRepository,
    private val getPlayer: GetPlayer,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(playerId: Long?): Result<Unit> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            val player = getPlayer(playerId).getOrThrow()
            dao.deletePlayer(player)
        }
    }
}
