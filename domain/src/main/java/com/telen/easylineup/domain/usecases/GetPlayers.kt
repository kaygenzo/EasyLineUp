/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.repository.PlayerRepository
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.withContext

class GetPlayers(
    private val dao: PlayerRepository,
    private val getTeam: GetTeam,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(): Result<List<Player>> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            val team = getTeam().await()
            dao.getPlayersByTeamId(team.id).await()
        }
    }
}
