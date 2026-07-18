/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import androidx.lifecycle.LiveData
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.repository.PlayerRepository

class ObserveTeamPlayersAndMaybePositionsForLineup(private val dao: PlayerRepository) {
    operator fun invoke(lineupId: Long): LiveData<List<PlayerWithPosition>> =
        dao.getTeamPlayersAndMaybePositions(lineupId)
}
