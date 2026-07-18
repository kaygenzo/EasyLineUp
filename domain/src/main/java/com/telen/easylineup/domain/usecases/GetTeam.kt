/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.repository.TeamRepository
import io.reactivex.rxjava3.core.Single

class GetTeam(
    private val dao: TeamRepository,
    private val schedulersProvider: SchedulersProvider
) {
    operator fun invoke(): Single<Team> {
        return dao.getTeamsRx().map { teams -> teams.first { team -> team.main } }
            .subscribeOn(schedulersProvider.io())
    }
}
