/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.BatterState
import com.telen.easylineup.domain.model.PlayerWithPosition
import io.reactivex.rxjava3.core.Completable

class UpdatePlayersWithBatters(private val schedulersProvider: SchedulersProvider) {
    operator fun invoke(
        players: List<PlayerWithPosition>,
        batters: List<BatterState>
    ): Completable {
        return Completable.fromAction {
            batters.forEach { apply(players, it) }
        }.subscribeOn(schedulersProvider.io())
    }

    private fun apply(players: List<PlayerWithPosition>, batter: BatterState) {
        players
            .firstOrNull { it.playerId == batter.playerId }
            ?.let { it.order = batter.playerOrder }
    }
}
