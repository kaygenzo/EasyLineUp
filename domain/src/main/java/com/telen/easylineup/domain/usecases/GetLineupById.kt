/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.repository.LineupRepository
import io.reactivex.rxjava3.core.Single

class GetLineupById(
    private val dao: LineupRepository,
    private val schedulersProvider: SchedulersProvider
) {
    operator fun invoke(lineupId: Long): Single<Lineup> {
        return dao.getLineupByIdSingle(lineupId).subscribeOn(schedulersProvider.io())
    }
}
