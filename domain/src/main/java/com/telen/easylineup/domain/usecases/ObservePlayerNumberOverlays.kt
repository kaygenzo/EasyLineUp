/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow

class ObservePlayerNumberOverlays(private val dao: PlayerRepository) {
    operator fun invoke(lineupId: Long): Flow<List<PlayerNumberOverlay>> =
        dao.observePlayersNumberOverlay(lineupId)
}
