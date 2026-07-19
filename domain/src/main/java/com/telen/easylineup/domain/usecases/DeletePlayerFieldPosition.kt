/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.ports.SchedulersProvider
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.MODE_ENABLED
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.isDpDhOrFlex
import com.telen.easylineup.domain.model.isSubstitute
import com.telen.easylineup.domain.model.reset
import io.reactivex.rxjava3.core.Completable

class DeletePlayerFieldPosition(private val schedulersProvider: SchedulersProvider) {
    operator fun invoke(
        players: List<PlayerWithPosition>,
        playerToDelete: Player,
        lineupMode: Int,
        extraHitterSize: Int
    ): Completable {
        return Completable.defer {
            try {
                // substitutes have the same position, let's use player to get the good one
                val player = players.first { it.playerId == playerToDelete.id }

                val playerIsSubstitute = player.isSubstitute()
                val playerOrder = player.order
                if (lineupMode == MODE_ENABLED && player.isDpDhOrFlex()) {
                    players.filter { it.isDpDhOrFlex() }
                } else {
                    listOf(player)
                }.forEach { it.reset() }

                // check if substitutes was a batter, update the next substitutes order to replace
                // it if necessary
                if (playerIsSubstitute && playerOrder < Constants.SUBSTITUTE_ORDER_VALUE) {
                    val substitutes = players.filter { it.isSubstitute() && it.order > 0 }
                    var found = false
                    substitutes.forEachIndexed { index, playerWithPosition ->
                        if (!found && index < extraHitterSize
                                && playerWithPosition.order == Constants.SUBSTITUTE_ORDER_VALUE
                        ) {
                            playerWithPosition.order = playerOrder
                            found = true
                        }
                    }
                }
                Completable.complete()
            } catch (e: NoSuchElementException) {
                Completable.error(e)
            }
        }.subscribeOn(schedulersProvider.io())
    }
}
