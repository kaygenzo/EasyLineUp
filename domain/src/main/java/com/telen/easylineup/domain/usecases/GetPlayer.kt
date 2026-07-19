/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.ports.SchedulersProvider
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.usecases.exceptions.NotExistingPlayerException
import io.reactivex.rxjava3.core.Single

class GetPlayer(
    private val dao: PlayerRepository,
    private val schedulersProvider: SchedulersProvider
) {
    operator fun invoke(playerId: Long?): Single<Player> {
        return (
            playerId?.let { id ->
                if (id == 0L) {
                    Single.error(NotExistingPlayerException())
                } else {
                    dao.getPlayerByIdAsSingle(id)
                }
            } ?: Single.error(IllegalArgumentException())
            ).subscribeOn(schedulersProvider.io())
    }
}
