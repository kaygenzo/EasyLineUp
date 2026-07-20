/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.repository.PlayerRepository
import kotlinx.coroutines.withContext

class InsertPlayers(
    private val dao: PlayerRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(players: List<Player>): Result<Unit> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            dao.insertPlayers(players)
        }
    }
}
