/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.repository.PlayerRepository
import io.reactivex.rxjava3.core.Flowable

class ObservePlayerNumberOverlays(private val dao: PlayerRepository) {
    operator fun invoke(lineupId: Long): Flowable<List<PlayerNumberOverlay>> =
        dao.observePlayersNumberOverlay(lineupId)
}
