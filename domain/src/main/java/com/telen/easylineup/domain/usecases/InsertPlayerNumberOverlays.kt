/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.repository.PlayerRepository
import io.reactivex.rxjava3.core.Completable

class InsertPlayerNumberOverlays(
    private val dao: PlayerRepository,
    private val schedulersProvider: SchedulersProvider
) {
    operator fun invoke(overlays: List<PlayerNumberOverlay>): Completable {
        return dao.createPlayerNumberOverlays(overlays).subscribeOn(schedulersProvider.io())
    }
}
