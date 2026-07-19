/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.ports.SchedulersProvider
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.repository.LineupRepository
import io.reactivex.rxjava3.core.Completable

class UpdateLineup(
    private val lineupRepo: LineupRepository,
    private val schedulersProvider: SchedulersProvider
) {
    operator fun invoke(lineup: Lineup): Completable {
        return lineupRepo.updateLineup(lineup).subscribeOn(schedulersProvider.io())
    }
}
