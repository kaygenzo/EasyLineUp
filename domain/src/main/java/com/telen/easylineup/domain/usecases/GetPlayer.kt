/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.usecases.exceptions.NotExistingPlayerException
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.withContext

class GetPlayer(
    private val dao: PlayerRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(playerId: Long?): Result<Player> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            val id = playerId ?: throw IllegalArgumentException()
            if (id == 0L) {
                throw NotExistingPlayerException()
            }
            dao.getPlayerByIdAsSingle(id).await()
        }
    }
}
