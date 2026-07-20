/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow

class ObservePlayers(private val dao: PlayerRepository) {
    operator fun invoke(teamId: Long): Flow<List<Player>> = dao.observePlayers(teamId).asFlow()
}
