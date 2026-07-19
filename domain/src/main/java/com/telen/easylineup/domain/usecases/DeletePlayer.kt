/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.ports.SchedulersProvider
import com.telen.easylineup.domain.repository.PlayerRepository
import io.reactivex.rxjava3.core.Completable

class DeletePlayer(
    private val dao: PlayerRepository,
    private val getPlayer: GetPlayer,
    private val schedulersProvider: SchedulersProvider
) {
    operator fun invoke(playerId: Long?): Completable {
        return getPlayer(playerId)
            .flatMapCompletable { dao.deletePlayer(it) }
            .subscribeOn(schedulersProvider.io())
    }
}
