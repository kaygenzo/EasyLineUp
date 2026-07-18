/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.repository.PlayerRepository
import io.reactivex.rxjava3.core.Completable

class InsertPlayers(
    private val dao: PlayerRepository,
    private val schedulersProvider: SchedulersProvider
) {
    operator fun invoke(players: List<Player>): Completable {
        return dao.insertPlayers(players).subscribeOn(schedulersProvider.io())
    }
}
