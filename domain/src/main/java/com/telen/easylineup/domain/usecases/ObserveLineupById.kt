/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.repository.LineupRepository
import kotlinx.coroutines.flow.Flow

class ObserveLineupById(private val dao: LineupRepository) {
    operator fun invoke(lineupId: Long): Flow<Lineup> = dao.getLineupById(lineupId)
}
