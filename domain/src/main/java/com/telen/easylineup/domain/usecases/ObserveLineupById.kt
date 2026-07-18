/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.repository.LineupRepository
import io.reactivex.rxjava3.core.Flowable

class ObserveLineupById(private val dao: LineupRepository) {
    operator fun invoke(lineupId: Long): Flowable<Lineup> = dao.getLineupById(lineupId)
}
