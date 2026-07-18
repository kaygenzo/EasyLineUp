/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import android.annotation.SuppressLint
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.repository.TeamRepository
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable

class SaveCurrentTeam(
    private val dao: TeamRepository,
    private val schedulersProvider: SchedulersProvider
) {
    @SuppressLint("ApplySharedPref")
    operator fun invoke(team: Team): Completable {
        return dao.getTeamsRx()
            .flatMapObservable { Observable.fromIterable(it) }
            .map {
                it.main = it.id == team.id
                it
            }
            .toList()
            .flatMapCompletable { dao.updateTeams(it) }
            .subscribeOn(schedulersProvider.io())
    }
}
