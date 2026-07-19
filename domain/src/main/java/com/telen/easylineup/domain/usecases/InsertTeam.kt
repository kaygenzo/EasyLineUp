/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.ports.SchedulersProvider
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.repository.TeamRepository
import io.reactivex.rxjava3.core.Single

class InsertTeam(
    private val dao: TeamRepository,
    private val schedulersProvider: SchedulersProvider
) {
    operator fun invoke(team: Team): Single<Long> {
        return dao.insertTeam(team).subscribeOn(schedulersProvider.io())
    }
}
