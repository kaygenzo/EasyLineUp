/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.ports.SchedulersProvider
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.MODE_DISABLED
import com.telen.easylineup.domain.model.MODE_ENABLED
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.isDpDhOrFlex
import com.telen.easylineup.domain.model.isPitcher
import com.telen.easylineup.domain.model.reset
import io.reactivex.rxjava3.core.Completable

class UpdatePlayersWithLineupMode(private val schedulersProvider: SchedulersProvider) {
    operator fun invoke(
        players: List<PlayerWithPosition>,
        lineup: Lineup,
        teamType: Int
    ): Completable {
        return Completable.defer {
            when (lineup.mode) {
                MODE_ENABLED -> when (teamType) {
                    TeamType.SOFTBALL.id -> {
                        /* nothing to do */
                    }
                    TeamType.BASEBALL.id -> {
                        // find the pitcher if exists and set him at position 10 in lineup
                        players.firstOrNull { it.isPitcher() }?.let {
                            // here we use directly the standard strategy because we only have
                            // one strategy in baseball
                            val strategy = TeamStrategy.getStrategyById(lineup.strategy)
                            it.order = strategy.getDesignatedPlayerOrder(lineup.extraHitters)
                            it.flags = PlayerFieldPosition.FLAG_FLEX
                        }
                    }
                    else -> return@defer Completable.error(IllegalArgumentException())
                }
                MODE_DISABLED -> players.filter { it.isDpDhOrFlex() }.forEach { it.reset() }
            }
            Completable.complete()
        }.subscribeOn(schedulersProvider.io())
    }
}
