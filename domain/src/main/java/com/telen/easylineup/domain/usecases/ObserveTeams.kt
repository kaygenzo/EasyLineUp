/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.repository.TeamRepository
import kotlinx.coroutines.flow.Flow

class ObserveTeams(private val dao: TeamRepository) {
    operator fun invoke(): Flow<List<Team>> = dao.getTeams()
}
