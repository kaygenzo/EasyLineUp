package com.telen.easylineup.domain.usecases

import android.annotation.SuppressLint
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.model.*
import com.telen.easylineup.domain.usecases.exceptions.FirstPositionEmptyException
import com.telen.easylineup.domain.usecases.exceptions.SamePlayerException
import io.reactivex.Single

internal class SwitchPlayersPosition(val dao: PlayerFieldPositionRepository): UseCase<SwitchPlayersPosition.RequestValues, SwitchPlayersPosition.ResponseValue>() {

    @SuppressLint("ApplySharedPref")
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {

        val player1 = try {
            requestValues.players.first { it.position == requestValues.position1.id }
        }
        catch (e: NoSuchElementException) {
            return Single.error(FirstPositionEmptyException())
        }

        val player2 = requestValues.players.firstOrNull { it.position == requestValues.position2.id }

        if(player1 == player2)
            return Single.error(SamePlayerException())

        val player1FieldPosition = player1.toPlayerFieldPosition()
        val player2FieldPosition = player2?.toPlayerFieldPosition()

        player1FieldPosition.position = requestValues.position2.id
        player2FieldPosition?.position = requestValues.position1.id

        val playerPositions = arrayListOf(player1FieldPosition)
        player2FieldPosition?.let {
            playerPositions.add(it)
        }

        // just keep reference of the first order different of 10
        val tmpOrder = playerPositions.firstOrNull { it.order != Constants.ORDER_PITCHER_WHEN_DH }?.order
        val oneIsFlex = playerPositions.any {it.flags and PlayerFieldPosition.FLAG_FLEX > 0}
        val oneIsDp = playerPositions.any {it.position == FieldPosition.DP_DH.id}

        // if at least one player has a flag flex and it is a baseball team, the pitcher is always
        // the flex. Otherwise, the flex can be any other player. The only exception is the DP/DH
        playerPositions.forEach {
            if(requestValues.lineupMode == MODE_ENABLED) {
                val newPosition = FieldPosition.getFieldPositionById(it.position)
                if(requestValues.teamType == TeamType.BASEBALL.id) {
                    when(newPosition) {
                        FieldPosition.PITCHER -> {
                            it.order = Constants.ORDER_PITCHER_WHEN_DH
                            it.flags = PlayerFieldPosition.FLAG_FLEX
                        }
                        else -> {
                            it.flags = PlayerFieldPosition.FLAG_NONE
                            //if possible, just keep order, but in case of a swap with a pitcher, exchange orders
                            if(it.order == Constants.ORDER_PITCHER_WHEN_DH) {
                                //we were a pitcher but not anymore.
                                it.order = tmpOrder ?: PlayerWithPosition.getNextAvailableOrder(requestValues.players, listOf(it.order))
                            }

                        }
                    }
                }
                else { //softball
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
                    if(oneIsFlex && oneIsDp) {
                        if(newPosition == FieldPosition.DP_DH) {
                            it.flags = PlayerFieldPosition.FLAG_NONE
                            if(it.order == Constants.ORDER_PITCHER_WHEN_DH) {
                                //we were a pitcher but not anymore.
                                it.order = tmpOrder ?: PlayerWithPosition.getNextAvailableOrder(requestValues.players, listOf(it.order))
                            }
                        }
                        else {
                            it.order = Constants.ORDER_PITCHER_WHEN_DH
                            it.flags = PlayerFieldPosition.FLAG_FLEX
                        }
                    }
                }
            }
        }

        return dao.updatePlayerFieldPositions(playerPositions)
                .andThen(Single.just(ResponseValue()))
    }

    class ResponseValue: UseCase.ResponseValue
    class RequestValues(val players: List<PlayerWithPosition>, val position1: FieldPosition, val position2: FieldPosition, val teamType: Int, val lineupMode: Int): UseCase.RequestValues
}