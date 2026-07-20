/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow

class ObservePlayer(private val dao: PlayerRepository) {
    operator fun invoke(playerId: Long): Flow<Player> = dao.getPlayerById(playerId).asFlow()
}
