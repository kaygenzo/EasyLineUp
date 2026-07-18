/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import androidx.lifecycle.LiveData
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.repository.TeamRepository

class ObserveTeams(private val dao: TeamRepository) {
    operator fun invoke(): LiveData<List<Team>> = dao.getTeams()
}
