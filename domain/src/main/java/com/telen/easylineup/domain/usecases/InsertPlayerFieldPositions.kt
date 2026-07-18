/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import io.reactivex.rxjava3.core.Completable

class InsertPlayerFieldPositions(
    private val dao: PlayerFieldPositionRepository,
    private val schedulersProvider: SchedulersProvider
) {
    operator fun invoke(positions: List<PlayerFieldPosition>): Completable {
        return dao.insertPlayerFieldPositions(positions).subscribeOn(schedulersProvider.io())
    }
}
