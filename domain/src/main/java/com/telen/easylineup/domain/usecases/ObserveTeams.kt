/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.repository.TeamRepository
import io.reactivex.rxjava3.core.Flowable

class ObserveTeams(private val dao: TeamRepository) {
    operator fun invoke(): Flowable<List<Team>> = dao.getTeams()
}
