/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.isDpDh
import com.telen.easylineup.domain.model.isSubstitute
import io.reactivex.rxjava3.core.Single

class GetOnlyPlayersInField(private val schedulersProvider: SchedulersProvider) {
    operator fun invoke(playersInLineup: List<PlayerWithPosition>): Single<List<PlayerWithPosition>> {
        return Single.just(playersInLineup)
            .map { list -> list.filter { it.position > 0 && !it.isSubstitute() && !it.isDpDh() } }
            .subscribeOn(schedulersProvider.io())
    }
}
