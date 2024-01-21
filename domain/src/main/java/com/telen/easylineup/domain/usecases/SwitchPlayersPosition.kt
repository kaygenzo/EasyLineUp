/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.MODE_ENABLED
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.getNextAvailableOrder
import com.telen.easylineup.domain.model.isDpDh
import com.telen.easylineup.domain.model.isFlex
import com.telen.easylineup.domain.usecases.exceptions.FirstPositionEmptyException
import com.telen.easylineup.domain.usecases.exceptions.SamePlayerException
import io.reactivex.rxjava3.core.Single

internal class SwitchPlayersPosition :
    UseCase<SwitchPlayersPosition.RequestValues, SwitchPlayersPosition.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        val players = requestValues.players.toMutableList()
        val extraHittersSize = requestValues.lineup.extraHitters
        val lineupMode = requestValues.lineup.mode
        val strategy = TeamStrategy.getStrategyById(requestValues.lineup.strategy)

        val player1 = try {
            players.first { it.position == requestValues.position1.id }
        } catch (e: NoSuchElementException) {
            return Single.error(FirstPositionEmptyException())
        }

        val player2 = players.firstOrNull { it.position == requestValues.position2.id }

        if (player1 == player2) {
            return Single.error(SamePlayerException())
        }

        player1.position = requestValues.position2.id
        player2?.position = requestValues.position1.id

        val playerPositions = arrayListOf(player1)
        player2?.let {
            playerPositions.add(it)
        }

        val orderDesignatedPlayer =
            strategy.getDesignatedPlayerOrder(extraHittersSize)

        // just keep reference of the first order different of 10
        val tmpOrder = playerPositions.firstOrNull { it.order != orderDesignatedPlayer }?.order
        val oneIsFlex = playerPositions.any { it.isFlex() }
        val oneIsDp = playerPositions.any { it.isDpDh() }

        // if at least one player has a flag flex and it is a baseball team, the pitcher is always
        // the flex. Otherwise, the flex can be any other player. The only exception is the DP/DH
        playerPositions.forEach {
            if (lineupMode == MODE_ENABLED) {
                val newPosition = FieldPosition.getFieldPositionById(it.position)
                if (requestValues.teamType == TeamType.BASEBALL.id) {
                    when (newPosition) {
                        FieldPosition.PITCHER -> {
                            it.order = orderDesignatedPlayer
                            it.flags = PlayerFieldPosition.FLAG_FLEX
                        }
                        else -> {
                            it.flags = PlayerFieldPosition.FLAG_NONE
                            // if possible, just keep order, but in case of a swap with a pitcher,
                            // exchange orders
                            if (it.order == orderDesignatedPlayer) {
                                // we were a pitcher but not anymore.
                                it.order = tmpOrder
                                    ?: players.getNextAvailableOrder(listOf(it.order))
                            }
                        }
                    }
                } else {
                    // softball
                    /*
                    * Rules:
                    * - Flex can be anyone except DP.
                    * - If FLEX exists, its order is 10
                    * - If 2 players switch their positions:
                    *   if it is 2 normal players, only positions change
                    *   if it is a FLEX and a normal player, only positions change
                    *   if it is the DP and a normal player, only positions change
                    *   if is is the DP and the FLEX, switch position, flags and order
                    */
                    if (oneIsFlex && oneIsDp) {
                        if (newPosition == FieldPosition.DP_DH) {
                            it.flags = PlayerFieldPosition.FLAG_NONE
                            if (it.order == orderDesignatedPlayer) {
                                // we were a pitcher but not anymore.
                                it.order = tmpOrder
                                    ?: players.getNextAvailableOrder(listOf(it.order))
                            }
                        } else {
                            it.order = orderDesignatedPlayer
                            it.flags = PlayerFieldPosition.FLAG_FLEX
                        }
                    }
                }
            }
        }

        return Single.just(ResponseValue())
    }

    class ResponseValue : UseCase.ResponseValue
    /**
     * @property players
     * @property position1
     * @property position2
     * @property teamType
     * @property lineup
     */
    class RequestValues(
        val players: List<PlayerWithPosition>,
        val position1: FieldPosition,
        val position2: FieldPosition,
        val teamType: Int,
        val lineup: Lineup
    ) : UseCase.RequestValues
}
