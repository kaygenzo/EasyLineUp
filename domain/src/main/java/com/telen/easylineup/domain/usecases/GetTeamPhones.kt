/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import io.reactivex.rxjava3.core.Single

class GetTeamPhones(
    private val getPlayers: GetPlayers,
    private val schedulersProvider: SchedulersProvider
) {
    operator fun invoke(): Single<List<String>> {
        return getPlayers()
            .map { players ->
                players
                    .filter { !it.phone.isNullOrEmpty() }
                    .map { it.phone ?: "" }
            }
            .subscribeOn(schedulersProvider.io())
    }
}
