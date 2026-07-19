/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.ports.SchedulersProvider
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.repository.PlayerRepository
import io.reactivex.rxjava3.core.Single

class GetPlayers(
    private val dao: PlayerRepository,
    private val getTeam: GetTeam,
    private val schedulersProvider: SchedulersProvider
) {
    operator fun invoke(): Single<List<Player>> {
        return getTeam()
            .flatMap { dao.getPlayersByTeamId(it.id) }
            .subscribeOn(schedulersProvider.io())
    }
}
