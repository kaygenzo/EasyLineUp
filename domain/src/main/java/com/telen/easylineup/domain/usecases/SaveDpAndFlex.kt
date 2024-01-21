/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
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
import com.telen.easylineup.domain.usecases.exceptions.NeedAssignBothPlayersException
import io.reactivex.rxjava3.core.Single

internal class SaveDpAndFlex : UseCase<SaveDpAndFlex.RequestValues, SaveDpAndFlex.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        val players = requestValues.players
        val lineup = requestValues.lineup
        val dp = requestValues.dp ?: return Single.error(NeedAssignBothPlayersException())
        val flex = requestValues.flex ?: return Single.error(NeedAssignBothPlayersException())
        val strategy = TeamStrategy.getStrategyById(lineup.strategy)

        val oldFlex = players.filter { it.isFlex() }

        // assign the new player as flex
        players.firstOrNull { it.playerId == flex.id }?.run {
            flags = PlayerFieldPosition.FLAG_FLEX
            order = strategy.getDesignatedPlayerOrder(lineup.extraHitters)
        }

        // reset old flex flags and reset them because there can be only one flex
        oldFlex.filter { it.playerId != flex.id }.forEach {
            it.flags = PlayerFieldPosition.FLAG_NONE
            it.order = players.getNextAvailableOrder(listOf(it.order))
        }

        // check if there is a dp and reset him because there can be only one dp
        players.firstOrNull { it.isDpDh() }?.reset()

        players.firstOrNull { it.playerId == dp.id }?.run {
            position = FieldPosition.DP_DH.id
            order = players.getNextAvailableOrder(listOf(order))
        }

        return Single.just(ResponseValue())
    }

    /**
     * @property lineup
     * @property dp
     * @property flex
     * @property players
     */
    class RequestValues(
        val lineup: Lineup,
        val dp: Player?,
        val flex: Player?,
        val players: List<PlayerWithPosition>
    ) : UseCase.RequestValues

    inner class ResponseValue : UseCase.ResponseValue
}
