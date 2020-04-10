package com.telen.easylineup.domain

import android.annotation.SuppressLint
import com.telen.easylineup.repository.data.PlayerFieldPositionsDao
import com.telen.easylineup.repository.model.*
import io.reactivex.Single

class SamePlayerException: Exception()
class FirstPositionEmptyException: Exception()

class SwitchPlayersPosition(val dao: PlayerFieldPositionsDao): UseCase<SwitchPlayersPosition.RequestValues, SwitchPlayersPosition.ResponseValue>() {

    @SuppressLint("ApplySharedPref")
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {

        val player1 = try {
            requestValues.players.first { it.position == requestValues.position1.position }
        }
        catch (e: NoSuchElementException) {
            return Single.error(FirstPositionEmptyException())
        }

        val player2 = requestValues.players.firstOrNull { it.position == requestValues.position2.position }

        if(player1 == player2)
            return Single.error(SamePlayerException())

        val player1FieldPosition = player1.toPlayerFieldPosition()
        val player2FieldPosition = player2?.toPlayerFieldPosition()

        player1FieldPosition.position = requestValues.position2.position
        player2FieldPosition?.position = requestValues.position1.position

        val playerPositions = arrayListOf(player1FieldPosition)
        player2FieldPosition?.let {
            playerPositions.add(it)
        }

        // if at least one player has a flag flex and it is a baseball team, the pitcher is always
        // the flex. Otherwise, the flex can be any other player. The only exception is the DP/DH

        playerPositions.forEach {
            if(requestValues.lineupMode == MODE_ENABLED) {
                val newPosition = FieldPosition.getFieldPosition(it.position)
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
                                it.order = playerPositions.firstOrNull { it.order != Constants.ORDER_PITCHER_WHEN_DH }?.order
                                        ?: PlayerWithPosition.getNextAvailableOrder(requestValues.players)
                            }

                        }
                    }
                }
                else { //softball
                    when(newPosition) {
                        FieldPosition.DP_DH -> {
                            it.order = PlayerWithPosition.getNextAvailableOrder(requestValues.players)
                            it.flags = PlayerFieldPosition.FLAG_NONE
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