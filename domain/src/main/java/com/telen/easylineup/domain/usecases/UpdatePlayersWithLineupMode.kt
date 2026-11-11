/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.MODE_DISABLED
import com.telen.easylineup.domain.model.MODE_ENABLED
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.getDesignatedPlayerOrder
import com.telen.easylineup.domain.model.isDpDhOrFlex
import com.telen.easylineup.domain.model.isPitcher
import com.telen.easylineup.domain.model.reset
import com.telen.easylineup.domain.model.update
import com.telen.easylineup.domain.ports.DispatcherProvider
import kotlinx.coroutines.withContext

class UpdatePlayersWithLineupMode(private val dispatcherProvider: DispatcherProvider) {
    /** Returns [players] as [lineup]'s mode wants them; [players] itself is untouched. */
    suspend operator fun invoke(
        players: List<PlayerWithPosition>,
        lineup: Lineup,
        teamType: Int
    ): Result<List<PlayerWithPosition>> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            val draft = players.toMutableList()
            when (lineup.mode) {
                MODE_ENABLED -> when (teamType) {
                    TeamType.SOFTBALL.id -> {
                        /* nothing to do */
                    }

                    TeamType.BASEBALL.id -> {
                        // find the pitcher if exists and set him at position 10 in lineup
                        val pitcher = draft.indexOfFirst { it.isPitcher() }
                        if (pitcher >= 0) {
                            // here we use directly the standard strategy because we only have
                            // one strategy in baseball
                            val strategy = TeamStrategy.getStrategyById(lineup.strategy)
                            draft.update(pitcher) {
                                it.copy(
                                    order = strategy.getDesignatedPlayerOrder(lineup.extraHitters),
                                    flags = PlayerFieldPosition.FLAG_FLEX
                                )
                            }
                        }
                    }

                    else -> throw IllegalArgumentException()
                }

                MODE_DISABLED -> draft.indices
                    .filter { draft[it].isDpDhOrFlex() }
                    .forEach { draft.update(it) { player -> player.reset() } }
            }
            draft
        }
    }
}
