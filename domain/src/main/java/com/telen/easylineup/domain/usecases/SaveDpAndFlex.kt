/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.getNextAvailableOrder
import com.telen.easylineup.domain.model.isDpDh
import com.telen.easylineup.domain.model.isFlex
import com.telen.easylineup.domain.model.reset
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.usecases.exceptions.NeedAssignBothPlayersException
import kotlinx.coroutines.withContext

class SaveDpAndFlex(private val dispatcherProvider: DispatcherProvider) {
    suspend operator fun invoke(
        lineup: Lineup,
        dp: Player?,
        flex: Player?,
        players: List<PlayerWithPosition>
    ): Result<Unit> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            val theDp = dp ?: throw NeedAssignBothPlayersException()
            val theFlex = flex ?: throw NeedAssignBothPlayersException()
            val strategy = TeamStrategy.getStrategyById(lineup.strategy)

            val oldFlex = players.filter { it.isFlex() }

            // assign the new player as flex
            players.firstOrNull { it.playerId == theFlex.id }?.run {
                flags = PlayerFieldPosition.FLAG_FLEX
                order = strategy.getDesignatedPlayerOrder(lineup.extraHitters)
            }

            // reset old flex flags and reset them because there can be only one flex
            oldFlex.filter { it.playerId != theFlex.id }.forEach {
                it.flags = PlayerFieldPosition.FLAG_NONE
                it.order = players.getNextAvailableOrder(listOf(it.order))
            }

            // check if there is a dp and reset him because there can be only one dp
            players.firstOrNull { it.isDpDh() }?.reset()

            players.firstOrNull { it.playerId == theDp.id }?.run {
                position = FieldPosition.DP_DH.id
                order = players.getNextAvailableOrder(listOf(order))
            }
        }
    }
}
