/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.MODE_ENABLED
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.isDpDhOrFlex
import com.telen.easylineup.domain.model.isSubstitute
import com.telen.easylineup.domain.model.reset
import io.reactivex.rxjava3.core.Single

internal class DeletePlayerFieldPosition :
    UseCase<DeletePlayerFieldPosition.RequestValues, DeletePlayerFieldPosition.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return try {
            val players = requestValues.players
            // substitutes have the same position, let's use player to get the good one
            val player = players.first { it.playerId == requestValues.playerToDelete.id }

            val playerIsSubstitute = player.isSubstitute()
            val playerOrder = player.order
            if (requestValues.lineupMode == MODE_ENABLED && player.isDpDhOrFlex()) {
                players.filter { it.isDpDhOrFlex() }
            } else {
                listOf(player)
            }.forEach { it.reset() }

            // check if substitutes was a batter, update the next substitutes order to replace it
            // if necessary
            if (playerIsSubstitute && playerOrder < Constants.SUBSTITUTE_ORDER_VALUE) {
                val substitutes = players.filter { it.isSubstitute() && it.order > 0 }
                var found = false
                substitutes.forEachIndexed { index, playerWithPosition ->
                    if (!found && index < requestValues.extraHitterSize
                            && playerWithPosition.order == Constants.SUBSTITUTE_ORDER_VALUE
                    ) {
                        playerWithPosition.order = playerOrder
                        found = true
                    }
                }
            }
            Single.just(ResponseValue())
        } catch (e: NoSuchElementException) {
            Single.error(e)
        }
    }

    /**
     * @property players
     * @property playerToDelete
     * @property lineupMode
     * @property extraHitterSize
     */
    class RequestValues(
        val players: List<PlayerWithPosition>,
        val playerToDelete: Player,
        val lineupMode: Int,
        val extraHitterSize: Int
    ) : UseCase.RequestValues

    class ResponseValue : UseCase.ResponseValue
}
