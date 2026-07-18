/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.repository.TeamRepository
import io.reactivex.rxjava3.core.Single

class GetAllTeams(
    private val dao: TeamRepository,
    private val schedulersProvider: SchedulersProvider
) {
    operator fun invoke(): Single<List<Team>> {
        return dao.getTeamsRx().subscribeOn(schedulersProvider.io())
    }
}
