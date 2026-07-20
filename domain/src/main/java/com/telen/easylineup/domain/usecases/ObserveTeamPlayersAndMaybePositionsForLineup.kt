/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow

class ObserveTeamPlayersAndMaybePositionsForLineup(private val dao: PlayerRepository) {
    operator fun invoke(lineupId: Long): Flow<List<PlayerWithPosition>> =
        dao.getTeamPlayersAndMaybePositions(lineupId).asFlow()
}
