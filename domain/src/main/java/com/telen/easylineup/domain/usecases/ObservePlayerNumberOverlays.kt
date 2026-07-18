/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import androidx.lifecycle.LiveData
import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.repository.PlayerRepository

class ObservePlayerNumberOverlays(private val dao: PlayerRepository) {
    fun execute(lineupId: Long): LiveData<List<PlayerNumberOverlay>> =
        dao.observePlayersNumberOverlay(lineupId)
}
