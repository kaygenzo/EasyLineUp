/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.ports.SchedulersProvider
import io.reactivex.rxjava3.core.Single

class GetTeamEmails(
    private val getPlayers: GetPlayers,
    private val schedulersProvider: SchedulersProvider
) {
    operator fun invoke(): Single<List<String>> {
        return getPlayers()
            .map { players ->
                players
                    .filter { !it.email.isNullOrEmpty() }
                    .map { it.email ?: "" }
            }
            .subscribeOn(schedulersProvider.io())
    }
}
