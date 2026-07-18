/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import androidx.lifecycle.LiveData
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.repository.LineupRepository

class ObserveLineupById(private val dao: LineupRepository) {
    operator fun invoke(lineupId: Long): LiveData<Lineup> = dao.getLineupById(lineupId)
}
