/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.usecases.exceptions.NameEmptyException
import io.reactivex.rxjava3.core.Completable

class CheckTeam(private val schedulersProvider: SchedulersProvider) {
    operator fun invoke(team: Team): Completable {
        return Completable.defer {
            if ("" == team.name.trim()) {
                Completable.error(NameEmptyException())
            } else {
                Completable.complete()
            }
        }.subscribeOn(schedulersProvider.io())
    }
}
